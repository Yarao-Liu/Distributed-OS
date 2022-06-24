package com.github.mrliu.storage.service.impl;

import com.github.mrliu.storage.constant.EncipherEnum;
import com.github.mrliu.storage.constant.FileConstants;
import com.github.mrliu.storage.constant.FileStorageType;
import com.github.mrliu.storage.encipher.EncipherFactory;
import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.entity.vo.FileDeleteVo;
import com.github.mrliu.storage.mapper.FileDataMapper;
import com.github.mrliu.storage.processor.DownloadProcessor;
import com.github.mrliu.storage.processor.FileProcessor;
import com.github.mrliu.storage.processor.UploadProcessor;
import com.github.mrliu.storage.properties.StorageProperties;
import com.github.mrliu.storage.service.FileService;
import com.github.mrliu.storage.strategy.file.FileStrategy;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Mr.Liu
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Resource
    private StorageProperties properties;

    @Resource
    private DownloadProcessor downloadProcessor;
    @Resource
    private UploadProcessor uploadProcessor;

    /**
     * 构建文件客户端对象
     *
     * @return minio客户端对象
     */
    private MinioClient buildClient() {
        StorageProperties.Properties minio = properties.getMinio();
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKeyId(), minio.getAccessKeySecret())
                .build();
    }

    @Resource
    private FileProcessor fileProcessor;
    @Resource
    private FileStrategy fileStrategy;
    @Resource
    private FileDataMapper fileDataMapper;

    @Override
    public FileDataEntity uploadFile(MultipartFile multipartFile) {
        return fileStrategy.upload(multipartFile, false);
    }

    @Override
    public void deleteFile(String id) {
        //删除fdfs文件服务器中的文件资源
        final ArrayList<FileDeleteVo> fileDeleteVos = new ArrayList<>();
        final FileDataEntity infoEntity = fileDataMapper.selectByPrimaryKey(id);
        final FileDeleteVo fileDeleteVo = new FileDeleteVo();
        fileDeleteVo.setId(id);
        fileDeleteVo.setFileName(infoEntity.getFileName());
        fileDeleteVo.setRelativePath(infoEntity.getRelativePath());
        fileDeleteVo.setGroup(infoEntity.getGroup());
        fileDeleteVos.add(fileDeleteVo);
        fileStrategy.delete(fileDeleteVos);
        //删除文件关联表
        fileDataMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void download(HttpServletResponse response, String[] ids) {
        final ArrayList<FileDataEntity> fileInfoEntities = new ArrayList<>();
        try {
            for (String id : ids) {
                final FileDataEntity infoEntity = fileDataMapper.selectByPrimaryKey(id);
                fileInfoEntities.add(infoEntity);
            }
            //真正下载文件的逻辑
            uploadProcessor.downloadFile(response, fileInfoEntities);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void saveFileInfo(FileDataEntity fileDataEntity) {
        fileDataMapper.insertSelective(fileDataEntity);
    }

    @Override
    public String filePreView(String fileName) {
        final MinioClient minioClient = buildClient();
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(properties.getMinio().getBucketName())
                    .method(Method.GET)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FileDataEntity sensitiveUpload(MultipartFile multipartFile, String encryption) {
        FileDataEntity fileDataEntity = null;
        if (FileConstants.BASE_64_ENCRYPTION.equalsIgnoreCase(encryption)) {
            fileDataEntity = base64SensitiveUpload(multipartFile);
        }
        return fileDataEntity;
    }

    @Override
    public void sensitiveDownload(String id, String encryption, HttpServletResponse response) {
        if (FileConstants.BASE_64_ENCRYPTION.equalsIgnoreCase(encryption)) {
            base64SensitiveDownload(id, response);
        }
    }

    @Nullable
    private FileDataEntity base64SensitiveUpload(MultipartFile file) {
        try {
            EncipherFactory factory = EncipherFactory.newInstance(EncipherEnum.BASE64);
            String base64String = factory.encrypt(file);
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            final String fileMd5 = fileProcessor.getFileMd5(file);
            final byte[] bytes = base64String.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            final MinioClient minioClient = downloadProcessor.buildClient();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(FileConstants.DEFAULT_BUCKET)
                    .object(file.getOriginalFilename())
                    .stream(stream, bytes.length, -1)
                    .build());
            final FileDataEntity build = FileDataEntity.builder()
                    .bucketName(FileConstants.DEFAULT_BUCKET)
                    .md5(fileMd5)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileStorageType(FileStorageType.MINIO)
                    .isDelete(false)
                    .relativePath(FileConstants.DEFAULT_BUCKET + FileConstants.SEPARATOR + fileMd5 + FileConstants.DOT + extension)
                    .fileName(fileMd5 + FileConstants.DOT + extension)
                    .url(FileConstants.DEFAULT_BUCKET + FileConstants.SEPARATOR + fileMd5 + FileConstants.DOT + extension)
                    .fileExt(extension)
                    .build();
            build.setId(UUID.randomUUID().toString());
            this.saveFileInfo(build);
            return build;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件加密上传失败!");
        }
        return null;
    }

    public void base64SensitiveDownload(String id, HttpServletResponse response) {
        final FileDataEntity infoEntity = fileDataMapper.selectByPrimaryKey(id);
        final MinioClient minioClient = downloadProcessor.buildClient();
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(FileConstants.DEFAULT_BUCKET)
                    .object(infoEntity.getOriginalFileName())
                    .build());
            //输入流转字节数组
            byte[] bytes = DownloadProcessor.toByteArray(inputStream);
            //字节数组转字符串
            String fileStr = new String(bytes);
            //字符串解码为字节数组
            EncipherFactory factory = EncipherFactory.newInstance(EncipherEnum.BASE64);
            bytes = factory.decrypt(fileStr);

            final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            IOUtils.copy(stream, response.getOutputStream());
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(infoEntity.getOriginalFileName(), "UTF-8"));
            response.setContentType("application/x-msdownload");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

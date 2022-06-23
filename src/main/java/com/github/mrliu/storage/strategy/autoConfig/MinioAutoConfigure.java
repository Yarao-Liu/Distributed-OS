package com.github.mrliu.storage.strategy.autoConfig;

import com.github.mrliu.storage.properties.StorageProperties;
import com.github.mrliu.storage.constant.FileStorageType;
import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.strategy.chunk.AbstractFileChunkStrategy;
import com.github.mrliu.storage.strategy.file.AbstractFileStrategy;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;
import com.github.mrliu.storage.entity.vo.FileDeleteVo;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.mrliu.storage.constant.FileConstants.*;

/**
 * @author Mr.Liu
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(name = "file.type", havingValue = "MINIO")
public class MinioAutoConfigure {
    @Resource
    private StorageProperties properties;

    private StorageProperties.Properties minio;

    private MinioClient buildClient() {
        minio = properties.getMinio();
        return MinioClient.builder()
                .credentials(minio.getAccessKeyId(), minio.getAccessKeySecret())
                .endpoint(minio.getEndpoint())
                .build();
    }

    @Service
    public class MinioServiceImpl extends AbstractFileStrategy {

        @SuppressWarnings("All")
        @Override
        public FileDataEntity uploadFile(FileDataEntity fileDataEntity, MultipartFile file, boolean isPart) {
            try {

                final MinioClient minioClient = buildClient();
                //获取文件名称判断上传路径
                String fileName = file.getOriginalFilename();
                String temp = fileName;
                if (!isPart) {
                    //小文件上传上传使用随机文件名
                    fileName = UUID.randomUUID() + DOT + fileDataEntity.getFileExt();
                    temp = fileName;
                } else {
                    //分块上传不随机生成文件名称
                    temp = TEMP + fileName;
                }
                //上传文件到对象存储服务
                final String bucketName = minio.getBucketName();
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                }
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(temp)
                        .stream(file.getInputStream(), file.getSize(), 1024 * 1024 * 100L)
                        .build());
                //保存文件对应的信息
                fileDataEntity.setRelativePath(bucketName + SEPARATOR + fileName);
                fileDataEntity.setUrl(bucketName + SEPARATOR + fileName);
                fileDataEntity.setFileStorageType(FileStorageType.MINIO);
                fileDataEntity.setBucketName(bucketName);
                fileDataEntity.setFileName(fileName);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return fileDataEntity;
        }

        /**
         * 文件删除接口
         *
         * @param fileDeleteVo 文件删除
         */
        @Override
        public void deleteFile(FileDeleteVo fileDeleteVo) {
            try {
                MinioClient minioClient = buildClient();
                final RemoveObjectArgs build = RemoveObjectArgs.builder()
                        .bucket(minio.getBucketName())
                        .object(fileDeleteVo.getRelativePath() + fileDeleteVo.getFileName())
                        .build();

                minioClient.removeObject(build);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ALL")
    @Service
    public class MinioChunkSerivceImpl extends AbstractFileChunkStrategy {
        @Override
        protected FileDataEntity merge(List<File> files,
                                       String fileName,
                                       FileChunkMergeVo fileChunkMergeVo) throws Exception {
            try {
                final MinioClient minioClient = buildClient();
                final String bucketName = minio.getBucketName();
                System.out.println(bucketName);
                //获取所有的文件分块
                List<ComposeSource> chunkList = new ArrayList<ComposeSource>();
                for (int i = 0; i < files.size(); i++) {
                    chunkList.add(
                            ComposeSource.builder()
                                    .bucket(bucketName)
                                    .object(TEMP + String.valueOf(i))
                                    .build());
                }
                //文件合并
                minioClient.composeObject(
                        ComposeObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .sources(chunkList)
                                .build());
                //删除文件分块文件
                for (int i = 0; i < files.size(); i++) {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(TEMP + String.valueOf(i)).build());
                }
                //创建对象并返回
                final String name = fileChunkMergeVo.getName();
                return FileDataEntity.builder()
                        .md5(fileChunkMergeVo.getMd5())
                        .fileExt(fileChunkMergeVo.getExt())
                        .fileSize(fileChunkMergeVo.getSize())
                        .fileName(name)
                        .originalFileName(fileName)
                        .fileStorageType(FileStorageType.MINIO)
                        .url(bucketName + SEPARATOR + name)
                        .relativePath(bucketName + SEPARATOR + name)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

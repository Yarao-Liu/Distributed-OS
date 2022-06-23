package com.github.mrliu.storage.processor;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.properties.StorageProperties;
import com.github.mrliu.storage.strategy.file.FileStrategy;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;
import com.github.mrliu.storage.entity.vo.FileUploadVo;
import com.github.mrliu.storage.constant.FileConstants;
import com.github.mrliu.storage.mapper.FileDataMapper;
import com.github.mrliu.storage.utils.WebUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Mr.Liu
 */
@Slf4j
@Component
public class UploadProcessor {
    @Resource
    private StorageProperties storageProperties;

    @Resource
    private WebUploader webUploader;

    @Resource
    private FileProcessor fileProcessor;

    @Resource
    private FileStrategy fileStrategy;

    @Resource
    private FileDataMapper fileDataMapper;

    /**
     * 分块上传的主流程
     *
     * @param file         文件实体
     * @param fileUploadVo 文件参数
     * @throws IOException IOException
     */
    public void uploadPart(MultipartFile file, FileUploadVo fileUploadVo) throws IOException {

        String storagePath = storageProperties.getStoragePath();
        //分片上传的相对路径
        String abstractFolder = LocalDate.now().format(FileConstants.DTF);
        //写文件的临时目录
        String uploadFolder = storagePath + abstractFolder;
        //为需要上传的分片文件准备对应的存储位置
        File targetFile = webUploader.getReadySpace(fileUploadVo, uploadFolder);
        //块文件保存到本地
        file.transferTo(targetFile);
        //转化成一个MultipartFile对象
        MultipartFile partFile = fileProcessor.file2MultipartFile(targetFile, targetFile.getName());
        //分块上传一个文件
        fileStrategy.upload(partFile, true);
    }


    /**
     * 判断文件是分片上传还是小文件上传
     *
     * @param file         文件实体
     * @param fileUploadVo 文件属性
     * @return FileChunkMergeVo
     * @throws IOException IOException
     */
    public FileChunkMergeVo uploadFileMethod(MultipartFile file, FileUploadVo fileUploadVo) throws IOException {

        if (fileUploadVo.getChunks() == null || fileUploadVo.getChunks() <= 0) {
            //小文件上传
            FileDataEntity upload = fileStrategy.upload(file, false);
            upload.setMd5(fileUploadVo.getMd5());
            upload.setId(UUID.randomUUID().toString());
            fileDataMapper.insertSelective(upload);
            return null;

        } else {
            //当前上传属于分片上传
            uploadPart(file, fileUploadVo);
            //封装上传合并信息vo
            FileChunkMergeVo fileChunkMergeVo = new FileChunkMergeVo();
            fileChunkMergeVo.setOriginalFileName(file.getOriginalFilename());
            BeanUtils.copyProperties(fileUploadVo, fileChunkMergeVo);
            log.info("接收到分片:" + file + "分片信息:" + fileChunkMergeVo);

            return fileChunkMergeVo;
        }
    }

    public void downloadFile(HttpServletResponse response, ArrayList<FileDataEntity> fileData) throws IOException {
        /*
         * 生成文件名
         */
        String extName = this.retrieveFileName(fileData);
        /*
         * 获取文件url
         */
        Map<String, String> map = this.filterFile(fileData);
        /*
         * 设置响应头
         */
        this.setResponseHeader(response, extName);
        /*
         * 下载文件逻辑
         */
        fileProcessor.downloadFile(response, map);
    }

    public String retrieveFileName(ArrayList<FileDataEntity> fileData) {
        String extName = fileData.get(0).getOriginalFileName();
        if (fileData.size() > 1) {
            extName = extName.substring(0,
                    extName.lastIndexOf(".")) + "等.zip";
        }
        return extName;
    }

    /**
     * 过滤无效文件名
     *
     * @param fileData 文件信息
     * @return 文件信息map
     */
    public Map<String, String> filterFile(ArrayList<FileDataEntity> fileData) {
        //key--fileName
        //value--url
        Map<String, String> map = new LinkedHashMap<>(fileData.size());
        Map<String, Integer> duplicateFile = new HashMap<>(fileData.size());

        fileData.stream()
                .filter((file) -> file != null && !StringUtils.isEmpty(file.getUrl()))
                .forEach((file) -> {
                    String originalFileName = file.getOriginalFileName();
                    if (map.containsKey(originalFileName)) {
                        if (duplicateFile.containsKey(originalFileName)) {
                            duplicateFile.put(originalFileName, duplicateFile.get(originalFileName) + 1);
                        } else {
                            duplicateFile.put(originalFileName, 1);
                        }
                        //解决压缩包内文件名重复的问题
                        originalFileName = buildNewFileName(originalFileName, duplicateFile.get(originalFileName));
                    }
                    map.put(originalFileName, file.getUrl());
                });
        System.out.println(map);
        return map;
    }

    public static String buildNewFileName(String filename, Integer order) {
        return new StringBuffer().append(filename).
                insert(filename.lastIndexOf("."), "(" + order + ")").toString();
    }

    /**
     * 设置响应头信息
     * @param response 响应实体
     * @param extName 文件名称
     * @throws UnsupportedEncodingException 不支持编码异常
     */
    public void setResponseHeader(HttpServletResponse response, String extName) throws UnsupportedEncodingException {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-disposition",
                "attachment;filename=" + URLEncoder.encode(extName, "UTF-8"));
        response.setContentType("application/x-msdownload");
    }
}

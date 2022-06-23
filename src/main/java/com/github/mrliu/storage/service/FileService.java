package com.github.mrliu.storage.service;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Mr.Liu
 */
public interface FileService {
    /**
     * 文件上传接口
     *
     * @param multipartFile 文件实体
     * @return true/false
     */
    FileDataEntity uploadFile(MultipartFile multipartFile);

    /**
     * 文件删除接口
     *
     * @param id 文件关联表id
     */
    void deleteFile(String id);

    /**
     * 单文件直接下载，多文件打包下载
     *
     * @param response response
     * @param ids      ids
     */
    void download(HttpServletResponse response, String[] ids);

    /**
     * 保存文件的扩展信息
     *
     * @param fileDataEntity fileInfoEntity
     */
    void saveFileInfo(FileDataEntity fileDataEntity);


    /**
     * 文件预览
     * @param fileName 文件名称
     * @return String
     */
    String filePreView(String fileName);

    /**
     * 加密上传
     * @param multipartFile 文件实体
     * @param encryption 加密方式
     * @return FileInfoEntity
     */
    FileDataEntity sensitiveUpload(MultipartFile multipartFile, String encryption);

    /**
     * 解密下载
     * @param id 文件id
     * @param encryption 加密方式
     * @param response 相应对象
     */
    void sensitiveDownload(String id, String encryption, HttpServletResponse response);
}

package com.github.mrliu.storage.entity.param;

import lombok.Data;

/**
 * @author Mr.Liu
 * @title: PartInfo
 * @description: 文件分片前的基本参数
 * @date 2022/3/2 9:49
 */
@Data
public class FileUploadParam {
    /**
     * 文件在服务器上目标路径
     */
    private String folder;
    /**
     * 文件的真实名称
     */
    private String fileName;
    /**
     * 文件的类型
     */
    private String contentType;
    /**
     * 文件分片的总数
     */
    private String partCount;
    /**
     * 文件的md5
     */
    private String md5;

    private String fileSize;
}

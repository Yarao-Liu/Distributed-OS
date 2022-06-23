package com.github.mrliu.storage.entity.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author Mr.Liu
 */
@Data
@ToString
public class FileUploadVo {
    /**
     * md5
     */
    private String md5;
    /**
     * size
     */
    private Long size;
    /**
     * 文件唯一名称
     */
    private String name;
    /**
     * 分片总数
     */
    private Integer chunks;
    /**
     * 当前分片
     */
    private Integer chunk;
    /**
     * 最后更新时间
     */
    private String lastModifiedDate;
    /**
     * 类型
     */
    private String type;
    /**
     * 文件后缀
     */
    private String ext;
    /**
     * 文件夹的唯一标识
     */
    private Long folderId;
}

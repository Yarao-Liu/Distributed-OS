package com.github.mrliu.storage.entity.vo;

import lombok.Data;

/**
 * @author Mr.Liu
 */
@Data
public class FileChunkMergeVo {
    /**
     * 文件的唯一名称
     */
    private String name;

    /**
     * 文件原始文件名
     */
    private String originalFileName;

    /**
     * 文件的MD5
     */
    private String md5;
    /**
     * 分片的总数
     */
    private Integer chunks;
    /**
     * 文件后缀
     */
    private String ext;
    /**
     * 文件件ID
     */
    private Long folderId;
    /**
     * 文件的总大小
     */
    private Long size;
    /**
     * 文件的类型
     */
    private String contextType;
}

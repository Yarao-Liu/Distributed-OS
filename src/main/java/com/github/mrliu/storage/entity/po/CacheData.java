package com.github.mrliu.storage.entity.po;

import lombok.Data;
import lombok.ToString;

/**
 * @author Mr.Liu
 * @description: 存储在redis中的数据格式
 * @date 2022/3/14 10:11
 */
@Data
@ToString
public class CacheData {

    private Long id;
    private String fileName;
    private String fileMd5;
    private Integer fileStatus;
    private String uploadId;
    private Integer totalChunk;
    private String fileUrl;
}

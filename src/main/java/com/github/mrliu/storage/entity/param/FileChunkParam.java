package com.github.mrliu.storage.entity.param;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.Liu
 * @description: 分片信息缓存到redis的实体
 * @date 2022/3/14 10:29
 */
@Data
@ToString
public class FileChunkParam {


    private String fileName;
    private String fileMd5;
    private LocalDateTime expiryTime;
    private List<String> uploadUrls;
    private String uploadId;
}

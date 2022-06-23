package com.github.mrliu.storage.entity.po;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.Liu
 * @description: 文件预上传响应的实体
 * @date 2022/3/14 10:01
 */
@Data
@ToString
public class FileUploadData {

    private String uploadId;
    private LocalDateTime expiryTime;
    private List<String> uploadUrls;
}

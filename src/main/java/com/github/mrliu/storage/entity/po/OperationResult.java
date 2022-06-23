package com.github.mrliu.storage.entity.po;

import lombok.Data;

import java.util.List;

/**
 * @author Mr.Liu
 * @description: 操作响应的结果
 * @date 2022/3/14 9:42
 */
@Data
public class OperationResult {

    private String url;

    /**
     * 状态：0-未上传，1-已上传，2-上传中
     */
    private Integer status;

    /**
     * 已上传分片列表
     */
    private List<Integer> chunkUploadedList;

    private String uploadId;

    private List<String> uploadUrls;
}

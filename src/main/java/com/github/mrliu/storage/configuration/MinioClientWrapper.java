package com.github.mrliu.storage.configuration;


import com.google.common.collect.HashMultimap;
import com.github.mrliu.storage.entity.po.FileUploadData;
import com.github.mrliu.storage.processor.DownloadProcessor;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;

import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.mrliu.storage.constant.FileConstants.UPLOAD_ID;

/**
 * @author Mr.Liu
 * @date 2021/3/23
 */
@Slf4j
@Component
public class MinioClientWrapper {

    private CustomMinioClient customMinioClient;
    @Resource
    private DownloadProcessor downloadProcessor;

    @PostConstruct
    public void init() {
        customMinioClient = new CustomMinioClient(downloadProcessor.buildClient());
    }

    /**
     * 单文件签名上传
     *
     * @param objectName 文件全路径名称
     * @return /
     */
    public String getUploadObjectUrl(String bucketName, String objectName) {
        // 上传文件时携带content-type头即可
        try {
            return customMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs
                            .builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.DAYS)
                            //.extraHeaders(headers)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 初始化分片上传
     *
     * @param objectName  文件全路径名称
     * @param partCount   分片数量
     * @param contentType 类型，如果类型使用默认流会导致无法预览
     * @return /
     */
    public FileUploadData initMultiPartUpload(String bucketName, String objectName, int partCount, String contentType) {
        String uploadId;
        List<String> uploadUrls = new ArrayList<>();
        try {
            if ("".equals(contentType) || contentType == null) {
                contentType = "application/octet-stream";
            }
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type", contentType);
            uploadId = customMinioClient.initMultiPartUpload(bucketName,
                    null,
                    objectName,
                    headers,
                    null);

            Map<String, String> reqParams = new HashMap<>(8);
            reqParams.put(UPLOAD_ID, uploadId);
            for (int i = 1; i <= partCount; i++) {
                reqParams.put("partNumber", String.valueOf(i));
                String uploadUrl = customMinioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(bucketName)
                                .object(objectName)
                                .expiry(1, TimeUnit.DAYS)
                                .extraQueryParams(reqParams)
                                .build());
                uploadUrls.add(uploadUrl);
            }
        } catch (Exception e) {
            log.error("initMultiPartUpload Error:" + e);
            return null;
        }
        FileUploadData uploadInfo = new FileUploadData();
        uploadInfo.setUploadId(uploadId);
        uploadInfo.setUploadUrls(uploadUrls);
        uploadInfo.setExpiryTime(LocalDateTime.now().plusDays(1));
        return uploadInfo;
    }

    /**
     * 分片上传完后合并
     *
     * @param bucketName 文件的桶名称
     * @param objectName 文件全路径名称
     * @param uploadId   返回的uploadId
     * @return /
     */
    public String mergeMultipartUpload(String bucketName, String objectName, String uploadId) {
        ObjectWriteResponse response;
        try {
            //TODO::目前仅做了最大1000分片
            Part[] parts = new Part[1000];

            ListPartsResponse partResult = customMinioClient.listMultipart(bucketName, null,
                    objectName, 1000, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            response = customMinioClient.mergeMultipartUpload(bucketName, null,
                    objectName, uploadId, parts, null, null);
            log.info("merge result: bucket:" + response.bucket() + "\tobject:" + response.object());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return response.region();
    }

}

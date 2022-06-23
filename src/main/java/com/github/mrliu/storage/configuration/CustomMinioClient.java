package com.github.mrliu.storage.configuration;

import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.messages.Part;

/**
 * @author Mr.Liu
 * @title: CustomMinioClient
 * @description: 自定义客户端
 * @date 2022/3/1 17:33
 */
public class CustomMinioClient extends MinioClient {

    public CustomMinioClient(MinioClient minioClient) {
        super(minioClient);
    }

    /**
     * 初始化分片
     */
    public String initMultiPartUpload(String bucket, String region,
                                      String object, Multimap<String, String> headers,
                                      Multimap<String, String> extraQueryParams) throws Exception {
        CreateMultipartUploadResponse response = this.createMultipartUpload(bucket, region, object, headers, extraQueryParams);
        return response.result().uploadId();
    }

    /**
     * 合并分片
     */
    public ObjectWriteResponse mergeMultipartUpload(String bucketName, String region,
                                                    String objectName, String uploadId,
                                                    Part[] parts, Multimap<String, String> extraHeaders,
                                                    Multimap<String, String> extraQueryParams) throws Exception {

        return this.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 分片列表
     */
    public ListPartsResponse listMultipart(String bucketName, String region,
                                           String objectName, Integer maxParts,
                                           Integer partNumberMarker, String uploadId,
                                           Multimap<String, String> extraHeaders,
                                           Multimap<String, String> extraQueryParams) throws Exception {
        return this.listParts(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }
}

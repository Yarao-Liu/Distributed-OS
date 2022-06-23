package com.github.mrliu.storage.service.impl;

import com.github.mrliu.storage.entity.param.FileChunkParam;
import com.github.mrliu.storage.entity.param.FileMergeParam;
import com.github.mrliu.storage.entity.param.FileUploadParam;
import com.github.mrliu.storage.entity.po.FileUploadData;

import com.github.mrliu.storage.service.UploadService;
import com.github.mrliu.storage.configuration.CustomMinioClient;
import com.github.mrliu.storage.configuration.MinioClientWrapper;
import com.github.mrliu.storage.constant.FileConstants;
import com.github.mrliu.storage.entity.po.CacheData;
import com.github.mrliu.storage.processor.DownloadProcessor;
import io.minio.ListPartsResponse;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.mrliu.storage.constant.FileConstants.DEFAULT_BUCKET;
import static com.github.mrliu.storage.constant.FileConstants.UNDER_LINE;

/**
 * @author Mr.Liu
 * @title: UploadServiceImpl
 * @description: TODO
 * @date 2022/3/2 9:22
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Resource
    private MinioClientWrapper minioClientWrapper;
    @Resource
    private DownloadProcessor downloadProcessor;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    public static final String BUCKET_NAME = "images";

    /**
     * 初始化分片
     *
     * @param param 大文件初始化参数
     * @return MinioUploadInfo
     */
    @Override
    public FileUploadData initMultiPartUpload(FileUploadParam param) {

        final String fileName = param.getFileName();
        final int partCount = Integer.parseInt(param.getPartCount());
        final String contentType = param.getContentType();
        FileUploadData uploadInfo = minioClientWrapper.initMultiPartUpload(BUCKET_NAME, fileName, partCount, contentType);
        if (uploadInfo != null) {
            //保存文件上传信息到redis
            final CacheData saveParam = new CacheData();
            saveParam.setUploadId(uploadInfo.getUploadId());
            saveParam.setFileMd5(param.getMd5());
            saveParam.setFileName(fileName);
            saveParam.setTotalChunk(partCount);
            saveParam.setFileStatus(FileConstants.MinioFileStatusEnum.UN_UPLOADED.ordinal());
            redisTemplate.boundValueOps(saveParam.getFileMd5()).set(saveParam);

            //保存分片信息到redis
            final FileChunkParam chunkUploadInfoParam = new FileChunkParam();
            chunkUploadInfoParam.setUploadId(uploadInfo.getUploadId());
            chunkUploadInfoParam.setUploadUrls(uploadInfo.getUploadUrls());
            chunkUploadInfoParam.setExpiryTime(uploadInfo.getExpiryTime());
            chunkUploadInfoParam.setFileMd5(param.getMd5());
            chunkUploadInfoParam.setFileName(param.getFileName());
            redisTemplate.boundValueOps(uploadInfo.getUploadId() + UNDER_LINE + param.getMd5()).set(chunkUploadInfoParam);
        }
        return uploadInfo;
    }


    @Override
    public String mergeMultipartUpload(FileMergeParam param) {
        final String result = minioClientWrapper.mergeMultipartUpload(BUCKET_NAME, param.getFileName(), param.getUploadId());
        if (!StringUtils.isEmpty(result)) {
            final CacheData uploadInfoDTO = new CacheData();
            uploadInfoDTO.setId(new Random().nextLong());
            uploadInfoDTO.setFileUrl(result);
            uploadInfoDTO.setFileMd5(param.getMd5());
            uploadInfoDTO.setFileName(param.getFileName());
            uploadInfoDTO.setUploadId(param.getUploadId());
            uploadInfoDTO.setFileStatus(FileConstants.MinioFileStatusEnum.UPLOADED.ordinal());
            //更新数据库的状态
            redisTemplate.boundValueOps(param.getMd5()).set(uploadInfoDTO);
            //上传完成删除分片信息
            redisTemplate.delete(param.getUploadId() + UNDER_LINE + param.getMd5());
        }
        return result;
    }

    @Override
    public List<Integer> listUploadParts(String fileName, String uploadId) {
        log.error("listUploadParts:" + uploadId);
        int maxParts = 1000;
        ListPartsResponse partsResponse;
        try {
            partsResponse = new CustomMinioClient(downloadProcessor.buildClient())
                    .listMultipart(
                            DEFAULT_BUCKET, null,
                            fileName, maxParts,
                            0, uploadId,
                            null, null);
            if (partsResponse == null) {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("查询文件分片列表错误：{}，uploadId:{}", e, uploadId);
            return null;
        }
        return partsResponse.result().partList().stream().map(Part::partNumber).collect(Collectors.toList());
    }
}

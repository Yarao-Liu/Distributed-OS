package com.github.mrliu.storage.rest;

import com.github.mrliu.storage.entity.param.FileChunkParam;
import com.github.mrliu.storage.entity.param.FileMergeParam;
import com.github.mrliu.storage.entity.param.FileUploadParam;
import com.github.mrliu.storage.entity.po.FileUploadData;
import com.github.mrliu.storage.entity.po.OperationResult;

import com.github.mrliu.storage.service.UploadService;
import com.google.common.collect.ImmutableMap;
import com.github.mrliu.storage.constant.FileConstants;
import com.github.mrliu.storage.entity.po.CacheData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.github.mrliu.storage.constant.FileConstants.DEFAULT_BUCKET;
import static com.github.mrliu.storage.constant.FileConstants.UNDER_LINE;

/**
 * @author Mr.Liu
 * @title: CustomController
 * @description: 自定义控制器
 * @date 2022/3/2 9:24
 */
@RestController
@CrossOrigin
@Slf4j
public class CustomController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UploadService uploadService;

    /**
     * 分片初始化
     *
     * @param param 请求参数-此处简单处理
     * @return ResponseEntity
     */
    @PostMapping("/multipart/init")
    public ResponseEntity<Object> getUploadId(@RequestBody FileUploadParam param) {
        if (param.getFolder() == null) {
            param.setFolder(DEFAULT_BUCKET);
        }
        final FileUploadData uploadInfo = uploadService.initMultiPartUpload(param);
        final String uploadId = uploadInfo.getUploadId();
        log.info("multipart init:" + uploadId);
        return new ResponseEntity<>(uploadInfo, HttpStatus.OK);
    }

    @GetMapping("/multipart/check")
    public OperationResult checkMd5(String md5) {
        //从缓存中查询
        log.info("即将上传的文件的MD5:" + md5);
        final OperationResult result = new OperationResult();
        final CacheData cacheData = (CacheData) redisTemplate.boundValueOps(md5).get();
        //未上传
        if (cacheData == null) {
            result.setStatus(FileConstants.MinioFileStatusEnum.UN_UPLOADED.ordinal());
            return result;
        }
        //已上传,即秒传
        if (cacheData.getFileStatus() == FileConstants.MinioFileStatusEnum.UPLOADED.ordinal()) {
            result.setStatus(FileConstants.MinioFileStatusEnum.UPLOADED.ordinal());
            result.setUrl(cacheData.getFileUrl());
            return result;
        }
        // 查询已上传分片列表并返回已上传列表
        List<Integer> chunkUploadedList = uploadService.listUploadParts(cacheData.getFileName(), cacheData.getUploadId());
        result.setStatus(FileConstants.MinioFileStatusEnum.UPLOADING.ordinal());
        result.setChunkUploadedList(chunkUploadedList);
        result.setUploadId(cacheData.getUploadId());
        final FileChunkParam param = (FileChunkParam) redisTemplate
                .boundValueOps(cacheData.getUploadId() + UNDER_LINE + md5).get();
        result.setUploadUrls(param == null || param.getUploadUrls() == null ? new ArrayList<>() : param.getUploadUrls());
        log.error("multipart check:" + cacheData.getUploadId());
        return result;
    }

    /**
     * 分片 合并
     *
     * @date 2022/3/2 10:35
     */
    @PostMapping("/multipart/complete")
    public ResponseEntity<Object> completeMultiPartUpload(FileMergeParam param) {
        log.error("multipart complete:" + param.getUploadId());
        final String result = uploadService.mergeMultipartUpload(param);
        if (StringUtils.isEmpty(result)) {
            return new ResponseEntity<>(ImmutableMap.of("success", result), HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(ImmutableMap.of("success", result), HttpStatus.OK);
    }
}

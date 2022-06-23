package com.github.mrliu.storage.service;

import com.github.mrliu.storage.entity.param.FileMergeParam;
import com.github.mrliu.storage.entity.po.FileUploadData;
import com.github.mrliu.storage.entity.param.FileUploadParam;

import java.util.List;

/**
 * @author Mr.Liu
 * @title: UploadService
 * @description: TODO
 * @date 2022/3/2 9:22
 */
public interface UploadService {
    /**
     * 分片上传初始化
     * @param param  大文件初始化参数
     * @return MinioUploadInfo
     */
    FileUploadData initMultiPartUpload(FileUploadParam param);

    /**
     * 合并分片
     * @param param 参数
     * @return URL
     */
    String mergeMultipartUpload(FileMergeParam param);

    /**
     * 获取已上传的文件块列表
     * @param fileName 文件名
     * @param uploadId 上传Id
     * @return List<Integer>
     */
    List<Integer> listUploadParts(String fileName, String uploadId);
}

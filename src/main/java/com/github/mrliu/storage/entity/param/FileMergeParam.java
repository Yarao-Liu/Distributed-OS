package com.github.mrliu.storage.entity.param;

import lombok.Data;

/**
 * @author Mr.Liu
 * @title: MergeInfo 文件合并参数
 * @description: 文件合并方法的参数
 * @date 2022/3/2 10:33
 */
@Data
public class FileMergeParam {

    private String fileName;

    private String uploadId;

    private String md5;
}

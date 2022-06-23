package com.github.mrliu.storage.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除文件条件封装
 *
 * @author Mr.Liu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteVo {

    private String id;

    private String group;

    private String fileName;

    private String relativePath;

    private Boolean file;
}

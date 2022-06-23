package com.github.mrliu.storage.utils;

import com.github.mrliu.storage.entity.vo.FileUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 封装分片操作的工具类
 *
 * @author Mr.Liu
 */
@Slf4j
@Component
public class WebUploader {
    /**
     * 为分片上传创建对应的保存位置，同时还可以创建临时文件
     *
     * @param fileUploadVo 文件上传信息
     * @param path         需要创建的路径
     * @return 文件实体
     */
    public File getReadySpace(FileUploadVo fileUploadVo, String path) {
        log.debug("上传的文件信息: " + fileUploadVo.toString());
        log.debug("上传的文件路径: " + path);
        boolean b = createFileFolder(path, false);
        if (!b) {
            return null;
        }
        //将文件分片保存到文件名对对应的MD5构成的目录
        String fileFolder = fileUploadVo.getName();
        if (fileFolder == null) {
            return null;
        }
        path += "/" + fileFolder;
        //创建临时文件和存放分片的目录
        b = createFileFolder(path, true);
        if (!b) {
            return null;
        }
        //构造需要上传的分片文件对应的路径
        String s = String.valueOf(fileUploadVo.getChunk());
        return new File(path, s);
    }

    /**
     * 具体执行创建分片所在的目录和临时文件
     *
     * @param file   文件路径
     * @param hasTmp 是否需要创建临时文件
     * @return 创建是否成功
     */
    private boolean createFileFolder(String file, boolean hasTmp) {
        File tmpFile = new File(file);
        if (!tmpFile.exists()) {
            try {
                tmpFile.mkdirs();
            } catch (Exception e) {
                log.error("创建分片所在的目录失败", e);
                return false;
            }

        }
        if (hasTmp) {
            //需要创建临时文件
            tmpFile = new File(file + ".tmp");
            if (tmpFile.exists()) {
                return tmpFile.setLastModified(System.currentTimeMillis());
            } else {
                //临时文件不存在，需要创建
                try {
                    tmpFile.createNewFile();
                } catch (Exception e) {
                    log.error("创建分片对应的临时文件失败", e);
                    return false;
                }

            }

        }
        return true;
    }
}

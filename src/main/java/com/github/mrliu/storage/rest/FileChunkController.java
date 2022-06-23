package com.github.mrliu.storage.rest;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.strategy.chunk.FileChunkStrategy;
import com.github.mrliu.storage.utils.CacheRedis;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;
import com.github.mrliu.storage.entity.vo.FileUploadVo;
import com.github.mrliu.storage.processor.UploadProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * fastDFS分片上传
 *
 * @author Mr.Liu
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/chunk")
public class FileChunkController {

    /**
     * 文件分块策略类
     */
    @Resource
    private FileChunkStrategy fileChunkStrategy;

    @Resource
    private CacheRedis cacheRedis;

    @Resource
    private UploadProcessor uploadProcessor;

    /**
     * 分片上传文件
     *
     * @param file         文件实体
     * @param fileUploadVo 上传vo
     * @return 文件合并vo
     */
    @PostMapping("/upload")
    public FileChunkMergeVo uploadFile(@RequestParam(value = "file") MultipartFile file,
                                       FileUploadVo fileUploadVo) throws Exception {

        if (file == null || file.isEmpty()) {
            log.error("分片上传的分片文件为空!");
            return null;
        }

        if (cacheRedis.findCacheToRedis(fileUploadVo.getMd5()) == null) {
            return uploadProcessor.uploadFileMethod(file, fileUploadVo);
        } else {
            //秒传和小文件上传直接返回null
            return null;
        }
    }

    /**
     * 文件合并接口
     *
     * @param fileChunkMergeVo 文件块信息
     * @return 文件详细信息
     */
    @PostMapping("/merge")
    public FileDataEntity mergeFile(FileChunkMergeVo fileChunkMergeVo) {
        System.out.println(fileChunkMergeVo);
        //redis中获取文件信息
        final FileDataEntity fileCache = cacheRedis.findCacheToRedis(fileChunkMergeVo.getMd5());
        if (fileCache != null) {
            return fileCache;
        }
        //redis中不存在,文件合并并保存到redis中
        final FileDataEntity fileDataEntity = fileChunkStrategy.chunkMerge(fileChunkMergeVo);
        cacheRedis.updateRedisToCache(fileDataEntity);
        return fileDataEntity;
    }
}

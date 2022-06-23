package com.github.mrliu.storage.utils;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author Mr.Liu
 */
@Slf4j
@Component
public class CacheRedis {

    @Resource
    private FileService fileService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试从redis 中取值
     *
     * @param md5Param 文件的MD5
     * @return FileInfoEntity
     */
    public FileDataEntity findCacheToRedis(String md5Param) {
        //尝试到redis中获取
        if (md5Param == null) {
            return null;
        }
        final FileDataEntity fileDataEntity = (FileDataEntity) redisTemplate.boundValueOps(md5Param).get();
        if (fileDataEntity != null) {
            fileDataEntity.setId(UUID.randomUUID().toString());
            fileDataEntity.setCrtTime(new Date());
            fileService.saveFileInfo(fileDataEntity);
            log.info("缓存中有值!实现文件秒传！");
        }
        return fileDataEntity;
    }

    /**
     * 保存文件信息到redis
     *
     * @param fileData fileData
     */
    public void updateRedisToCache(FileDataEntity fileData) {

        if (fileData != null && fileData.getMd5() != null) {
            redisTemplate.boundValueOps(fileData.getMd5()).set(fileData);
        }
    }
}

package com.github.mrliu.storage.rest;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.service.FileService;
import com.github.mrliu.storage.utils.CacheRedis;
import com.github.mrliu.storage.processor.FileProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.github.mrliu.storage.constant.FileConstants.BASE_64_ENCRYPTION;

/**
 * @author Mr.Liu
 */
@Slf4j
@RestController
public class FileController {
    @Resource
    private CacheRedis cacheRedis;

    @Resource
    private FileProcessor fileProcessor;

    @Resource
    private FileService fileService;
    /**
     * 健康测试接口
     *
     * @return 字符串
     */
    @RequestMapping("/hello")
    public String sayHello() {
        return "hello world";
    }

    /**
     * 上传测试接口
     *
     * @param multipartFile 文件实体
     * @return true/false
     */
    @PostMapping("/upload")
    public FileDataEntity upload(@RequestParam("file") MultipartFile multipartFile) {
        String md5Param = fileProcessor.getFileMd5(multipartFile);
        FileDataEntity fileDataEntity = cacheRedis.findCacheToRedis(md5Param);
        if (fileDataEntity != null) {
            return fileDataEntity;
        }
        log.info("开始文件上传...");
        FileDataEntity infoEntity = fileService.uploadFile(multipartFile);
        infoEntity.setId(UUID.randomUUID().toString());
        infoEntity.setMd5(md5Param);
        fileService.saveFileInfo(infoEntity);
        //保存文件信息到redis
        cacheRedis.updateRedisToCache(infoEntity);
        return infoEntity;
    }

    @PostMapping("/sensitiveUpload")
    public FileDataEntity sensitiveUpload(@RequestParam("file") MultipartFile multipartFile) {
        return fileService.sensitiveUpload(multipartFile, BASE_64_ENCRYPTION);
    }


    @PostMapping("/sensitiveDownload")
    public void sensitiveDownload(@RequestParam("id") String id, HttpServletResponse response) {
        fileService.sensitiveDownload(id, BASE_64_ENCRYPTION, response);
    }


    /**
     * 删除测试接口(真删除)
     *
     * @param id 文件id
     * @return ok/error
     */
    @PostMapping("/delete")
    public String delete(String id) {
        try {
            fileService.deleteFile(id);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "error";
        }
        return "ok";
    }

    @GetMapping(value = "/download")
    public void download(@RequestParam("ids[]") String[] ids, HttpServletResponse response) {

        System.out.println(ids[0]);
        fileService.download(response, ids);
    }


    @GetMapping("/preView")
    public String preView(String fileName) {
        return fileService.filePreView(fileName);
    }
}

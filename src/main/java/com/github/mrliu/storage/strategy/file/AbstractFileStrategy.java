package com.github.mrliu.storage.strategy.file;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.entity.vo.FileDeleteVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author Mr.Liu
 * 文件策略的抽象处理类
 */
@Slf4j
public abstract class AbstractFileStrategy implements FileStrategy {

    /**
     * 上传文件前的固定参数部分设置
     *
     * @param file   文件实体
     * @param isPart 是否是文件分块上传
     * @return 文件固定参数实体
     */
    @Override
    public FileDataEntity upload(MultipartFile file, boolean isPart) {

        try {
            final String originalFilename = file.getOriginalFilename();
            final String extension = FilenameUtils.getExtension(originalFilename);
            //封装到一个固定属性的FileInfoEntity对象
            FileDataEntity fileInfo = FileDataEntity.builder()
                    .fileSize(file.getSize())
                    .originalFileName(originalFilename)
                    .fileExt(extension)
                    .isDelete(false)
                    .build();
            //设置时间参数
            fileInfo.setCrtTime(new Date());
            return uploadFile(fileInfo, file, isPart);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 文件上传抽象方法，具体功能有子类实现
     *
     * @param fileDataEntity 文件对象
     * @param multipartFile  文件实体
     * @param isPart         是否分片
     * @return 文件实体
     * @throws Exception 异常
     */
    public abstract FileDataEntity uploadFile(FileDataEntity fileDataEntity,
                                              MultipartFile multipartFile,
                                              boolean isPart) throws Exception;

    @Override
    public void delete(List<FileDeleteVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        //删除操作是否成功的一个标志位
        for (FileDeleteVo fileDeleteVo : list) {
            try {
                deleteFile(fileDeleteVo);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 文件删除抽象方法，由子类实现
     *
     * @param fileDeleteVo 删除条件
     */
    public abstract void deleteFile(FileDeleteVo fileDeleteVo);
}

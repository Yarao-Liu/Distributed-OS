package com.github.mrliu.storage.strategy.file;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.entity.vo.FileDeleteVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Mr.Liu
 * 最高层文件处理策略接口
 */
public interface FileStrategy {
    /**
     * 上传文件
     * @param multipartFile 文件实体
     * @param isPart 是否是文件分块上传
     * @return FileInfoEntity
     */
    FileDataEntity upload(MultipartFile multipartFile, boolean isPart);

    /**
     * 删除文件
     * @param fileDeleteVos 删除条件
     */
    void delete(List<FileDeleteVo> fileDeleteVos);
}

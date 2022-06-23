package com.github.mrliu.storage.strategy.chunk;

import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;

/**
 * 最高层的文件分片处理策略接口
 *
 * @author Mr.Liu
 */
public interface FileChunkStrategy {
    /**
     * 分片上传文件合并方法
     *
     * @param fileChunkMergeVo 文件合并参数vo
     * @return 文件详细信息
     */
    FileDataEntity chunkMerge(FileChunkMergeVo fileChunkMergeVo);
}

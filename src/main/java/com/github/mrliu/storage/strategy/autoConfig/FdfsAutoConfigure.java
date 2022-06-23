package com.github.mrliu.storage.strategy.autoConfig;

import com.github.mrliu.storage.properties.StorageProperties;
import com.github.mrliu.storage.strategy.chunk.AbstractFileChunkStrategy;
import com.github.mrliu.storage.strategy.file.AbstractFileStrategy;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.github.mrliu.storage.constant.FileStorageType;
import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;
import com.github.mrliu.storage.entity.vo.FileDeleteVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Mr.Liu
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(name = "file.type", havingValue = "FAST_DFS")
public class FdfsAutoConfigure {
    @Resource
    private StorageProperties storageProperties;

    @Service
    public class FastDfsServiceImpl extends AbstractFileStrategy {
        @Resource
        private FastFileStorageClient client;

        @Override
        public FileDataEntity uploadFile(FileDataEntity fileDataEntity, MultipartFile multipartFile, boolean isPart) throws IOException {
            StorePath storePath = client.uploadFile(multipartFile.getInputStream(),
                    multipartFile.getSize(),
                    fileDataEntity.getFileExt(),
                    null);
            fileDataEntity.setGroup(storePath.getGroup());
            final String[] split = storePath.getPath().split("/");
            fileDataEntity.setFileName(split[split.length - 1]);
            fileDataEntity.setUrl(storageProperties.getUriPrefix() + storePath.getFullPath());
            fileDataEntity.setRelativePath(storePath.getPath());
            fileDataEntity.setFileStorageType(FileStorageType.FAST_DFS);
            return fileDataEntity;
        }

        @Override
        public void deleteFile(FileDeleteVo fileDeleteVo) {
            client.deleteFile(fileDeleteVo.getGroup(), fileDeleteVo.getRelativePath());
        }
    }

    @Service
    public class FastDfsChunkSerivceImpl extends AbstractFileChunkStrategy {
        @Resource
        private AppendFileStorageClient storageClient;

        @Override
        protected FileDataEntity merge(List<File> files, String fileName, FileChunkMergeVo fileChunkMergeVo) throws IOException {
            StorePath storePath = null;
            for (int i = 0; i < files.size(); i++) {
                java.io.File file = files.get(i);

                FileInputStream in = FileUtils.openInputStream(file);
                if (i == 0) {
                    storePath = storageClient.uploadAppenderFile(null, in,
                            file.length(), fileChunkMergeVo.getExt());
                } else {
                    storageClient.appendFile(storePath.getGroup(), storePath.getPath(),
                            in, file.length());
                }
            }
            if (storePath == null) {
                log.error("上传失败！");
                return null;
            }
            final String url = storageProperties.getUriPrefix() +
                    storePath.getFullPath();
            return FileDataEntity.builder().url(url)
                    .group(storePath.getGroup())
                    .relativePath(storePath.getPath())
                    .build();
        }
    }
}

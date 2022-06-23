package com.github.mrliu.storage.properties;

import com.github.mrliu.storage.constant.FileStorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件策略配置属性类
 *
 * @author Mr.Liu
 */
@Data
@ConfigurationProperties(prefix = "file")
public class StorageProperties {
    /**
     * 为以下3个值，指定不同的自动化配置
     * qin_iu：七牛oss
     * ali_yun：阿里云oss
     * fast_dfs：本地部署的fastDFS
     */
    private FileStorageType type = FileStorageType.LOCAL;
    /**
     * 文件访问前缀
     */
    private String uriPrefix = "";
    /**
     * 内网通道前缀 主要用于解决某些服务器的无法访问外网ip的问题
     */
    private String innerUriPrefix = "";

    public String getInnerUriPrefix() {
        return innerUriPrefix;
    }

    /**
     * 分片上传时临时目录
     */
    private String storagePath = "folder";


    private Properties local;
    private Properties fdfs;
    private Properties minio;

    @Data
    public static class Properties {
        private String uriPrefix;
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }
}

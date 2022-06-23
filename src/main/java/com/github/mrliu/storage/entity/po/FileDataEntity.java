package com.github.mrliu.storage.entity.po;

import com.github.mrliu.storage.constant.FileStorageType;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author Mr.Liu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FILE_INFO")
public class FileDataEntity extends BaseEntity {
    /**
     * 存储到文件服务器的文件名称
     */
    @Column(name = "FILE_NAME")
    private String fileName;
    /**
     * 文件的后缀名
     */
    @Column(name = "FILE_EXT")
    private String fileExt;
    /**
     * 文件的大小
     */
    @Column(name = "FILE_SIZE")
    private Long fileSize;
    /**
     * 文件的存储路径
     */
    @Column(name = "RELATIVE_PATH")
    private String relativePath;
    /**
     * 桶名称
     */
    @Column(name = "BUCKET_NAME")
    private String bucketName;
    /**
     * 原始文件名
     */
    @Column(name = "ORIGINAL_FILE_NAME")
    private String originalFileName;
    /**
     * fastDfs组名
     */
    @Column(name = "GROUP_NAME")
    private  String group;
    /**
     * 文件是否删除
     */
    @Column(name = "IS_DELETE")
    private Boolean isDelete;
    /**
     * 文件访问链接
     */
    @Column(name = "URL")
    private String url;

    @Column(name = "FILE_SERVER_TYPE")
    private FileStorageType fileStorageType;
    @Column(name = "MD5")
    private String md5;
}

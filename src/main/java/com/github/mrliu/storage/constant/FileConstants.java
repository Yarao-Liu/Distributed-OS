package com.github.mrliu.storage.constant;


import java.io.Serializable;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 数据库常量
 * 文件表
 * </p>
 *
 * @author Mr.Liu
 */
public class FileConstants implements Serializable {

    /**
     * 字段常量
     */
    public static final String DOT = ".";

    public static final String SEPARATOR = "/";

    public static final String TEMP = "temp" + SEPARATOR;

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("/yyyy/MM");

    public static final String UPLOAD_ID = "uploadId";

    public static final String DEFAULT_BUCKET = "images";

    public static final String BASE_64_ENCRYPTION = "base64";

    public static final String UNDER_LINE = "_";

    private FileConstants() {
        super();
    }

    public enum MinioFileStatusEnum {
        /**
         * 待上传
         * 已上传
         * 上传中
         */
        UN_UPLOADED,
        UPLOADED,
        UPLOADING
    }
}

package com.github.mrliu.storage.constant;

/**
 * @author Mr.Liu
 * @description: 常用加密算法枚举类
 * @date 2022/4/22 17:43
 */
public enum EncipherEnum {
    /**
     * base64
     */
    BASE64("Base64"),
    /**
     * md5
     */
    MD5("Md5"),
    /**
     * sha1
     */
    SHA("Sha"),
    /**
     * sha256
     */
    AES("Aes"),
    /**
     * des
     */
    DES("Des");

    private final String value;

    EncipherEnum(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}

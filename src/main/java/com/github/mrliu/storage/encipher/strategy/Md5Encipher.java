package com.github.mrliu.storage.encipher.strategy;

import com.github.mrliu.storage.encipher.EncipherFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author 13439
 */
@Slf4j
public class Md5Encipher extends EncipherFactory {
    @Override
    public String encrypt(MultipartFile file) throws IOException {
        try{
            byte[] uploadBytes = file.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            return new BigInteger(1, digest).toString(16);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new RuntimeException("加密文件失败!");
        }
    }

    @Override
    public byte[] decrypt(String content) {
        throw new RuntimeException("MD5加密算法是单项的，不能进行反向解密");
    }
}

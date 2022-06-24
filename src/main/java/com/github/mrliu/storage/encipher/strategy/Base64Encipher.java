package com.github.mrliu.storage.encipher.strategy;

import com.github.mrliu.storage.encipher.EncipherFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
/**
 * @author Mr.Liu
 * @description: Base64加密工具包
 * @date 2022/4/22 18:10
 */
public class Base64Encipher extends EncipherFactory {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER =Base64.getDecoder();

    /**
     * 将file文件转为base64编码
     * @param file 文件
     * @return base64字符串
     */
    @Override
    public String encrypt(MultipartFile file) throws IOException {
        return byteToString(file.getBytes());
    }
    /**
     * base64将byte[]转为String
     * @param source byte[]
     * @return String
     */
    public static String byteToString(byte[] source) {
        //Base64 Encoded
        return BASE64_ENCODER.encodeToString(source);
    }

    /**
     * 解密算法
     * @param content 待解密内容
     * @return 解密后的字符串
     */
    @Override
    public byte[]  decrypt(String content) {
        return stringToByte(content);
    }



    /**
     * base64将String转为byte[]
     * @param source string
     * @return byte[]
     */
    public static byte[] stringToByte(String source) {
        return BASE64_DECODER.decode(source);
    }
}

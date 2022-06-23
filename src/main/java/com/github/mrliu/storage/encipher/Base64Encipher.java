package com.github.mrliu.storage.encipher;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * base64转码工具类
 * @author Mr.Liu
 */
public class Base64Encipher {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER =Base64.getDecoder();

    /**
     * 将file文件转为base64编码
     * @param multipartFile 文件
     * @return base64字符串
     */
    public static  String fileToBase64(MultipartFile multipartFile) throws IOException{
        return byteToString(multipartFile.getBytes());
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
     * base64将String转为byte[]
     * @param source string
     * @return byte[]
     */
    public static byte[] stringToByte(String source) {
        return BASE64_DECODER.decode(source);
    }
}

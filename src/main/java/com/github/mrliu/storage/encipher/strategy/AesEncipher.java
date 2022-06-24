package com.github.mrliu.storage.encipher.strategy;

import com.github.mrliu.storage.encipher.EncipherFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;

/**
 * @author 13439
 */
@Slf4j
public class AesEncipher extends EncipherFactory {

    private static Cipher encryptCipher=null;

    private static Cipher decryptCipher=null;
    /**
     * 加密的默认密钥
     */
    public static String SECRET_KEY = "1234567890123456";

    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public AesEncipher() {
        init(null);
    }
    /**
     * AesEncipher 使用自定义秘钥加密
     * @param secretKey 对称加密秘钥
     */
    public AesEncipher(String secretKey) {
        init(secretKey);
    }

    private void init(String sKey)  {
        try {
            if (sKey == null) {
                sKey = SECRET_KEY;
            }
            if (SECRET_KEY.length() != 16) {
                throw new RuntimeException("AesEncipher secretKey length must be 16");
            }
            byte[] raw = sKey.getBytes();
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
            //"算法/模式/补码方式"
            encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, sKeySpec);

            decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, sKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("AesEncipher init error", e);
        }
    }
    @Override
    public String encrypt(MultipartFile file) throws IOException {

        return encryptBytes(file.getBytes());
    }
    /**
     * 加密
     */
    public String encryptBytes(byte[] sSrc) {
        try {
            byte[] bytes = encryptCipher.doFinal(sSrc);
            //此处使用BASE64做转码功能，同时能起到2次加密的作用。
            return Base64.getEncoder().encodeToString(bytes);
        }catch (Exception e) {
            log.error("AesEncipher encrypt error", e);
            return null;
        }
    }




    @Override
    public byte[] decrypt(String content) {
        try {
            //先用base64解密
            byte[] bytes = Base64.getDecoder().decode(content);
            return decryptCipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            log.error("AesEncipher decrypt error", e);
            return new byte[0];
        }
    }

    public static void main(String[] args) {
        /*
         * 此处使用AES-128-ECB加密模式，key需要为16位。
         */
        String cKey = "1234567890123456";
        // 需要加密的字串
        String cSrc = "administrator";
        System.out.println(cSrc);
        // 加密
        AesEncipher aesEncipher = new AesEncipher();
        String encrypt = aesEncipher.encryptBytes(cSrc.getBytes());
        System.out.println("加密后的字串是：" + encrypt);

        // 解密
        byte[] decrypt = aesEncipher.decrypt(encrypt);
        System.out.println("解密后的字串是：" + new String(decrypt));

    }
}

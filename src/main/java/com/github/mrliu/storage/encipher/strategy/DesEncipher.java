package com.github.mrliu.storage.encipher.strategy;

import com.github.mrliu.storage.encipher.EncipherFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;

/**
 * @author 13439
 */
public class DesEncipher extends EncipherFactory {

    private final Cipher encryptCipher;

    private final Cipher decryptCipher;

    public static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

    public DesEncipher() {
        Key key = getKey("12345678".getBytes());
        try {
            encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);

            decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            throw new RuntimeException("构造Des加密算法失败", e);
        }
    }

    private Key getKey(byte[] arrTmp) {
        byte[] keyBytes = new byte[8];
        for (int i = 0; i < arrTmp.length && i < keyBytes.length; i++) {
            keyBytes[i] = arrTmp[i];
        }
        return new SecretKeySpec(keyBytes, "DES");
    }
    public String encrypt(String file) throws IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = encryptCipher.doFinal(file.getBytes());
        return new String(Base64.getEncoder().encode(bytes));
    }
    @Override
    public String encrypt(MultipartFile file) throws IOException, IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = encryptCipher.doFinal(file.getBytes());
        return new String(Base64.getEncoder().encode(bytes));
    }

    public String decrypt(byte[] content) throws IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = Base64.getDecoder().decode(content);
        return new String(decryptCipher.doFinal(bytes));
    }
    @Override
    public byte[] decrypt(String content) throws IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = Base64.getDecoder().decode(content);
        return decryptCipher.doFinal(bytes);
    }





    public static void main(String[] args) {
        DesEncipher desEncipher = new DesEncipher();
        try {
            String encrypt = desEncipher.encrypt("administrator123456");
            System.out.println(encrypt);
            byte[] decrypt = desEncipher.decrypt(encrypt);
            String s = new String(decrypt);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




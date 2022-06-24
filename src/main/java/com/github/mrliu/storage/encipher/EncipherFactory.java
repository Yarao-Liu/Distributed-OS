package com.github.mrliu.storage.encipher;

import com.github.mrliu.storage.constant.EncipherEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mr.Liu
 * @description: 加密算法工厂类
 * @date 2022/4/22 17:48
 */
@Slf4j
public abstract class EncipherFactory {

    public static final String CHILD_PACKAGE="strategy.encipher";
    public static final String DOT=".";
    public static final String FILE_SUFFIX="Encipher";

    protected EncipherFactory() {
    }

    /**
     * 创建加密算法
     *
     * @param encipherEnum 加密算法枚举
     * @return 加密算法
     */
    public static EncipherFactory newInstance(EncipherEnum encipherEnum) {
        try {
            String packageName = ClassUtils.getPackageName(EncipherFactory.class.getPackage().getName());
            String className = packageName + DOT + CHILD_PACKAGE + DOT + encipherEnum.getValue()+FILE_SUFFIX;
            System.out.println(className);
            log.info("new instance--> "+className);
            return (EncipherFactory) ClassUtils.forName(className, EncipherFactory.class.getClassLoader()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("很抱歉，名称为" + encipherEnum + "的获取加密算法的具体工厂还没创建！", e);
        }
    }

    /**
     * 加密
     *
     * @param file 待加密文件
     * @return 加密后的内容
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws IllegalBlockSizeException 块大小不合法
     * @throws BadPaddingException 填充错误
     */
    public abstract String encrypt(MultipartFile file) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException;

    /**
     * 解密
     * @param content 待解密内容
     * @return 解密后的内容
     * @throws IOException IO异常
     * @throws IllegalBlockSizeException 块大小不合法
     * @throws BadPaddingException 填充错误
     */
    public abstract byte[] decrypt(String content) throws IllegalBlockSizeException, BadPaddingException, IOException;

}

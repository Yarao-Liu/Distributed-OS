package com.github.mrliu.storage.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件锁工具类
 * @author Mr.Liu
 */
@Component
public class FileLock {
    private static final Map<String, Lock> LOCKS = new HashMap<>();

    /**
     * 获取锁
     *
     */
    public static synchronized Lock getLock(String key) {
        if (LOCKS.containsKey(key)) {
            return LOCKS.get(key);
        } else {
            Lock one = new ReentrantLock();
            LOCKS.put(key, one);
            return one;
        }
    }

    /**
     * 删除锁
     *
     */
    public static synchronized void removeLock(String key) {
        LOCKS.remove(key);
    }
}

package com.github.mrliu.storage;

import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mr.Liu
 */
@MapperScan(basePackages = "com.github.mrliu.storage.mapper")
@SpringBootApplication
public class FastStorageApp {

    public static void main(String[] args) {
        SpringApplication.run(FastStorageApp.class, args);
    }

}

package com.github.mrliu.storage;

import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mr.Liu
 */
@MapperScan(basePackages = "com.github.mrliu.storage.mapper")
@SpringBootApplication
public class FileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServerApplication.class, args);
    }

}

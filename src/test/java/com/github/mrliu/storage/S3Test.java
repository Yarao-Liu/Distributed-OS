package com.github.mrliu.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Mr.Liu
 * @description: S3的部分功能测试
 * @date 2022/3/16 8:36
 */
public class S3Test {
    @Test
    public  void main() throws Exception {
        String fileName = "minio.exe";
        final MinioClient minioClient = MinioClient.builder().endpoint("http://localhost:9000").credentials("minioadmin", "minioadmin").build();
        final String preUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket("images").object(fileName).method(Method.PUT).build());
        System.out.println("获取到的preUrl:" + preUrl);
        final URL url = new URL(preUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        //判断是面向字节流编程还是字符流编程
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        final File file = new File("C:\\Users\\Mr.Liu\\Desktop\\" + fileName);
        final DataInputStream in = new DataInputStream(new FileInputStream(file));
        IOUtils.copy(in, out);
        out.close();
        connection.getResponseCode();
        System.out.println("HTTP response code: " + connection.getResponseCode());

        // Check to make sure that the object was uploaded successfully.
        final GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder().bucket("images").object(fileName).build());
        System.out.println("Object: " + object.object() + " created in bucket: " + object.bucket());
    }
}

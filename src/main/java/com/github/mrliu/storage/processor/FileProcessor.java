package com.github.mrliu.storage.processor;

import com.github.mrliu.storage.properties.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Mr.Liu
 */
@Slf4j
@Component
public class FileProcessor {
    @Resource
    private DownloadProcessor downloadProcessor;
    @Resource
    private StorageProperties properties;

    /**
     * 文件多功能下载
     * @param response 响应实体
     * @param map 文件信息
     * @throws IOException IO异常
     */
    public void downloadFile(HttpServletResponse response, Map<String, String> map) throws IOException {
        ArrayList<String> names = new ArrayList<>(map.keySet());
        // 压缩文件的压缩文件输出流
        if (names.size() > 1) {
            zipFile(response, map, names);
        } else {
            //正常下载单个文件
            final ServletOutputStream outputStream = response.getOutputStream();
            byte[] resource = getFileResource(map.get(names.get(0)));
            IOUtils.write(resource, outputStream);
        }
    }

    private void zipFile(HttpServletResponse response, Map<String, String> map, ArrayList<String> names) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
        for (final String name : names) {
            final String url = map.get(name);
            final byte[] resource = getFileResource(url);
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(resource);
        }
        zipOutputStream.close();
    }

    public byte[] getFileResource(String url) {
        try {
            byte[] resource = new byte[0];
            if (properties.getFdfs() != null) {
                resource = downloadProcessor.getFdfsResource(url);
            } else if (properties.getMinio() != null) {
                resource = downloadProcessor.getMinioResource(url);
            }
            return resource;
        } catch (Exception e) {
            log.error(e.getMessage());
            return "null".getBytes();
        }
    }

    /**
     * 构建一个MultipartFile对象
     *
     * @param file     本地file文件
     * @param fileName 真实的文件名称
     * @return 构建一个MultipartFile对象对象
     */
    public MultipartFile file2MultipartFile(File file, String fileName) {
        DiskFileItem fileItem = (DiskFileItem) new DiskFileItemFactory().createItem("file",
                MediaType.ALL_VALUE, true, fileName);
        try (InputStream input = new FileInputStream(file); OutputStream os = fileItem.getOutputStream()) {
            IOUtils.copy(input, os);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file: " + e, e);
        }
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * 获取小文件的MD5值
     * @param file 上传的小文件
     * @return MD5
     */
    public String getFileMd5(MultipartFile file) {

        try {
            byte[] uploadBytes = file.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            return new BigInteger(1, digest).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "null";
    }
}

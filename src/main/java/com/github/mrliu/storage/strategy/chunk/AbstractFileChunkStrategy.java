package com.github.mrliu.storage.strategy.chunk;

import com.github.mrliu.storage.properties.StorageProperties;
import com.github.mrliu.storage.constant.FileConstants;
import com.github.mrliu.storage.entity.po.FileDataEntity;
import com.github.mrliu.storage.service.FileService;
import com.github.mrliu.storage.utils.FileLock;
import com.github.mrliu.storage.entity.vo.FileChunkMergeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static com.github.mrliu.storage.constant.FileConstants.DOT;
import static com.github.mrliu.storage.constant.FileConstants.SEPARATOR;

/**
 * 文件分片处理抽象类
 *
 * @author Mr.Liu
 */
@Slf4j
public abstract class AbstractFileChunkStrategy implements FileChunkStrategy {

    @Resource
    private FileService fileService;

    @Resource
    private StorageProperties storageProperties;

    /**
     * 分片合并处理主要流程
     *
     * @param fileChunkMergeVo 文件合并参数vo
     * @return 文件详情
     */
    @Override
    public FileDataEntity chunkMerge(FileChunkMergeVo fileChunkMergeVo) {
        //定义文件合并后的文件名称
        String fileName = fileChunkMergeVo.getName() + DOT + fileChunkMergeVo.getExt();
        //分片合并
        FileDataEntity result = this.chunkMerge(fileChunkMergeVo, fileName);
        //合并后文件信息保存到数据库
        if (result != null) {
            //合并成功
            //文件信息保存到数据库
            //设置文件对象的属性，保存到数据库
            final String id = UUID.randomUUID().toString();
            result.setId(id);
            result.setOriginalFileName(fileChunkMergeVo.getOriginalFileName());
            result.setFileStorageType(storageProperties.getType());
            result.setFileSize(fileChunkMergeVo.getSize());
            result.setMd5(fileChunkMergeVo.getMd5());
            result.setFileName(fileName);
            result.setFileExt(fileChunkMergeVo.getExt());
            result.setBucketName(storageProperties.getMinio().getBucketName());
            result.setCrtTime(new Date());
            result.setIsDelete(false);
            //合并文件后的信息保存到数据库
            fileService.saveFileInfo(result);
            return result;
        }
        //合并失败
        return null;
    }

    /**
     * 分片合并
     *
     * @param fileChunkMergeVo 分配信息vo
     * @param fileName         文件名
     * @return 文件详情
     */
    public FileDataEntity chunkMerge(FileChunkMergeVo fileChunkMergeVo, String fileName) {
        //获得分片文件的存储路径,以及写文件的临时目录
        String storagePath = storageProperties.getStoragePath();
        String abstractFolder = LocalDate.now().format(FileConstants.DTF);
        String uploadFolder = storagePath + abstractFolder;

        //如果文件分块的返回的总是是null,则是使用分块接口上传了小文件
        Integer chunks = fileChunkMergeVo.getChunks();
        if (chunks == null) {
            return null;
        }

        //根据指定目录获取文件数量
        String folder = fileChunkMergeVo.getName();
        String relativePath = uploadFolder + SEPARATOR + folder;
        int chunksNum = this.getChunksNum(relativePath);

        //检查分片数量是否足够
        if (chunks == chunksNum) {
            //数量足够,可以合并
            Lock lock = FileLock.getLock(folder);
            lock.lock();
            try {
                //获取所有的分片文件,合并前需要排序,调用子类分片合并方法实现分片合并
                List<File> files = getChunks(relativePath);
                files.sort(Comparator.comparingInt(f -> Integer.parseInt(f.getName())));
                FileDataEntity result = this.merge(files, fileName, fileChunkMergeVo);
                //清理文件
                this.cleanSpace(folder, uploadFolder);
                return result;
            } catch (Exception e) {
                log.error("分片合并失败");
                return null;
            } finally {
                //释放锁,并清理锁对象
                lock.unlock();
                FileLock.removeLock(folder);
            }
        }
        log.error("分片数量不足，无法进行数量合并");
        //暂时不做异常抛出
        return null;
    }

    /**
     * 清理本地文件
     *
     * @param folder 文件夹
     * @param path   文件名
     */
    public void cleanSpace(String folder, String path) {
        log.info("目标文件夹:" + path + folder);
        //删除存放分片文件的目录
        File chunkFolder = new File(path + folder);
        FileUtils.deleteQuietly(chunkFolder);
        //删除.tmp文件
        File tmpFile = new File(path + folder + DOT + "tmp");
        FileUtils.deleteQuietly(tmpFile);
    }

    /**
     * 获取指定文件夹下文件数量
     *
     * @param path 临时存储路径
     * @return 文件数量
     */
    public int getChunksNum(String path) {
        log.info("指定文件夹下文件数量:" + path);
        File folder = new File(path);
        File[] files = folder.listFiles((file) -> !file.isDirectory());
        return files == null ? 0 : files.length;
    }

    /**
     * 获取指定目录的文件
     *
     * @param path 路径
     * @return 文件列表
     */
    public List<File> getChunks(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles((file) -> !file.isDirectory());
        if (files == null) {
            log.info("文件太小无须分片.");
            return null;
        }
        return new ArrayList<>(Arrays.asList(files));
    }

    /**
     * 分片合并抽象方法
     *
     * @param files            文件分片列表
     * @param fileName         文件分片名称
     * @param fileChunkMergeVo 文件分片信息
     * @return 文件详情信息
     * @exception Exception 异常描述
     */
    protected abstract FileDataEntity merge(List<File> files,
                                            String fileName,
                                            FileChunkMergeVo fileChunkMergeVo) throws Exception;
}

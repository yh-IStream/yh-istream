package com.istream.file.storage;

import com.istream.file.enums.StorageType;

import java.io.InputStream;

/**
 * 文件存储服务接口
 *
 * <p>抽象存储操作</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public interface FileStorageService {

    /**
     * 存储文件
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param mimeType    MIME 类型
     * @return 存储后的相对路径
     */
    String store(InputStream inputStream, String fileName, String mimeType);

    /**
     * 加载文件
     *
     * @param filePath 文件相对路径
     * @return 文件输入流
     */
    InputStream load(String filePath);

    /**
     * 删除文件
     *
     * @param filePath 文件相对路径
     */
    void delete(String filePath);

    /**
     * 获取文件访问 URL
     *
     * @param filePath 文件相对路径
     * @return 访问 URL
     */
    String getAccessUrl(String filePath);

    /**
     * 获取当前存储类型
     *
     * @return 存储类型枚举
     */
    StorageType getStorageType();
}
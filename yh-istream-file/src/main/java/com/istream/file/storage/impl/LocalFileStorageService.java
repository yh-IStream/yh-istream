package com.istream.file.storage.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.istream.common.exception.BusinessException;
import com.istream.common.enums.ResultCode;
import com.istream.file.config.OssProperties;
import com.istream.file.enums.StorageType;
import com.istream.file.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final OssProperties ossProperties;

    @Override
    public String store(InputStream inputStream, String fileName, String mimeType) {
        try {
            String dateDir = DateUtil.today().replace("-", "/");
            Path dir = Paths.get(ossProperties.getLocal().getUploadPath(), dateDir);
            FileUtil.mkdir(dir.toString());

            String ext = FileUtil.extName(fileName);
            String storedName = IdUtil.fastSimpleUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path targetPath = dir.resolve(storedName);
            FileUtil.writeFromStream(inputStream, targetPath.toFile());

            String relativePath = dateDir + "/" + storedName;
            log.info("文件存储成功: {}", relativePath);
            return relativePath;
        } catch (Exception e) {
            log.error("文件存储失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public InputStream load(String filePath) {
        Path fullPath = resolveAndValidate(filePath);
        try {
            return new FileInputStream(fullPath.toFile());
        } catch (FileNotFoundException e) {
            log.error("文件不存在: {}", filePath, e);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void delete(String filePath) {
        Path fullPath = resolveAndValidate(filePath);
        FileUtil.del(fullPath);
        log.info("文件删除成功: {}", filePath);
    }

    @Override
    public String getAccessUrl(String filePath) {
        return ossProperties.getLocal().getUrlPrefix() + "/" + filePath;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    /**
     * 解析并校验文件路径
     *
     * <p>规范化路径后校验是否在上传根目录内，拒绝 {@code ../} 等遍历序列。</p>
     *
     * @param filePath 相对文件路径（来自数据库）
     * @return 规范化后的绝对路径
     * @throws BusinessException 路径遍历时抛出
     */
    private Path resolveAndValidate(String filePath) {
        Path basePath = Paths.get(ossProperties.getLocal().getUploadPath()).toAbsolutePath().normalize();
        Path fullPath = basePath.resolve(filePath).normalize();
        if (!fullPath.startsWith(basePath)) {
            log.error("路径遍历攻击检测: filePath={}, resolved={}", filePath, fullPath);
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "非法文件路径");
        }
        return fullPath;
    }
}
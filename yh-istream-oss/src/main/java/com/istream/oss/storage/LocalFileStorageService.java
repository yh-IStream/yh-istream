package com.istream.oss.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.istream.common.exception.BusinessException;
import com.istream.common.enums.ResultCode;
import com.istream.oss.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final OssProperties ossProperties;

    @Override
    public String store(InputStream inputStream, String fileName, String mimeType) {
        try {
            String dateDir = cn.hutool.core.date.DateUtil.today().replace("-", "/");
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
        try {
            Path fullPath = Paths.get(ossProperties.getLocal().getUploadPath(), filePath);
            return new FileInputStream(fullPath.toFile());
        } catch (FileNotFoundException e) {
            log.error("文件不存在: {}", filePath, e);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void delete(String filePath) {
        Path fullPath = Paths.get(ossProperties.getLocal().getUploadPath(), filePath);
        FileUtil.del(fullPath);
        log.info("文件删除成功: {}", filePath);
    }

    @Override
    public String getAccessUrl(String filePath) {
        return ossProperties.getLocal().getUrlPrefix() + "/" + filePath;
    }
}
package com.istream.file.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.istream.common.exception.BusinessException;
import com.istream.common.enums.ResultCode;
import com.istream.file.model.query.file.SysFileQuery;
import com.istream.file.entity.SysFile;
import com.istream.file.enums.StorageType;
import com.istream.file.mapper.SysFileMapper;
import com.istream.file.service.SysFileService;
import com.istream.file.storage.FileStorageService;
import com.istream.framework.util.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件管理服务实现
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    private final FileStorageService fileStorageService;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/csv", "text/html",
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed",
            "application/json", "application/xml"
    );

    private static final Map<String, Set<String>> ALLOWED_MAGIC_NUMBERS = Map.of(
            "image/jpeg", Set.of("FFD8FF"),
            "image/png", Set.of("89504E47"),
            "image/gif", Set.of("47494638"),
            "image/bmp", Set.of("424D"),
            "image/webp", Set.of("52494646"),
            "application/pdf", Set.of("25504446"),
            "application/zip", Set.of("504B0304", "504B0506", "504B0708"),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("504B0304"),
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of("504B0304"),
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of("504B0304")
    );

    private static final int MAGIC_BYTES_LENGTH = 4;

    @Override
    @Transactional(readOnly = true)
    public IPage<SysFile> page(SysFileQuery query) {
        Page<SysFile> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysFile>()
                .like(query.getOriginalName() != null && !query.getOriginalName().isEmpty(),
                        SysFile::getOriginalName, SqlUtils.escapeLike(query.getOriginalName()))
                .eq(query.getFileExt() != null && !query.getFileExt().isEmpty(),
                        SysFile::getFileExt, query.getFileExt())
                .orderByDesc(SysFile::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile upload(MultipartFile file, String module) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName).toLowerCase();
        String mimeType = file.getContentType();

        String filePath;
        try (InputStream inputStream = file.getInputStream()) {
            filePath = fileStorageService.store(inputStream, originalName, mimeType);
        } catch (Exception e) {
            log.error("文件上传失败: {}", originalName, e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }

        SysFile sysFile = new SysFile();
        sysFile.setFileName(FileUtil.getName(filePath));
        sysFile.setOriginalName(originalName);
        sysFile.setFilePath(filePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setMimeType(mimeType);
        sysFile.setFileExt(ext);
        sysFile.setStorageType(StorageType.LOCAL);
        sysFile.setStorageUrl(fileStorageService.getAccessUrl(filePath));
        sysFile.setModule(module);

        save(sysFile);
        return sysFile;
    }

    @Override
    public InputStream getFileStream(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return fileStorageService.load(sysFile.getFilePath());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        removeById(id);
        try {
            fileStorageService.delete(sysFile.getFilePath());
        } catch (Exception e) {
            log.error("磁盘文件删除失败，但数据库记录已删除: {}", sysFile.getFilePath(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> list) {
        List<? extends Serializable> ids = list.stream()
                .map(id -> id instanceof Serializable ? (Serializable) id : Long.valueOf(id.toString()))
                .toList();
        List<SysFile> files = listByIds(ids);
        boolean result = super.removeByIds(list);
        if (result) {
            for (SysFile file : files) {
                try {
                    fileStorageService.delete(file.getFilePath());
                } catch (Exception e) {
                    log.error("磁盘文件删除失败，但数据库记录已删除: {}", file.getFilePath(), e);
                }
            }
        }
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "文件不能为空");
        }
        long maxBytes = DataSize.parse(maxFileSize).toBytes();
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "文件大小超过限制（最大" + maxFileSize + "）");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "不支持的文件类型：" + contentType);
        }
        if (!validateMagicNumber(file, contentType)) {
            throw new BusinessException(ResultCode.PARAM_VALID_ERROR, "文件类型校验失败，文件内容与声明的类型不一致");
        }
    }

    private boolean validateMagicNumber(MultipartFile file, String contentType) {
        Set<String> expectedSignatures = ALLOWED_MAGIC_NUMBERS.get(contentType);
        if (expectedSignatures == null) {
            return true;
        }
        try {
            byte[] header = new byte[MAGIC_BYTES_LENGTH];
            try (InputStream is = file.getInputStream()) {
                int read = is.read(header, 0, MAGIC_BYTES_LENGTH);
                if (read < MAGIC_BYTES_LENGTH) {
                    return false;
                }
            }
            String hex = HexFormat.of().withUpperCase().formatHex(header);
            for (String sig : expectedSignatures) {
                if (hex.startsWith(sig)) {
                    return true;
                }
            }
            log.warn("File magic number mismatch: claimed={}, actual={}", contentType, hex);
            return false;
        } catch (Exception e) {
            log.warn("Failed to read file magic number", e);
            return false;
        }
    }

}
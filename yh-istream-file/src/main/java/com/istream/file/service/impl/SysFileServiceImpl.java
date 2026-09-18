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
import java.util.List;
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

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "md", "json", "xml", "yml", "yaml",
            "zip", "rar", "7z", "tar", "gz",
            "mp3", "mp4", "avi", "mov", "wmv"
    );

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

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
    @Transactional(readOnly = true)
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
        @SuppressWarnings("unchecked")
        List<SysFile> files = listByIds((Collection<? extends Serializable>) list);
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
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        if (file.getSize() > DataSize.parse(maxFileSize).toBytes()) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEED);
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        String ext = FileUtil.extName(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }
    }
}
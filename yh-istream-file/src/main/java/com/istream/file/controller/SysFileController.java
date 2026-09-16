package com.istream.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.BusinessType;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.R;
import com.istream.file.model.dto.SysFileDTO;
import com.istream.file.model.query.SysFileQuery;
import com.istream.file.converter.SysFileConverter;
import com.istream.file.entity.SysFile;
import com.istream.file.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.util.unit.DataSize;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService sysFileService;
    private final SysFileConverter sysFileConverter;

    /** 最大文件大小，默认 10MB，与 spring.servlet.multipart.max-file-size 保持一致 */
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    /** 允许上传的文件 MIME 类型 */
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

    /** 允许的文件头部魔数签名（十六进制），用于校验文件真实类型 */
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

    @OperLog(title = "文件管理", businessType = BusinessType.INSERT)
    @RateLimit(key = "file:upload", rate = 3, timeout = 0)
    @Operation(summary = "上传文件")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public R<SysFile> upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(defaultValue = "common") String module) {
        if (file.isEmpty()) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "文件不能为空");
        }
        long maxBytes = DataSize.parse(maxFileSize).toBytes();
        if (file.getSize() > maxBytes) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "文件大小超过限制（最大" + maxFileSize + "）");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "不支持的文件类型：" + contentType);
        }

        if (!validateMagicNumber(file, contentType)) {
            return R.fail(ResultCode.PARAM_VALID_ERROR,
                    "文件类型校验失败，文件内容与声明的类型不一致");
        }

        return R.ok(sysFileService.upload(file, module));
    }

    @Operation(summary = "下载文件")
    @SaCheckPermission("system:file:download")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        SysFile sysFile = sysFileService.getById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        try (InputStream inputStream = sysFileService.getFileStream(id)) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String encodedName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedName);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(sysFile.getFileSize()));
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件下载失败: id={}", id, e);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_ERROR);
        }
    }

    @Operation(summary = "预览文件（图片等）")
    @SaCheckPermission("system:file:download")
    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) {
        SysFile sysFile = sysFileService.getById(id);
        if (sysFile == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        try (InputStream inputStream = sysFileService.getFileStream(id)) {
            String mimeType = sysFile.getMimeType() != null
                    ? sysFile.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            response.setContentType(mimeType);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(sysFile.getFileSize()));

            if (mimeType.startsWith("image/")) {
                String encodedName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedName);
            }

            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件预览失败: id={}", id, e);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_ERROR);
        }
    }

    @OperLog(title = "文件管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除文件")
    @SaCheckPermission("system:file:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysFileService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "分页查询文件列表")
    @SaCheckPermission("system:file:list")
    @GetMapping("/list")
    public R<IPage<SysFileDTO>> list(SysFileQuery query) {
        IPage<SysFile> page = sysFileService.page(query);
        IPage<SysFileDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(sysFileConverter::toDto)
                .toList());
        return R.ok(dtoPage);
    }

    /**
     * 校验文件头部魔数签名，防止文件类型伪装攻击
     */
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
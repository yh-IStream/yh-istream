package com.istream.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.BusinessType;
import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.R;
import com.istream.framework.util.PageUtils;
import com.istream.file.model.dto.file.SysFileDTO;
import com.istream.file.model.query.file.SysFileQuery;
import com.istream.file.converter.SysFileConverter;
import com.istream.file.entity.SysFile;
import com.istream.file.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @OperLog(title = "文件管理", businessType = BusinessType.INSERT)
    @RateLimit(key = "file:upload", rate = 3, timeout = 0)
    @Operation(summary = "上传文件")
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public R<SysFileDTO> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam(defaultValue = "common") String module) {
        return R.ok(sysFileConverter.toDto(sysFileService.upload(file, module)));
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
        return R.ok(PageUtils.toDtoPage(page, sysFileConverter::toDto));
    }
}
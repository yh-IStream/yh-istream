package com.istream.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.generator.model.ColumnInfo;
import com.istream.generator.model.GenRequest;
import com.istream.generator.model.TableInfo;
import com.istream.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器 Controller
 *
 * @author isteam
 * @since 2026-08-17
 */
@Tag(name = "代码生成器")
@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    @Operation(summary = "查询所有表")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/tables")
    public R<List<TableInfo>> listTables() {
        return R.ok(generatorService.listTables());
    }

    @Operation(summary = "查询表列信息")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/columns/{tableName}")
    public R<List<ColumnInfo>> listColumns(@PathVariable String tableName) {
        return R.ok(generatorService.listColumns(tableName));
    }

    @OperLog(title = "代码生成器", businessType = BusinessType.OTHER)
    @RateLimit(key = "generator:preview", rate = 10, timeout = 0)
    @Operation(summary = "预览生成代码")
    @SaCheckPermission("generator:code:preview")
    @PostMapping("/preview/{tableName}")
    public R<Map<String, String>> preview(@PathVariable String tableName,
                                          @RequestBody GenRequest request) {
        return R.ok(generatorService.preview(tableName, request));
    }

    @OperLog(title = "代码生成器", businessType = BusinessType.OTHER)
    @RateLimit(key = "generator:preview", rate = 10, timeout = 0)
    @Operation(summary = "批量预览生成代码")
    @SaCheckPermission("generator:code:preview")
    @PostMapping("/batch-preview")
    public R<Map<String, Map<String, String>>> batchPreview(@RequestBody GenRequest request) {
        return R.ok(generatorService.batchPreview(request));
    }

    @OperLog(title = "代码生成器", businessType = BusinessType.OTHER)
    @RateLimit(key = "generator:download", rate = 5, timeout = 0)
    @Operation(summary = "生成并下载代码")
    @SaCheckPermission("generator:code:download")
    @PostMapping("/download/{tableName}")
    public void download(@PathVariable String tableName,
                         @RequestBody GenRequest request,
                         HttpServletResponse response) throws IOException {
        Map<String, String> codeMap = generatorService.preview(tableName, request);

        byte[] zipBytes = buildZip(codeMap);
        String fileName = tableName + ".zip";

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                        .replace("+", "%20"));
        response.setContentLength(zipBytes.length);
        response.getOutputStream().write(zipBytes);
    }

    @OperLog(title = "代码生成器", businessType = BusinessType.OTHER)
    @RateLimit(key = "generator:download", rate = 5, timeout = 0)
    @Operation(summary = "批量生成并下载代码")
    @SaCheckPermission("generator:code:download")
    @PostMapping("/batch-download")
    public void batchDownload(@RequestBody GenRequest request,
                              HttpServletResponse response) throws IOException {
        Map<String, Map<String, String>> allCodeMap = generatorService.batchPreview(request);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Map.Entry<String, Map<String, String>> tableEntry : allCodeMap.entrySet()) {
                String tableName = tableEntry.getKey();
                for (Map.Entry<String, String> codeEntry : tableEntry.getValue().entrySet()) {
                    String path = tableName + "/" + codeEntry.getKey();
                    zos.putNextEntry(new ZipEntry(path));
                    zos.write(codeEntry.getValue().getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                }
            }
        }

        String fileName = "generator-output.zip";
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                        .replace("+", "%20"));
        response.setContentLength(bos.size());
        response.getOutputStream().write(bos.toByteArray());
    }

    private byte[] buildZip(Map<String, String> codeMap) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Map.Entry<String, String> entry : codeMap.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return bos.toByteArray();
    }
}
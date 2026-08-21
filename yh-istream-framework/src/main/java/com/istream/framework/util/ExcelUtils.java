package com.istream.framework.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EasyExcel 通用导出工具类
 *
 * <p>基于 EasyExcel 4.x API，提供统一的 Excel 导出能力。
 * 实体类字段使用 {@code @ExcelProperty("列名")} 标注即可自动映射。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   List<UserExportDTO> data = userService.listForExport(query);
 *   ExcelUtils.export(response, "用户列表", UserExportDTO.class, data);
 * }</pre>
 *
 * @author isteam
 * @since 2026-08-17
 */
@Slf4j
public final class ExcelUtils {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    private ExcelUtils() {
    }

    /**
     * Web 导出：将数据写入 HttpServletResponse，触发浏览器下载。
     *
     * @param response HttpServletResponse
     * @param fileName 文件名（不含扩展名，支持中文）
     * @param head     表头对应的实体类（字段需标注 {@code @ExcelProperty}）
     * @param data     数据列表，空列表将导出仅含表头的空文件
     * @param <T>      数据类型
     */
    public static <T> void export(HttpServletResponse response, String fileName, Class<T> head, List<T> data) {
        export(response, fileName, DEFAULT_SHEET_NAME, head, data);
    }

    /**
     * Web 导出：支持自定义 Sheet 名称。
     *
     * @param response  HttpServletResponse
     * @param fileName  文件名（不含扩展名，支持中文）
     * @param sheetName Sheet 名称
     * @param head      表头对应的实体类
     * @param data      数据列表
     * @param <T>       数据类型
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> head, List<T> data) {
        try {
            setResponseHeader(response, fileName);
            EasyExcel.write(response.getOutputStream(), head)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("Excel 导出失败: fileName={}", fileName, e);
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    /**
     * 写入 OutputStream（适用于非 Web 场景，如生成文件到磁盘或 ByteArrayOutputStream）。
     *
     * @param outputStream 输出流
     * @param head         表头对应的实体类
     * @param data         数据列表
     * @param <T>          数据类型
     */
    public static <T> void write(OutputStream outputStream, Class<T> head, List<T> data) {
        write(outputStream, DEFAULT_SHEET_NAME, head, data);
    }

    /**
     * 写入 OutputStream，自定义 Sheet 名称。
     *
     * @param outputStream 输出流
     * @param sheetName    Sheet 名称
     * @param head         表头对应的实体类
     * @param data         数据列表
     * @param <T>          数据类型
     */
    public static <T> void write(OutputStream outputStream, String sheetName, Class<T> head, List<T> data) {
        EasyExcel.write(outputStream, head)
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * 获取 {@link ExcelWriterBuilder}，用于需要自定义写处理器（如单元格样式、下拉框等）的高级场景。
     *
     * <p>使用示例：</p>
     * <pre>{@code
     *   ExcelUtils.builder(response.getOutputStream(), DemoDTO.class)
     *           .registerWriteHandler(new CustomCellWriteHandler())
     *           .sheet("数据")
     *           .doWrite(data);
     * }</pre>
     *
     * @param outputStream 输出流
     * @param head         表头对应的实体类
     * @param <T>          数据类型
     * @return ExcelWriterBuilder 实例
     */
    public static <T> ExcelWriterBuilder builder(OutputStream outputStream, Class<T> head) {
        return EasyExcel.write(outputStream, head);
    }

    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx");
    }
}
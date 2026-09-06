package com.istream.framework.util;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * EasyExcel Web 导出工具类
 *
 * <p>将数据列表以 Excel 格式写入 HttpServletResponse，触发浏览器下载。
 * 实体类字段使用 {@code @ExcelProperty("列名")} 标注即可自动映射。</p>
 *
 * <p>支持两种导出模式：
 * <ul>
 *   <li>{@link #export} - 一次性全量导出（数据量小时使用）</li>
 *   <li>{@link #exportByPage} - 分页流式导出（数据量大时使用，避免 OOM）</li>
 * </ul>
 * </p>
 *
 * @author istream
 * @since 2026-08-17
 */
public final class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    /**
     * 一次性全量导出
     *
     * @param response  HTTP 响应
     * @param fileName  文件名（不含扩展名）
     * @param sheetName 工作表名
     * @param clazz     数据类型
     * @param data      数据列表
     * @param <T>       数据泛型
     * @throws IOException 写入异常
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) throws IOException {
        setExcelResponseHeaders(response, fileName);
        EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
    }

    /**
     * 分页流式导出（避免大数据量 OOM）
     *
     * <p>通过分页查询函数逐页加载数据并流式写入 Excel，每页数据写完即释放，
     * 内存占用仅为一页数据量，适合百万级数据导出。</p>
     *
     * @param response     HTTP 响应
     * @param fileName     文件名（不含扩展名）
     * @param sheetName    工作表名
     * @param clazz        数据类型
     * @param pageLoader   分页加载函数，接收页码（从1开始），返回分页结果
     * @param <T>          数据泛型
     * @throws IOException 写入异常
     */
    public static <T> void exportByPage(HttpServletResponse response, String fileName, String sheetName,
                                        Class<T> clazz, Function<Long, IPage<T>> pageLoader) throws IOException {
        setExcelResponseHeaders(response, fileName);

        var excelWriter = EasyExcel.write(response.getOutputStream(), clazz).build();
        var writeSheet = EasyExcel.writerSheet(sheetName).build();

        try {
            long pageNum = 1;
            while (true) {
                IPage<T> page = pageLoader.apply(pageNum);
                List<T> records = page.getRecords();
                if (records == null || records.isEmpty()) {
                    break;
                }
                excelWriter.write(records, writeSheet);
                if (page.getCurrent() >= page.getPages()) {
                    break;
                }
                pageNum++;
            }
        } finally {
            excelWriter.finish();
        }
    }

    /**
     * 设置 Excel 下载响应头
     */
    private static void setExcelResponseHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded + ".xlsx");
    }
}
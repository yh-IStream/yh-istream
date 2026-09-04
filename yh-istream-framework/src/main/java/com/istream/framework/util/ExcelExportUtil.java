package com.istream.framework.util;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EasyExcel Web 导出工具类
 *
 * <p>将数据列表以 Excel 格式写入 HttpServletResponse，触发浏览器下载。
 * 实体类字段使用 {@code @ExcelProperty("列名")} 标注即可自动映射。</p>
 */
public final class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded + ".xlsx");
        EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
    }
}
package com.istream.framework.util;

/**
 * SQL 工具类
 *
 * <p>提供 SQL 查询相关的辅助方法，如 LIKE 模式转义等。</p>
 *
 * @author istream
 * @since 2026-09-11
 */
public final class SqlUtils {

    private SqlUtils() {
    }

    /**
     * 对 LIKE 查询的用户输入进行转义，防止通配符 % 和 _ 被解析
     *
     * <p>转义顺序：先转义反斜杠本身，再转义 % 和 _，
     * 配合 SQL 中 {@code ESCAPE '\\'} 子句使用。</p>
     *
     * @param value 原始用户输入
     * @return 转义后的字符串，若输入为 null 则返回 null
     */
    public static String escapeLike(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
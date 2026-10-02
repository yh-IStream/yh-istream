package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作业务类型枚举
 *
 * <p>用于 {@code @OperLog} 注解标记操作类型，
 * 操作日志表按此分类记录。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Getter
@AllArgsConstructor
public enum BusinessType {

    OTHER(0, "其他"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    QUERY(4, "查询"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    LOGIN(7, "登录"),
    LOGOUT(8, "登出");

    private final int code;
    private final String desc;
}
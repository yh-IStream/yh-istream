package com.istream.common.enums;

import lombok.Getter;

@Getter
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

    BusinessType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
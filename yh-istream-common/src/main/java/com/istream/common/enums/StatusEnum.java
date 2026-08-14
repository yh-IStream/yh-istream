package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    /** 启用 */
    ENABLED(0, "启用"),

    /** 禁用 */
    DISABLED(1, "禁用");

    private final Integer code;
    private final String desc;
}
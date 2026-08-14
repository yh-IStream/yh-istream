package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    /** 目录 */
    DIR("M", "目录"),

    /** 菜单 */
    MENU("C", "菜单"),

    /** 按钮 */
    BUTTON("F", "按钮");

    private final String code;
    private final String desc;
}
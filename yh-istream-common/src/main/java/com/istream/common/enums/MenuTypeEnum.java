package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * <p>定义菜单的三种类型：目录（M）、菜单（C）、按钮/权限（F）。</p>
 *
 * @author istream
 * @since 2026-08-17
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
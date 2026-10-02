package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * <p>定义启用/禁用两种状态，用于用户、角色、部门等实体的状态标记。</p>
 *
 * @author istream
 * @since 2026-08-17
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

    /**
     * 判断给定的状态码是否非法
     *
     * @param code 状态码
     * @return 是否非法
     * @since 2026-09-09
     */
    public static boolean isInvalidCode(Integer code) {
        return code == null || (!code.equals(ENABLED.code) && !code.equals(DISABLED.code));
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若 code 为 null 或不匹配则返回 null
     * @since 2026-09-27
     */
    public static StatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        if (ENABLED.code.equals(code)) {
            return ENABLED;
        }
        if (DISABLED.code.equals(code)) {
            return DISABLED;
        }
        return null;
    }
}
package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围枚举
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    /** 全部数据 */
    ALL(1, "全部数据"),

    /** 自定义数据 */
    CUSTOM(2, "自定义数据"),

    /** 本部门数据 */
    DEPT(3, "本部门数据"),

    /** 本部门及以下数据 */
    DEPT_AND_CHILD(4, "本部门及以下数据"),

    /** 仅本人数据 */
    SELF(5, "仅本人数据");

    private final Integer code;
    private final String desc;

    public static DataScopeEnum of(Integer code) {
        if (code == null) {
            return SELF;
        }
        for (DataScopeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return SELF;
    }
}
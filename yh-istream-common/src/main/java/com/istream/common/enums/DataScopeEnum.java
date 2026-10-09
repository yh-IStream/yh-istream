package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据权限范围枚举
 *
 * <p>定义角色可访问的数据范围，由 {@code DataScopeAspect} 根据此枚举
 * 动态拼接 SQL 条件实现行级数据隔离。</p>
 *
 * @author istream
 * @since 2026-08-17
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

    private static final Logger log = LoggerFactory.getLogger(DataScopeEnum.class);

    private final Integer code;
    private final String desc;

    public static DataScopeEnum of(Integer code) {
        if (code == null) {
            log.warn("DataScopeEnum.of(null) → 降级为 SELF，请检查角色 dataScope 字段是否为空");
            return SELF;
        }
        for (DataScopeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        log.warn("DataScopeEnum.of({}) → 无匹配项，降级为 SELF，请检查角色 dataScope 字段值是否合法", code);
        return SELF;
    }
}
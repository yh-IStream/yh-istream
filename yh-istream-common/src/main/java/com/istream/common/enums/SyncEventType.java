package com.istream.common.enums;

import lombok.Getter;

/**
 * 数据同步事件类型
 *
 * <p>用于 {@link com.istream.common.annotation.RealTimeSync} 注解声明
 * Service 类关注的实体变更类型，AOP 切面据此自动发布 {@link EventType#DATA_CHANGE} 事件。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Getter
public enum SyncEventType {

    /** 实体创建 */
    CREATE(1, "创建"),

    /** 实体更新 */
    UPDATE(2, "更新"),

    /** 实体删除 */
    DELETE(3, "删除");

    private final int code;
    private final String desc;

    SyncEventType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
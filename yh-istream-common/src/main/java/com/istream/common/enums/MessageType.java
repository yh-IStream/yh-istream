package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * <ul>
 *   <li>{@link #USER} — 定向消息，通过 sys_message_user 关联特定用户</li>
 *   <li>{@link #TENANT} — 租户广播，tenant_id 匹配的用户均可见</li>
 *   <li>{@link #GLOBAL} — 全量广播，所有用户可见</li>
 * </ul>
 *
 * @author istream
 * @since 2026-10-16
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /** 定向消息：发送给指定用户 */
    USER(0, "定向"),

    /** 租户广播：发送给同一租户下所有用户 */
    TENANT(1, "租户广播"),

    /** 全量广播：发送给所有用户 */
    GLOBAL(2, "全量广播");

    private final int code;
    private final String desc;
}
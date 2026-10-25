package com.istream.common.enums;

/**
 * 限流维度枚举
 *
 * <p>定义 {@link com.istream.common.annotation.RateLimit} 注解的限流维度：</p>
 * <ul>
 *   <li>{@link #GLOBAL} — 全局限流，所有客户端共享令牌桶</li>
 *   <li>{@link #IP} — 按来源 IP 限流，每个 IP 独享令牌桶</li>
 *   <li>{@link #USER} — 按登录用户 ID 限流，每个用户独享令牌桶；未登录时降级为 IP 维度</li>
 * </ul>
 *
 * @author istream
 * @since 2026-10-22
 */
public enum LimitType {

    /**
     * 全局限流：所有客户端共享同一个令牌桶
     */
    GLOBAL,

    /**
     * 按来源 IP 限流：每个客户端 IP 独享一个令牌桶
     */
    IP,

    /**
     * 按登录用户 ID 限流：每个用户独享一个令牌桶；未登录时降级为 IP 维度
     */
    USER
}
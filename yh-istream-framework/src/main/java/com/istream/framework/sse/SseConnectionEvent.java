package com.istream.framework.sse;

/**
 * SSE 连接生命周期事件
 *
 * <p>当用户 SSE 连接建立或断开时发布，供 {@code com.istream.message.connection.ConnectionRegistry} 等组件监听。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
public record SseConnectionEvent(
        /* 事件类型 */
        Type eventType,
        /* 用户ID */
        Long userId
) {
    public enum Type {
        /** 连接建立 */
        CONNECTED,
        /** 连接断开（含超时、异常、主动关闭） */
        DISCONNECTED
    }
}
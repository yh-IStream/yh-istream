package com.istream.framework.sse;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * SSE 连接生命周期事件
 *
 * <p>当用户 SSE 连接建立或断开时发布，供 {@link com.istream.message.connection.ConnectionRegistry} 等组件监听。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Getter
public class SseConnectionEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** 事件类型 */
    private final Type eventType;

    /** 用户ID */
    private final Long userId;

    public SseConnectionEvent(Object source, Type eventType, Long userId) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
    }

    public enum Type {
        /** 连接建立 */
        CONNECTED,
        /** 连接断开（含超时、异常、主动关闭） */
        DISCONNECTED
    }
}
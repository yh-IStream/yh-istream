package com.istream.common.model.sse;

import java.time.LocalDateTime;

/**
 * SSE 实时推送事件
 *
 * @author istream
 * @since 2026-08-20
 */
public record SseEvent(
        /* 事件类型 */
        String type,

        /* 事件数据 */
        Object data,

        /* 事件时间戳 */
        LocalDateTime timestamp
) {

    public static SseEvent of(String type, Object data) {
        return new SseEvent(type, data, LocalDateTime.now());
    }

    public static SseEvent heartbeat() {
        return new SseEvent("HEARTBEAT", "ping", LocalDateTime.now());
    }
}
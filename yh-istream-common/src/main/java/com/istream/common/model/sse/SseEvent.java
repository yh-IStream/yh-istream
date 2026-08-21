package com.istream.common.model.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSE 实时推送事件
 *
 * @author isteam
 * @since 2026-08-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {

    private String type;

    private Object data;

    private LocalDateTime timestamp;

    public static SseEvent of(String type, Object data) {
        return new SseEvent(type, data, LocalDateTime.now());
    }

    public static SseEvent heartbeat() {
        return new SseEvent("HEARTBEAT", "ping", LocalDateTime.now());
    }
}
package com.istream.watchdog.channel;

import com.istream.common.enums.EventType;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.watchdog.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 告警通道
 *
 * <p>通过 SSE 实时推送告警到在线用户的前端铃铛。
 * 在线用户立即收到，离线用户由升级策略选择下一级通道。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@RequiredArgsConstructor
public class SseAlertChannel implements AlertChannel {

    private final SseService sseService;

    @Override
    public String getName() {
        return "sse";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            Long userId = Long.parseLong(target);
            SseEvent sseEvent = SseEvent.of(EventType.ALERT.name(), event);
            sseService.sendToUser(userId, sseEvent);
            log.info("SSE 告警推送成功: userId={}, rule={}", userId, event.getRuleName());
        } catch (Exception e) {
            log.warn("SSE 告警推送失败: target={}, rule={}", target, event.getRuleName(), e);
        }
    }
}
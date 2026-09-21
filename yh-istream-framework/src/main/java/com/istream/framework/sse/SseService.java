package com.istream.framework.sse;

import com.istream.common.model.sse.SseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 实时推送服务
 * <p>
 * 管理客户端 SSE 连接，支持广播和单播。每30秒发送心跳保活，
 * 连接超时或异常时自动清理。连接建立/断开时发布 {@link SseConnectionEvent} 事件。
 *
 * @author istream
 * @since 2026-08-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT = 300_000L;

    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 客户端订阅 SSE 连接
     *
     * @param userId 用户ID
     * @return SseEmitter 实例
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 连接完成: userId={}", userId);
            emitters.remove(userId);
            eventPublisher.publishEvent(new SseConnectionEvent(this, SseConnectionEvent.Type.DISCONNECTED, userId));
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时: userId={}", userId);
            emitters.remove(userId);
            eventPublisher.publishEvent(new SseConnectionEvent(this, SseConnectionEvent.Type.DISCONNECTED, userId));
        });

        emitter.onError(ex -> {
            log.debug("SSE 连接异常: userId={}, error={}", userId, ex.getMessage());
            emitters.remove(userId);
            eventPublisher.publishEvent(new SseConnectionEvent(this, SseConnectionEvent.Type.DISCONNECTED, userId));
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(SseEvent.of("CONNECTED", "SSE 连接已建立")));
        } catch (IOException e) {
            log.warn("SSE 初始握手失败: userId={}", userId, e);
            emitters.remove(userId);
        }

        log.info("SSE 客户端已连接: userId={}, 当前连接数={}", userId, emitters.size());
        eventPublisher.publishEvent(new SseConnectionEvent(this, SseConnectionEvent.Type.CONNECTED, userId));
        return emitter;
    }

    /**
     * 广播事件给所有已连接客户端
     *
     * @param event SSE 事件
     */
    public void broadcast(SseEvent event) {
        if (emitters.isEmpty()) {
            return;
        }
        log.debug("SSE 广播: type={}, 接收方数={}", event.getType(), emitters.size());
        for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
            sendEvent(entry.getKey(), entry.getValue(), event);
        }
    }

    /**
     * 向指定用户发送事件
     *
     * @param userId 用户ID
     * @param event  SSE 事件
     */
    public void sendToUser(Long userId, SseEvent event) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            sendEvent(userId, emitter, event);
        }
    }

    /**
     * 每30秒发送心跳，清理死连接
     */
    @Scheduled(fixedRate = 30_000)
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        SseEvent heartbeat = SseEvent.heartbeat();
        for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
            sendEvent(entry.getKey(), entry.getValue(), heartbeat);
        }
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return emitters.size();
    }

    private void sendEvent(Long userId, SseEmitter emitter, SseEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.getType().toLowerCase())
                    .data(event));
        } catch (IOException e) {
            log.debug("SSE 发送失败，移除连接: userId={}", userId);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }
}
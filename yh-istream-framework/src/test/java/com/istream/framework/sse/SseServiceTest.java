package com.istream.framework.sse;

import com.istream.common.model.sse.SseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * SseService 单元测试
 *
 * <p>验证 SSE 连接管理的核心行为：订阅、连接计数、广播、单播。</p>
 *
 * @author istream
 * @since 2026-09-19
 */
@DisplayName("SseService 实时推送服务")
class SseServiceTest {

    private SseService sseService;

    @BeforeEach
    void setUp() {
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
        sseService = new SseService(mockPublisher);
    }

    @Test
    @DisplayName("订阅后连接数增加")
    void shouldIncreaseConnectionCountAfterSubscribe() {
        assertEquals(0, sseService.getConnectionCount());

        sseService.subscribe(1L);
        assertEquals(1, sseService.getConnectionCount());

        sseService.subscribe(2L);
        assertEquals(2, sseService.getConnectionCount());
    }

    @Test
    @DisplayName("订阅返回非空 SseEmitter")
    void shouldReturnNonNullEmitter() {
        SseEmitter emitter = sseService.subscribe(1L);
        assertNotNull(emitter);
    }

    @Test
    @DisplayName("同一用户重复订阅覆盖旧连接")
    void shouldReplaceOldConnectionOnReSubscribe() {
        sseService.subscribe(1L);
        sseService.subscribe(1L);
        assertEquals(1, sseService.getConnectionCount());
    }

    @Test
    @DisplayName("广播不抛异常（无连接时）")
    void shouldBroadcastWithoutErrorWhenNoConnections() {
        SseEvent event = SseEvent.of("TEST", "data");
        sseService.broadcast(event);
    }

    @Test
    @DisplayName("单播不抛异常（用户不在线时）")
    void shouldSendToUserWithoutErrorWhenUserOffline() {
        SseEvent event = SseEvent.of("TEST", "data");
        sseService.sendToUser(999L, event);
    }

    @Test
    @DisplayName("心跳不抛异常（无连接时）")
    void shouldHeartbeatWithoutErrorWhenNoConnections() {
        sseService.heartbeat();
    }

    @Test
    @DisplayName("心跳不抛异常（有连接时）")
    void shouldHeartbeatWithoutErrorWhenHasConnections() {
        sseService.subscribe(1L);
        sseService.heartbeat();
    }

    @Test
    @DisplayName("多个用户订阅后连接数正确")
    void shouldTrackMultipleConnections() {
        for (long i = 1; i <= 5; i++) {
            sseService.subscribe(i);
        }
        assertEquals(5, sseService.getConnectionCount());
    }
}
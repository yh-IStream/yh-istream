package com.istream.framework.sse;

import com.istream.common.model.sse.SseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        RedissonClient mockRedisson = mock(RedissonClient.class);
        sseService = new SseService(mockPublisher, mockRedisson);
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
    @DisplayName("同一用户多次订阅并存（多标签页）")
    void shouldKeepMultipleConnectionsForSameUser() {
        sseService.subscribe(1L);
        sseService.subscribe(1L);
        assertEquals(2, sseService.getConnectionCount());
        assertEquals(1, sseService.getOnlineUserCount());
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

    @Test
    @DisplayName("单用户连接数超过上限时拒绝新连接")
    void shouldRejectConnectionWhenExceedingPerUserLimit() {
        int limit = 8;
        for (int i = 0; i < limit; i++) {
            sseService.subscribe(1L);
        }
        assertEquals(limit, sseService.getConnectionCount());

        sseService.subscribe(1L);
        assertEquals(limit, sseService.getConnectionCount());
        assertEquals(1, sseService.getOnlineUserCount());
    }

    @Test
    @DisplayName("并发订阅不会突破单用户连接数上限")
    void shouldNotExceedPerUserLimitUnderConcurrency() throws InterruptedException {
        int limit = 8;
        int attempts = 20;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(attempts);
        ExecutorService executor = Executors.newFixedThreadPool(attempts);

        for (int i = 0; i < attempts; i++) {
            executor.execute(() -> {
                try {
                    startGate.await();
                    sseService.subscribe(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }
        startGate.countDown();
        doneGate.await();
        executor.shutdown();

        assertEquals(limit, sseService.getConnectionCount());
    }
}
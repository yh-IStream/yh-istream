package com.istream.message.event;

import com.istream.common.enums.EventType;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.message.connection.ConnectionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * EventBus 单元测试
 *
 * @author istream
 * @since 2026-09-21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventBus SSE 事件总线")
class EventBusTest {

    @Mock
    private SseService sseService;
    @Mock
    private ConnectionRegistry connectionRegistry;

    @InjectMocks
    private EventBus eventBus;

    private final SseEvent event = SseEvent.of("DATA_CHANGE", "测试数据");

    @Test
    @DisplayName("publishToUser 正常推送")
    void shouldPublishToUser() {
        eventBus.publishToUser(100L, event);

        verify(sseService).sendToUser(100L, event);
    }

    @Test
    @DisplayName("publishToUser userId 为 null 时跳过")
    void shouldSkipPublishToUserWhenNull() {
        eventBus.publishToUser(null, event);
        eventBus.publishToUser(100L, null);

        verify(sseService, never()).sendToUser(anyLong(), any(SseEvent.class));
    }

    @Test
    @DisplayName("publishToUser SSE 异常不抛")
    void shouldNotThrowOnPublishToUserError() {
        doThrow(new RuntimeException("SSE 异常")).when(sseService).sendToUser(anyLong(), any(SseEvent.class));

        eventBus.publishToUser(100L, event);
    }

    @Test
    @DisplayName("publishToTenant 正常广播")
    void shouldPublishToTenant() {
        eventBus.publishToTenant(1L, event);

        verify(sseService).broadcast(event);
    }

    @Test
    @DisplayName("publishToTenant tenantId 为 null 时跳过")
    void shouldSkipPublishToTenantWhenNull() {
        eventBus.publishToTenant(null, event);
        eventBus.publishToTenant(1L, null);

        verify(sseService, never()).broadcast(any(SseEvent.class));
    }

    @Test
    @DisplayName("broadcast 全量广播")
    void shouldBroadcast() {
        eventBus.broadcast(event);

        verify(sseService).broadcast(event);
    }

    @Test
    @DisplayName("broadcast event 为 null 时跳过")
    void shouldSkipBroadcastWhenNull() {
        eventBus.broadcast(null);

        verify(sseService, never()).broadcast(any(SseEvent.class));
    }

    @Test
    @DisplayName("broadcast SSE 异常不抛")
    void shouldNotThrowOnBroadcastError() {
        doThrow(new RuntimeException("SSE 异常")).when(sseService).broadcast(any(SseEvent.class));

        eventBus.broadcast(event);
    }
}
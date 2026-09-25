package com.istream.message.center;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.istream.common.enums.EventType;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.message.entity.SysMessage;
import com.istream.message.entity.SysMessageUser;
import com.istream.message.service.SysMessageService;
import com.istream.message.service.SysMessageUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * MessageCenterImpl 单元测试
 *
 * @author istream
 * @since 2026-09-21
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MessageCenterImpl 消息中心")
class MessageCenterImplTest {

    @Mock
    private SysMessageService sysMessageService;
    @Mock
    private SysMessageUserService sysMessageUserService;
    @Mock
    private SseService sseService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RAtomicLong atomicLong;

    @InjectMocks
    private MessageCenterImpl messageCenter;

    @BeforeEach
    void setUp() {
        when(sysMessageService.save(any(SysMessage.class))).thenAnswer(inv -> {
            SysMessage msg = inv.getArgument(0);
            msg.setId(1L);
            return true;
        });
    }

    @Test
    @DisplayName("sendToUser 发送给指定用户并落库")
    void shouldSendToUser() {
        when(sysMessageUserService.save(any(SysMessageUser.class))).thenReturn(true);
        when(redissonClient.getAtomicLong(anyString())).thenReturn(atomicLong);

        messageCenter.sendToUser(100L, "标题", "内容", EventType.NOTIFICATION, "order", 200L);

        verify(sysMessageService).save(any(SysMessage.class));
        verify(sysMessageUserService).save(any(SysMessageUser.class));
        verify(sseService).sendToUser(anyLong(), any(SseEvent.class));
        verify(atomicLong).incrementAndGet();
    }

    @Test
    @DisplayName("sendToUser userId 为 null 时直接返回")
    void shouldSkipSendToUserWhenUserIdNull() {
        messageCenter.sendToUser(null, "标题", "内容", EventType.NOTIFICATION, "order", 200L);

        verify(sysMessageService, never()).save(any(SysMessage.class));
        verify(sseService, never()).sendToUser(anyLong(), any(SseEvent.class));
    }

    @Test
    @DisplayName("sendToTenant 发送给租户并广播 SSE")
    void shouldSendToTenant() {
        messageCenter.sendToTenant(1L, "公告", "内容", EventType.SYSTEM, "tenant", 1L);

        verify(sysMessageService).save(any(SysMessage.class));
        verify(sseService).broadcast(any(SseEvent.class));
    }

    @Test
    @DisplayName("broadcast 全量广播")
    void shouldBroadcast() {
        messageCenter.broadcast("全站公告", "内容", EventType.SYSTEM);

        verify(sysMessageService).save(any(SysMessage.class));
        verify(sseService).broadcast(any(SseEvent.class));
    }

    @Test
    @DisplayName("getUnreadCount Redis 命中时返回计数")
    void shouldGetUnreadCountFromRedis() {
        when(redissonClient.getAtomicLong("message:unread:100")).thenReturn(atomicLong);
        when(atomicLong.isExists()).thenReturn(true);
        when(atomicLong.get()).thenReturn(5L);

        long count = messageCenter.getUnreadCount(100L);

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("getUnreadCount Redis 未命中时降级 DB 查询")
    void shouldFallbackToDbWhenRedisMiss() {
        when(redissonClient.getAtomicLong("message:unread:100")).thenReturn(atomicLong);
        when(atomicLong.isExists()).thenReturn(false);
        when(sysMessageUserService.count(any(LambdaQueryWrapper.class))).thenReturn(3L);

        long count = messageCenter.getUnreadCount(100L);

        assertEquals(3L, count);
    }

    @Test
    @DisplayName("markAsRead 标记已读并减 Redis 计数")
    void shouldMarkAsRead() {
        when(sysMessageUserService.update(any())).thenReturn(true);
        when(redissonClient.getAtomicLong("message:unread:100")).thenReturn(atomicLong);
        when(atomicLong.isExists()).thenReturn(true);
        when(atomicLong.decrementAndGet()).thenReturn(0L);

        try (MockedConstruction<LambdaUpdateWrapper> ignored = mockConstruction(
                LambdaUpdateWrapper.class, withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {
            messageCenter.markAsRead(1L, 100L);
        }

        verify(sysMessageUserService).update(any());
        verify(atomicLong).decrementAndGet();
    }

    @Test
    @DisplayName("markAsRead 未更新时不自减 Redis")
    void shouldNotDecrementWhenNotUpdated() {
        when(sysMessageUserService.update(any())).thenReturn(false);

        try (MockedConstruction<LambdaUpdateWrapper> ignored = mockConstruction(
                LambdaUpdateWrapper.class, withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {
            messageCenter.markAsRead(1L, 100L);
        }

        verify(redissonClient, never()).getAtomicLong(anyString());
    }

    @Test
    @DisplayName("markAllAsRead 全部已读并归零 Redis")
    void shouldMarkAllAsRead() {
        when(sysMessageUserService.update(any())).thenReturn(true);
        when(redissonClient.getAtomicLong("message:unread:100")).thenReturn(atomicLong);

        try (MockedConstruction<LambdaUpdateWrapper> ignored = mockConstruction(
                LambdaUpdateWrapper.class, withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {
            messageCenter.markAllAsRead(100L);
        }

        verify(sysMessageUserService).update(any());
        verify(atomicLong).set(0);
    }

    @Test
    @DisplayName("resolveAggregate 10 秒内同一实体聚合")
    void shouldAggregateSameEntityWithinWindow() {
        SysMessage existing = new SysMessage();
        existing.setId(1L);
        existing.setTitle("旧标题");
        existing.setContent("旧内容");
        when(sysMessageService.getOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        messageCenter.sendToUser(100L, "新标题", "新内容", EventType.DATA_CHANGE, "order", 200L);

        verify(sysMessageService).updateById(any(SysMessage.class));
        verify(sysMessageService, never()).save(any(SysMessage.class));
    }

    @Test
    @DisplayName("SSE SseEvent 包含消息数据")
    void shouldBuildSseEventWithMessage() {
        messageCenter.broadcast("测试", "内容", EventType.NOTIFICATION);

        ArgumentCaptor<SseEvent> captor = ArgumentCaptor.forClass(SseEvent.class);
        verify(sseService).broadcast(captor.capture());
        SseEvent event = captor.getValue();

        assertEquals(EventType.NOTIFICATION.name(), event.getType());
        assertNotNull(event.getData());
        assertTrue(event.getData() instanceof SysMessage);
        SysMessage msg = (SysMessage) event.getData();
        assertEquals("测试", msg.getTitle());
        assertEquals("内容", msg.getContent());
    }
}
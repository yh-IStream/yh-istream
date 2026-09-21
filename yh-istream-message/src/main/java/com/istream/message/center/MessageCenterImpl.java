package com.istream.message.center;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.istream.common.enums.EventType;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.sse.SseService;
import com.istream.message.entity.SysMessage;
import com.istream.message.entity.SysMessageUser;
import com.istream.message.service.SysMessageService;
import com.istream.message.service.SysMessageUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息中心实现
 *
 * <p>核心消息发送入口，提供消息落库 + SSE 即时推送 + 聚合去重。</p>
 *
 * <h3>消息聚合策略</h3>
 * <p>同一 (entityType, entityId, eventType) 组合在 10 秒内的多次变更聚合为 1 条通知，
 * 更新现有消息的标题和内容，只推一次。</p>
 *
 * <h3>降级约束（决策 7）</h3>
 * <p>SSE 推送失败只 warn 不抛异常，不阻塞主业务流。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCenterImpl implements MessageCenter {

    private static final Duration AGGREGATE_WINDOW = Duration.ofSeconds(10);

    private final SysMessageService sysMessageService;
    private final SysMessageUserService sysMessageUserService;
    private final SseService sseService;

    /**
     * 聚合锁，防止并发场景下同一实体重复创建消息
     */
    private final Map<String, Object> aggregateLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToUser(Long userId, String title, String content, EventType eventType,
                           String entityType, Long entityId) {
        if (userId == null) {
            return;
        }
        SysMessage message = resolveAggregate(title, content, eventType, entityType, entityId);
        if (message == null) {
            return;
        }
        saveMessageUser(message.getId(), userId);
        pushToUser(userId, message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToTenant(Long tenantId, String title, String content, EventType eventType,
                             String entityType, Long entityId) {
        if (tenantId == null) {
            return;
        }
        SysMessage message = resolveAggregate(title, content, eventType, entityType, entityId);
        if (message == null) {
            return;
        }
        broadcastSse(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void broadcast(String title, String content, EventType eventType) {
        SysMessage message = createMessage(title, content, eventType, null, null);
        broadcastSse(message);
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        LambdaQueryWrapper<SysMessageUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageUser::getUserId, userId)
                .eq(SysMessageUser::getIsRead, 0);
        return sysMessageUserService.count(wrapper);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return;
        }
        LambdaUpdateWrapper<SysMessageUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessageUser::getMessageId, messageId)
                .eq(SysMessageUser::getUserId, userId)
                .set(SysMessageUser::getIsRead, 1)
                .set(SysMessageUser::getReadTime, LocalDateTime.now());
        sysMessageUserService.update(wrapper);
    }

    @Override
    public void markAllAsRead(Long userId) {
        if (userId == null) {
            return;
        }
        LambdaUpdateWrapper<SysMessageUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessageUser::getUserId, userId)
                .eq(SysMessageUser::getIsRead, 0)
                .set(SysMessageUser::getIsRead, 1)
                .set(SysMessageUser::getReadTime, LocalDateTime.now());
        sysMessageUserService.update(wrapper);
    }

    /**
     * 聚合策略：同一实体 10 秒内多次变更 → 更新已有消息而非新建
     *
     * @return null 表示已聚合（调用方跳过后续推送），非 null 表示新消息
     */
    private SysMessage resolveAggregate(String title, String content, EventType eventType,
                                        String entityType, Long entityId) {
        if (entityType == null || entityId == null) {
            return createMessage(title, content, eventType, entityType, entityId);
        }

        String lockKey = entityType + ":" + entityId + ":" + eventType.name();
        synchronized (aggregateLocks.computeIfAbsent(lockKey, k -> new Object())) {
            LocalDateTime since = LocalDateTime.now().minus(AGGREGATE_WINDOW);
            LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMessage::getEntityType, entityType)
                    .eq(SysMessage::getEntityId, entityId)
                    .eq(SysMessage::getEventType, eventType.name())
                    .ge(SysMessage::getCreateTime, since)
                    .orderByDesc(SysMessage::getCreateTime)
                    .last("LIMIT 1");

            SysMessage existing = sysMessageService.getOne(wrapper);
            if (existing != null) {
                existing.setTitle(title);
                existing.setContent(content);
                existing.setUpdateTime(LocalDateTime.now());
                sysMessageService.updateById(existing);
                log.debug("消息聚合: entityType={}, entityId={}, eventType={}", entityType, entityId, eventType);
                return existing;
            }
            return createMessage(title, content, eventType, entityType, entityId);
        }
    }

    /**
     * 创建并保存消息
     */
    private SysMessage createMessage(String title, String content, EventType eventType,
                                     String entityType, Long entityId) {
        SysMessage message = new SysMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setEventType(eventType.name());
        message.setEntityType(entityType);
        message.setEntityId(entityId);

        try {
            Long loginUserId = SecurityUtils.getLoginUserId();
            message.setSenderId(loginUserId);
            message.setSenderName(loginUserId != null ? String.valueOf(loginUserId) : null);
        } catch (Exception e) {
            log.debug("无法获取当前登录用户信息，使用系统发送者");
        }

        sysMessageService.save(message);
        return message;
    }

    /**
     * 保存用户消息关联
     */
    private void saveMessageUser(Long messageId, Long userId) {
        SysMessageUser messageUser = new SysMessageUser();
        messageUser.setMessageId(messageId);
        messageUser.setUserId(userId);
        messageUser.setIsRead(0);
        sysMessageUserService.save(messageUser);
    }

    /**
     * SSE 推送给指定用户（失败降级）
     */
    private void pushToUser(Long userId, SysMessage message) {
        try {
            SseEvent event = buildSseEvent(message);
            sseService.sendToUser(userId, event);
        } catch (Exception e) {
            log.warn("SSE 单播推送失败（降级，消息已落库）: userId={}, messageId={}", userId, message.getId(), e);
        }
    }

    /**
     * SSE 全量广播（失败降级）
     */
    private void broadcastSse(SysMessage message) {
        try {
            SseEvent event = buildSseEvent(message);
            sseService.broadcast(event);
        } catch (Exception e) {
            log.warn("SSE 广播推送失败（降级，消息已落库）: messageId={}", message.getId(), e);
        }
    }

    /**
     * 构建 SSE 事件
     */
    private SseEvent buildSseEvent(SysMessage message) {
        return SseEvent.of(message.getEventType(), message);
    }
}
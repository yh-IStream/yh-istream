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
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 消息中心实现
 *
 * <p>核心消息发送入口，提供消息落库 + SSE 即时推送 + 聚合去重。</p>
 *
 * <h3>消息聚合策略</h3>
 * <p>同一 (entityType, entityId, eventType) 组合在 10 秒内的多次变更聚合为 1 条通知，
 * 更新现有消息的标题和内容，只推一次。</p>
 *
 * <h3>锁与事务顺序</h3>
 * <p>遵循项目原则：加锁 → 开事务 → 执行 → 提交事务 → 释放锁。
 * {@code sendToUser}/{@code sendToTenant} 使用 Redisson 分布式锁 + {@link TransactionTemplate} 编程式事务，
 * 确保：① 多实例部署下聚合锁跨 JVM 生效；② 锁在事务外获取，避免事务内持锁时间过长；
 * ③ SSE 推送和 Redis 计数在事务提交后执行，保证数据一致性。</p>
 *
 * <h3>Redis 未读计数</h3>
 * <p>每用户维护一个 Redis AtomicLong（key: message:unread:{userId}），
 * 发送 +1、已读 -1、全部已读归零。Redis 不可用时降级为 DB count 查询。</p>
 *
 * <h3>降级约束</h3>
 * <p>SSE 推送失败只 warn 不抛异常，不阻塞主业务流。
 * 聚合锁获取超时/中断时降级为无锁模式，最坏情况产生重复消息。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCenterImpl implements MessageCenter {

    private static final Duration AGGREGATE_WINDOW = Duration.ofSeconds(10);
    private static final String UNREAD_KEY_PREFIX = "message:unread:";

    private final SysMessageService sysMessageService;
    private final SysMessageUserService sysMessageUserService;
    private final SseService sseService;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String AGGREGATE_LOCK_PREFIX = "message:aggregate:";

    @Override
    public void sendToUser(Long userId, String title, String content, EventType eventType,
                           String entityType, Long entityId) {
        if (userId == null) {
            return;
        }
        RLock lock = acquireAggregateLock(entityType, entityId, eventType);
        try {
            SysMessage message = transactionTemplate.execute(status -> {
                SysMessage msg = resolveAggregate(title, content, eventType, entityType, entityId);
                if (msg != null) {
                    saveMessageUser(msg.getId(), userId);
                }
                return msg;
            });
            if (message != null) {
                pushToUser(userId, message);
                incrementUnread(userId);
            }
        } finally {
            releaseLockSafely(lock);
        }
    }

    @Override
    public void sendToTenant(Long tenantId, String title, String content, EventType eventType,
                             String entityType, Long entityId) {
        if (tenantId == null) {
            return;
        }
        RLock lock = acquireAggregateLock(entityType, entityId, eventType);
        try {
            SysMessage message = transactionTemplate.execute(status ->
                    resolveAggregate(title, content, eventType, entityType, entityId));
            if (message != null) {
                broadcastSse(message);
            }
        } finally {
            releaseLockSafely(lock);
        }
    }

    @Override
    public void broadcast(String title, String content, EventType eventType) {
        SysMessage message = transactionTemplate.execute(status ->
                createMessage(title, content, eventType, null, null));
        if (message != null) {
            broadcastSse(message);
        }
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        try {
            RAtomicLong counter = redissonClient.getAtomicLong(UNREAD_KEY_PREFIX + userId);
            if (counter.isExists()) {
                long count = counter.get();
                return Math.max(count, 0);
            }
        } catch (Exception e) {
            log.debug("Redis 未读计数查询失败，降级为 DB 查询: userId={}", userId, e);
        }
        return getUnreadCountFromDb(userId);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return;
        }
        LambdaUpdateWrapper<SysMessageUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessageUser::getMessageId, messageId)
                .eq(SysMessageUser::getUserId, userId)
                .eq(SysMessageUser::getIsRead, 0)
                .set(SysMessageUser::getIsRead, 1)
                .set(SysMessageUser::getReadTime, LocalDateTime.now());
        boolean updated = sysMessageUserService.update(wrapper);
        if (updated) {
            decrementUnread(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        resetUnread(userId);
    }

    private void incrementUnread(Long userId) {
        try {
            redissonClient.getAtomicLong(UNREAD_KEY_PREFIX + userId).incrementAndGet();
        } catch (Exception e) {
            log.debug("Redis 未读计数 +1 失败（降级）: userId={}", userId, e);
        }
    }

    private void decrementUnread(Long userId) {
        try {
            RAtomicLong counter = redissonClient.getAtomicLong(UNREAD_KEY_PREFIX + userId);
            if (counter.isExists()) {
                long count = counter.decrementAndGet();
                if (count < 0) {
                    counter.set(0);
                }
            }
        } catch (Exception e) {
            log.debug("Redis 未读计数 -1 失败（降级）: userId={}", userId, e);
        }
    }

    private void resetUnread(Long userId) {
        try {
            redissonClient.getAtomicLong(UNREAD_KEY_PREFIX + userId).set(0);
        } catch (Exception e) {
            log.debug("Redis 未读计数归零失败（降级）: userId={}", userId, e);
        }
    }

    private long getUnreadCountFromDb(Long userId) {
        LambdaQueryWrapper<SysMessageUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageUser::getUserId, userId)
                .eq(SysMessageUser::getIsRead, 0);
        return sysMessageUserService.count(wrapper);
    }

    /**
     * 获取聚合分布式锁
     *
     * <p>当 entityType 或 entityId 为 null 时无需聚合，返回 null。
     * 锁获取超时或被中断时降级为无锁模式（返回 null），最坏情况产生重复消息。</p>
     *
     * @return Redisson 锁实例，无需聚合时返回 null
     */
    private RLock acquireAggregateLock(String entityType, Long entityId, EventType eventType) {
        if (entityType == null || entityId == null) {
            return null;
        }
        String lockKey = AGGREGATE_LOCK_PREFIX + entityType + ":" + entityId + ":" + eventType.name();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                log.warn("聚合锁获取超时，降级为无锁模式: {}", lockKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("聚合锁等待被中断，降级为无锁模式: {}", lockKey);
            return null;
        }
        return lock;
    }

    /**
     * 安全释放分布式锁
     */
    private void releaseLockSafely(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 聚合策略：同一实体 10 秒内多次变更 → 更新已有消息而非新建
     *
     * <p>纯数据库操作，不包含锁逻辑。调用方须在事务内调用，
     * 且在事务外已持有聚合分布式锁（加锁 → 开事务 → 执行 → 提交 → 释放锁）。</p>
     *
     * @return null 表示已聚合（调用方跳过后续推送），非 null 表示新消息
     */
    private SysMessage resolveAggregate(String title, String content, EventType eventType,
                                        String entityType, Long entityId) {
        if (entityType == null || entityId == null) {
            return createMessage(title, content, eventType, entityType, entityId);
        }

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
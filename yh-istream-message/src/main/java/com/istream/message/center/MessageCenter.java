package com.istream.message.center;

import com.istream.common.enums.EventType;

/**
 * 消息中心
 *
 * <p>统一的消息发送入口，提供按用户、按租户、全量广播三种发送方式。</p>
 * <p>每条消息经过三层保障：MySQL 持久化落库（不丢）→ SSE 即时推送（快）→ 前端定时拉未读（兜底）。</p>
 * <p>高频场景自动聚合：同一实体同一事件类型 10 秒内多次变更聚合为 1 条通知。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
public interface MessageCenter {

    /**
     * 向指定用户发送消息（持久化 + 即时推送）
     *
     * @param userId     接收用户ID
     * @param title      消息标题
     * @param content    消息内容
     * @param eventType  事件类型
     * @param entityType 实体类型（用于聚合，可为 null）
     * @param entityId   实体ID（用于聚合，可为 null）
     */
    void sendToUser(Long userId, String title, String content, EventType eventType,
                    String entityType, Long entityId);

    /**
     * 向指定租户下所有用户发送消息（持久化 + 即时推送）
     *
     * @param tenantId   租户ID
     * @param title      消息标题
     * @param content    消息内容
     * @param eventType  事件类型
     * @param entityType 实体类型（用于聚合，可为 null）
     * @param entityId   实体ID（用于聚合，可为 null）
     */
    void sendToTenant(Long tenantId, String title, String content, EventType eventType,
                      String entityType, Long entityId);

    /**
     * 全量广播消息（持久化 + 即时推送）
     *
     * @param title     消息标题
     * @param content   消息内容
     * @param eventType 事件类型
     */
    void broadcast(String title, String content, EventType eventType);

    /**
     * 获取用户未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    long getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 全部标记为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
}
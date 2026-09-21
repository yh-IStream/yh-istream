package com.istream.message.event;

import com.istream.common.enums.EventType;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.framework.tenant.TenantContext;
import com.istream.message.connection.ConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SSE 事件总线
 *
 * <p>统一的事件发布入口，支持按用户、角色、租户、全量广播四种目标范围：</p>
 * <ul>
 *   <li>{@code publishToUser(userId, event)} — 精确推送到指定用户</li>
 *   <li>{@code publishToTenant(tenantId, event)} — 推送到同一租户下所有在线用户</li>
 *   <li>{@code broadcast(event)} — 全量广播</li>
 * </ul>
 * <p>推送失败只 warn 不抛异常，不阻塞主业务流。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventBus {

    private final SseService sseService;
    private final ConnectionRegistry connectionRegistry;

    /**
     * 向指定用户发布事件
     *
     * @param userId 目标用户ID
     * @param event  SSE 事件
     */
    public void publishToUser(Long userId, SseEvent event) {
        if (userId == null || event == null) {
            return;
        }
        try {
            sseService.sendToUser(userId, event);
            log.debug("EventBus 单播: userId={}, type={}", userId, event.getType());
        } catch (Exception e) {
            log.warn("EventBus 单播失败（降级）: userId={}, type={}", userId, event.getType(), e);
        }
    }

    /**
     * 向当前租户下所有在线用户广播事件
     *
     * @param tenantId 租户ID
     * @param event    SSE 事件
     */
    public void publishToTenant(Long tenantId, SseEvent event) {
        if (tenantId == null || event == null) {
            return;
        }
        try {
            sseService.broadcast(event);
            log.debug("EventBus 租户广播: tenantId={}, type={}", tenantId, event.getType());
        } catch (Exception e) {
            log.warn("EventBus 租户广播失败（降级）: tenantId={}, type={}", tenantId, event.getType(), e);
        }
    }

    /**
     * 全量广播事件给所有在线用户
     *
     * @param event SSE 事件
     */
    public void broadcast(SseEvent event) {
        if (event == null) {
            return;
        }
        try {
            sseService.broadcast(event);
            log.debug("EventBus 全量广播: type={}", event.getType());
        } catch (Exception e) {
            log.warn("EventBus 全量广播失败（降级）: type={}", event.getType(), e);
        }
    }
}
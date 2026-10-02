package com.istream.common.event;

import com.istream.common.enums.SyncEventType;

import java.time.LocalDateTime;

/**
 * 数据变更事件
 *
 * <p>由 {@code @RealTimeSync} AOP 切面在 Service 方法执行成功后发布。
 * 下游模块（MessageCenter、Watchdog）通过 Spring {@code @EventListener}
 * 监听此事件，实现解耦。</p>
 *
 * <p>事件流：Service 方法执行 → RealTimeSyncAspect → DataChangeEvent →</p>
 * <ul>
 *   <li>MessageCenter：DATA_CHANGE 通知（SSE 即推 + MySQL 持久化）</li>
 *   <li>Watchdog：规则引擎检测 → 可能触发 ALERT</li>
 * </ul>
 *
 * @author istream
 * @since 2026-09-24
 */
public record DataChangeEvent(

        // 实体类型名称（如 "SysOrder"）
        String entityType,

        // 变更实体ID
        Long entityId,

        // 变更类型
        SyncEventType syncEventType,

        // 变更数据摘要（JSON，可选）
        String dataSummary,

        // 操作人ID
        Long operatorId,

        // 租户ID
        Long tenantId,

        // 事件时间
        LocalDateTime eventTime
) {}
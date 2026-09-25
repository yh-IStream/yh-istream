package com.istream.common.event;

import com.istream.common.enums.SyncEventType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 数据变更事件
 *
 * <p>由 {@link com.istream.common.annotation.RealTimeSync} AOP 切面在 Service 方法执行成功后发布。
 * 下游模块（MessageCenter、Watchdog）通过 Spring {@link org.springframework.context.event.EventListener}
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
@Getter
public class DataChangeEvent extends org.springframework.context.ApplicationEvent {

    /** 实体类型名称（如 "SysOrder"） */
    private final String entityType;

    /** 变更实体ID */
    private final Long entityId;

    /** 变更类型 */
    private final SyncEventType syncEventType;

    /** 变更数据摘要（JSON，可选） */
    private final String dataSummary;

    /** 操作人ID */
    private final Long operatorId;

    /** 租户ID */
    private final Long tenantId;

    /** 事件时间 */
    private final LocalDateTime eventTime;

    /**
     * 构造数据变更事件
     *
     * @param source       事件源（通常是 AOP 切面实例）
     * @param entityType   实体类型名称
     * @param entityId     实体ID
     * @param syncEventType 变更类型
     * @param dataSummary  数据摘要
     * @param operatorId   操作人ID
     * @param tenantId     租户ID
     */
    public DataChangeEvent(Object source, String entityType, Long entityId,
                           SyncEventType syncEventType, String dataSummary,
                           Long operatorId, Long tenantId) {
        super(source);
        this.entityType = entityType;
        this.entityId = entityId;
        this.syncEventType = syncEventType;
        this.dataSummary = dataSummary;
        this.operatorId = operatorId;
        this.tenantId = tenantId;
        this.eventTime = LocalDateTime.now();
    }
}
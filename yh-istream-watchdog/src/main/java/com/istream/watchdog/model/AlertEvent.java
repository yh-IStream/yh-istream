package com.istream.watchdog.model;

import com.istream.common.enums.SyncEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警事件
 *
 * <p>Watchdog 规则引擎检测到异常后产生的告警事件，经 {@link com.istream.watchdog.router.AlertRouter}
 * 路由到指定通道推送。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    /** 告警ID（雪花算法） */
    private Long id;

    /** 触发告警的规则名称 */
    private String ruleName;

    /** 告警严重级别（1=低, 2=中, 3=高, 4=紧急） */
    private int severity;

    /** 实体类型（如 "SysOrder"） */
    private String entityType;

    /** 实体ID */
    private Long entityId;

    /** 触发告警的变更类型 */
    private SyncEventType triggerEvent;

    /** 告警标题 */
    private String title;

    /** 告警内容 */
    private String content;

    /** 告警上下文数据（规则评估时的变量值） */
    private Map<String, Object> context;

    /** 接收人用户ID列表 */
    private List<Long> receiverIds;

    /** 指定推送通道（为空时由路由策略决定） */
    private List<String> channels;

    /** 告警时间 */
    private LocalDateTime alertTime;

    /** 确认状态（0=未确认, 1=已确认） */
    private int confirmed;

    /** 确认人ID */
    private Long confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmedTime;

    /** 租户ID */
    private Long tenantId;
}
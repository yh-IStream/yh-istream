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
 * <p>Watchdog 规则引擎检测到异常后产生的告警事件，经 {@link AlertRouter}
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

    /** 严重级别：低 */
    public static final int SEVERITY_LOW = 1;
    /** 严重级别：中 */
    public static final int SEVERITY_MEDIUM = 2;
    /** 严重级别：高 */
    public static final int SEVERITY_HIGH = 3;
    /** 严重级别：紧急 */
    public static final int SEVERITY_CRITICAL = 4;

    /** 告警ID（雪花算法） */
    private Long id;

    /** 触发告警的规则名称 */
    private String ruleName;

    /** 告警严重级别（参见 SEVERITY_* 常量） */
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

    /** 接收人用户ID列表（SSE 通道使用） */
    private List<Long> receiverIds;

    /** 接收人邮箱列表（Email 通道使用） */
    private List<String> receiverEmails;

    /** 指定推送通道（为空时由路由策略决定） */
    private List<String> channels;

    /** 告警时间 */
    private LocalDateTime alertTime;

    /** 确认状态 */
    private Boolean confirmed;

    /** 确认人ID */
    private Long confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmedTime;

    /** 租户ID */
    private Long tenantId;
}
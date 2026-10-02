package com.istream.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 处置建议
 *
 * <p>AI 分析发现异常后生成的处置建议，需人工确认后才执行。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionSuggestion {

    /** 建议ID */
    private Long id;

    /** 关联告警ID */
    private Long alertId;

    /** 实体类型 */
    private String entityType;

    /** 实体ID */
    private Long entityId;

    /** AI 建议的操作（如 "冻结订单"、"通知主管"） */
    private String action;

    /** 操作参数 */
    private Map<String, Object> params;

    /** AI 置信度 */
    private double confidence;

    /** AI 分析结论 */
    private String conclusion;

    /** 建议时间 */
    private LocalDateTime suggestTime;

    /** 确认状态（0=待确认, 1=已确认执行, 2=已拒绝） */
    private int status;

    /** 确认人ID */
    private Long confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmedTime;

    /** 租户ID */
    private Long tenantId;
}
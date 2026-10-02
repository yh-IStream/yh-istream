package com.istream.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 处置建议实体（MySQL 持久化）
 *
 * <p>AI 分析发现异常后生成的处置建议，替代内存 ConcurrentHashMap，
 * 支持重启不丢失、多实例共享、历史追溯。</p>
 *
 * <p>状态流转：0（待确认）→ 1（已执行）或 2（已拒绝）</p>
 *
 * @author istream
 * @since 2026-09-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_suggestion")
public class AiSuggestion extends BaseEntity {

    /** 关联告警ID */
    private Long alertId;

    /** 实体类型 */
    private String entityType;

    /** 实体ID */
    private Long entityId;

    /** AI 建议的操作 */
    private String action;

    /** 操作参数（JSON 字符串） */
    private String params;

    /** AI 置信度 */
    private BigDecimal confidence;

    /** AI 分析结论 */
    private String conclusion;

    /** 建议生成时间 */
    private LocalDateTime suggestTime;

    /** 状态（0=待确认 1=已执行 2=已拒绝） */
    private Integer status;

    /** 确认人ID */
    private Long confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmedTime;
}
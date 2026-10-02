package com.istream.common.event;

import com.istream.common.annotation.AnomalyGuard;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 异常守护事件
 *
 * <p>由 {@link AnomalyGuard} AOP 切面在 AI 分析完成后发布。
 * Watchdog 监听此事件，将 AI 洞察作为告警走多通道推送。</p>
 *
 * <p>事件流：Service 方法执行 → AnomalyGuardAspect → AI 分析 → AnomalyGuardEvent → WatchdogListener → 多通道推送</p>
 *
 * @author istream
 * @since 2026-09-26
 */
public record AnomalyGuardEvent(
        // 实体类型名称（如 "SysOrder"）
        String entityType,

        // 实体ID
        Long entityId,

        // AI 分析提示词
        String prompt,

        // AI 分析结论
        String conclusion,

        // 置信度（0.0 ~ 1.0）
        double confidence,

        // AI 建议的处置操作（如 "冻结订单"、"通知主管"）
        String suggestedAction,

        // AI 建议的处置参数（如 orderId、reason）
        Map<String, Object> actionParams,

        // 分析上下文数据
        Map<String, Object> context,

        // 操作人ID
        Long operatorId,

        // 租户ID
        Long tenantId,

        // 事件时间
        LocalDateTime eventTime
) {}
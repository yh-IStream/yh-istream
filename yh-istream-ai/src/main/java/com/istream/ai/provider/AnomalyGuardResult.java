package com.istream.ai.provider;

import java.util.Map;

/**
 * 异常守护分析结果
 *
 * @author istream
 * @since 2026-09-26
 */
public record AnomalyGuardResult(
        /* 是否发现异常 */
        boolean anomalyDetected,
        /* 分析结论 */
        String conclusion,
        /* 置信度（0.0 ~ 1.0） */
        double confidence,
        /* 建议的处置操作（如 "冻结订单"） */
        String suggestedAction,
        /* 处置操作参数 */
        Map<String, Object> actionParams
) {}
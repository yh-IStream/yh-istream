package com.istream.ai.provider;

import java.util.Map;

/**
 * 异常守护分析请求
 *
 * @author istream
 * @since 2026-09-26
 */
public record AnomalyGuardRequest(
        /* 分析提示词 */
        String prompt,
        /* 实体类型 */
        String entityType,
        /* 实体ID */
        Long entityId,
        /* 变更数据上下文 */
        Map<String, Object> data,
        /* 置信度阈值 */
        double confidenceThreshold
) {}
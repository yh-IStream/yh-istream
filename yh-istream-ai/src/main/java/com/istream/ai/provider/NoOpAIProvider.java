package com.istream.ai.provider;

import com.istream.ai.config.AIProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * NoOp AI 提供者（AI 关闭时的空实现）
 *
 * <p>当 {@code istream.ai.enabled=false} 时使用此实现，
 * JVM 中不注册实际 AI Bean，零开销。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@RequiredArgsConstructor
public class NoOpAIProvider implements AIProvider {

    private final AIProperties properties;

    @Override
    public AnomalyGuardResult analyze(AnomalyGuardRequest request) {
        log.debug("NoOpAIProvider: AI 模块未启用，跳过分析 entityType={}", request.entityType());
        return new AnomalyGuardResult(false, "AI 未启用", 0.0, null, Collections.emptyMap());
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
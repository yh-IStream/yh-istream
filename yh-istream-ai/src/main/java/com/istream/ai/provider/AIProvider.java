package com.istream.ai.provider;

/**
 * AI 提供者接口
 *
 * <p>统一的 AI 分析接口，支持多种后端实现（OpenRouter、Ollama、OpenAI）。
 * 当 {@code istream.ai.enabled=false} 时使用 {@link NoOpAIProvider}，零开销。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
public interface AIProvider {

    /**
     * 分析数据变更，检测未知异常
     *
     * @param request 分析请求
     * @return 分析结果
     */
    AnomalyGuardResult analyze(AnomalyGuardRequest request);

    /**
     * 检查 AI 提供者是否可用
     *
     * @return true 表示可用
     */
    boolean isAvailable();
}
package com.istream.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 模块配置
 *
 * <p>对应 YAML 配置前缀 {@code istream.ai}，支持按需开启/关闭整个 AI 模块。</p>
 *
 * <p>模型连接配置由 Spring AI 2.0 原生管理：</p>
 * <ul>
 *   <li>OpenAI / OpenRouter：{@code spring.ai.openai.base-url} / {@code spring.ai.openai.api-key} / {@code spring.ai.openai.chat.options.model}</li>
 *   <li>Ollama：{@code spring.ai.ollama.base-url} / {@code spring.ai.ollama.chat.options.model}</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>{@code
 * istream:
 *   ai:
 *     enabled: true
 *     provider: openai          # openai / ollama
 *     analyze:
 *       batch-size: 10
 *       confidence-threshold: 0.7
 *     mcp:
 *       enabled: false
 *
 * spring:
 *   ai:
 *     openai:
 *       base-url: https://openrouter.ai/api/v1
 *       api-key: ${AI_OPENROUTER_KEY:}
 *       chat:
 *         options:
 *           model: google/gemma-3-27b-it:free
 *           temperature: 0.3
 * }</pre>
 *
 * @author istream
 * @since 2026-09-24
 */
@Data
@ConfigurationProperties(prefix = "istream.ai")
public class AIProperties {

    /** 是否启用 AI 模块（默认关闭，渐进式架构 L3） */
    private boolean enabled = false;

    /** AI 提供者（openai / ollama），对应 Spring AI 自动配置的 ChatModel */
    private String provider = "openai";

    /** AI 分析配置 */
    private AnalyzeConfig analyze = new AnalyzeConfig();

    /** MCP Server 配置 */
    private McpConfig mcp = new McpConfig();

    @Data
    public static class AnalyzeConfig {
        /** 批量分析大小（攒 N 条调 1 次 LLM） */
        private int batchSize = 10;
        /** 置信度阈值（低于此值不推送，只写日志） */
        private double confidenceThreshold = 0.7;
        /** 分析超时时间（秒） */
        private int timeoutSeconds = 30;
    }

    @Data
    public static class McpConfig {
        /** 是否启用 MCP Server */
        private boolean enabled = false;
        /** MCP Server 名称 */
        private String name = "istream-mcp";
        /** MCP Server 版本 */
        private String version = "1.0.0";
    }
}
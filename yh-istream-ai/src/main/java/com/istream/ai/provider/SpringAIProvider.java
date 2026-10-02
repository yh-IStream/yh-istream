package com.istream.ai.provider;

import com.istream.ai.config.AIProperties;
import com.istream.ai.mcp.ToolScanResult;
import com.istream.ai.mcp.ToolScanResult.ToolDefinition;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring AI 统一提供者
 *
 * <p>基于 Spring AI {@link ChatClient} 统一调用所有模型后端，
 * 支持 OpenAI / OpenRouter / Ollama 等多种模型，切换只需改配置。</p>
 *
 * <p>系统提示词动态注入已注册的 {@code @Tool} 方法签名，
 * AI 可直接返回匹配的工具名和参数，前端卡片展示时无需额外映射。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
public class SpringAIProvider implements AIProvider {

    private final ChatClient chatClient;
    private final AIProperties properties;
    private final List<ToolDefinition> toolDefinitions;

    public SpringAIProvider(ChatClient chatClient, AIProperties properties,
                            List<ToolDefinition> toolDefinitions) {
        this.chatClient = chatClient;
        this.properties = properties;
        this.toolDefinitions = Collections.unmodifiableList(toolDefinitions);
        log.info("SpringAIProvider: 初始化完成，已索引 {} 个 @Tool 方法", toolDefinitions.size());
    }

    @Override
    public AnomalyGuardResult analyze(AnomalyGuardRequest request) {
        if (!isAvailable()) {
            return new AnomalyGuardResult(false, "AI 提供者不可用", 0.0, null, Collections.emptyMap());
        }

        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(request);

            log.debug("SpringAIProvider: 调用 AI 分析 entityType={}, entityId={}", request.entityType(), request.entityId());

            var chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .chatResponse();

            if (chatResponse != null) {
                chatResponse.getResults();
                log.info("SpringAIProvider: ChatResponse results={}, metadata={}",
                        chatResponse.getResults().size(),
                        chatResponse.getMetadata());
            }

            String rawText = null;
            if (chatResponse != null) {
                chatResponse.getResults();
            }
            if (chatResponse != null && !chatResponse.getResults().isEmpty()) {
                var output = chatResponse.getResults().getFirst().getOutput();
                log.info("SpringAIProvider: output type={}, text=[{}], mediaCount={}, toolCallCount={}, toString=[{}]",
                        output.getClass().getSimpleName(),
                        output.getText() != null ? output.getText() : "null",
                        output.getMedia().size(),
                        output.getToolCalls().size(),
                        output.toString().length() > 300 ? output.toString().substring(0, 300) + "..." : output.toString());
                rawText = output.getText();
            }

            if (rawText == null || rawText.isBlank()) {
                log.warn("SpringAIProvider: AI 返回空文本 entityType={}, entityId={}", request.entityType(), request.entityId());
                return new AnomalyGuardResult(false, "AI 分析失败: 模型返回空文本", 0.0, null, Collections.emptyMap());
            }

            log.debug("SpringAIProvider: AI 原始响应长度={}, 前200字符={}",
                    rawText.length(), rawText.length() > 200 ? rawText.substring(0, 200) + "..." : rawText);

            String jsonText = cleanJsonResponse(rawText);
            AnomalyGuardResult result = JSONUtil.toBean(jsonText, AnomalyGuardResult.class);

            if (result == null) {
                return new AnomalyGuardResult(false, "AI 返回空结果", 0.0, null, Collections.emptyMap());
            }

            if (result.anomalyDetected() && result.confidence() < request.confidenceThreshold()) {
                log.info("SpringAIProvider: AI 检测到异常但置信度 {} 低于阈值 {}，仅记录日志",
                        String.format("%.2f", result.confidence()),
                        String.format("%.2f", request.confidenceThreshold()));
                return new AnomalyGuardResult(false, result.conclusion(), result.confidence(),
                        result.suggestedAction(), result.actionParams());
            }

            log.info("SpringAIProvider: AI 分析完成 anomalyDetected={}, confidence={}, action={}",
                    result.anomalyDetected(), String.format("%.2f", result.confidence()),
                    result.suggestedAction());

            return result;

        } catch (Exception e) {
            log.warn("SpringAIProvider: AI 分析失败 entityType={}, entityId={}",
                    request.entityType(), request.entityId(), e);
            return new AnomalyGuardResult(false, "AI 分析失败: " + e.getMessage(), 0.0, null, Collections.emptyMap());
        }
    }

    @Override
    public boolean isAvailable() {
        return properties.isEnabled();
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                你是 iStream 智能守护的 AI 分析引擎。你的任务是分析业务数据变更，检测可能的异常。

                分析要求：
                1. 基于提供的提示词和数据上下文判断是否存在异常
                2. 如果发现异常，必须从下面的"可用工具"列表中选择最合适的 suggestedAction
                3. 置信度规则：90%+ 为严重异常，70%-90% 为可疑，50%-70% 为轻微，50% 以下为正常
                4. 如果未发现异常，anomalyDetected=false，confidence=0.0，suggestedAction 填空字符串

                可用工具：
                """);

        if (toolDefinitions.isEmpty()) {
            sb.append("（无已注册工具，请使用 istream.ai.enabled=true 并创建 @Tool 方法）\n");
        } else {
            for (ToolDefinition def : toolDefinitions) {
                sb.append(String.format("- **%s**: %s\n", def.method().getName(), def.description()));
                sb.append("  参数: ");
                Parameter[] params = def.method().getParameters();
                if (params.length == 0) {
                    sb.append("无");
                } else {
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(params[i].getName()).append("(").append(params[i].getType().getSimpleName()).append(")");
                    }
                }
                sb.append("\n");
            }
        }

        sb.append("""

                请严格以 JSON 格式返回，不要包含 markdown 代码块标记：
                {"anomalyDetected": true/false, "conclusion": "详细分析结论", "confidence": 0.85, "suggestedAction": "工具方法名", "actionParams": {"param1": "value1"}}
                """);

        return sb.toString();
    }

    private String buildUserPrompt(AnomalyGuardRequest request) {
        return String.format("""
                分析提示词：%s
                实体类型：%s
                实体ID：%s
                数据上下文：%s
                置信度阈值：%.1f
                """, request.prompt(), request.entityType(), request.entityId(),
                request.data(), request.confidenceThreshold());
    }

    /**
     * 清洗 AI 响应的 JSON 文本
     *
     * <p>免费小模型经常在 JSON 外包 markdown fence（```json ... ```）或夹带思考过程。
     * 此方法提取花括号包裹的纯 JSON 部分，确保可被 Jackson 反序列化。</p>
     */
    private String cleanJsonResponse(String rawText) {
        String trimmed = rawText.trim();

        int braceStart = trimmed.indexOf('{');
        int braceEnd = trimmed.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            String extracted = trimmed.substring(braceStart, braceEnd + 1);
            log.debug("SpringAIProvider: JSON 清洗完成，从长度 {} 提取到 {} 字符", trimmed.length(), extracted.length());
            return extracted;
        }

        log.warn("SpringAIProvider: 无法从响应中提取 JSON，返回原文: {}", trimmed.length() > 300 ? trimmed.substring(0, 300) + "..." : trimmed);
        return trimmed;
    }
}
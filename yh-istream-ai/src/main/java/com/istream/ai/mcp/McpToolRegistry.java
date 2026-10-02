package com.istream.ai.mcp;

import com.istream.ai.model.ActionSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具执行器
 *
 * <p>基于 Spring AI {@code @Tool} 注解，职责单一：</p>
 * <ul>
 *   <li>MCP Server 注册 → 由 Spring AI {@code ToolCallbackProvider} 自动完成，本类不参与</li>
 *   <li>人工确认后执行 → 本类唯一职责：找到 @Tool 方法并反射调用</li>
 * </ul>
 *
 * <p>工具定义由 {@link ToolScanResult} 在启动时一次性扫描提供，
 * 本类不再自行扫描，消除与 {@code ToolCallbackProvider} 的重复扫描。</p>
 *
 * <p>安全红线：所有 AI 生成的处置建议必须经 {@link ActionSuggestionManager#confirm} 人工确认后才执行，
 * 这是全局策略，不依赖任何注解属性。</p>
 *
 * @author istream
 * @since 2026-09-25
 */
@Slf4j
public class McpToolRegistry {

    private final ApplicationContext applicationContext;
    private final ActionSuggestionManager suggestionManager;
    private final List<ToolScanResult.ToolDefinition> toolDefinitions;

    public McpToolRegistry(ApplicationContext applicationContext,
                          ActionSuggestionManager suggestionManager,
                          List<ToolScanResult.ToolDefinition> toolDefinitions) {
        this.applicationContext = applicationContext;
        this.suggestionManager = suggestionManager;
        this.toolDefinitions = Collections.unmodifiableList(toolDefinitions);
        log.info("McpToolRegistry: 初始化完成，索引 {} 个 @Tool 方法", toolDefinitions.size());
    }

    /**
     * 获取所有工具定义（API 友好格式，不含反射 Method 对象）
     */
    public List<ToolInfo> getToolDefinitions() {
        return toolDefinitions.stream()
                .map(d -> new ToolInfo(d.beanName(), d.method().getName(), d.description()))
                .toList();
    }

    /**
     * 人工确认后执行工具方法
     *
     * @param suggestionId 处置建议 ID
     * @param confirmedBy  确认人 ID
     * @return 执行结果
     */
    public Object executeConfirmed(Long suggestionId, Long confirmedBy) {
        ActionSuggestion suggestion = suggestionManager.confirm(suggestionId, confirmedBy);
        if (suggestion == null || suggestion.getStatus() != 1) {
            log.warn("McpToolRegistry: 处置建议未确认或不存在 id={}", suggestionId);
            return null;
        }

        String action = suggestion.getAction();
        Map<String, Object> params = suggestion.getParams();

        for (ToolScanResult.ToolDefinition definition : toolDefinitions) {
            if (definition.method().getName().equals(action)) {
                try {
                    Object bean = applicationContext.getBean(definition.beanName());
                    Object[] args = buildArgs(definition.method(), params);
                    Object result = definition.method().invoke(bean, args);
                    log.info("McpToolRegistry: 工具执行成功 action={}, confirmedBy={}", action, confirmedBy);
                    return result;
                } catch (Exception e) {
                    log.error("McpToolRegistry: 工具执行失败 action={}", action, e);
                    return "执行失败: " + e.getMessage();
                }
            }
        }

        log.warn("McpToolRegistry: 未找到匹配的 @Tool 方法 action={}", action);
        return null;
    }

    private static final DefaultConversionService conversionService = new DefaultConversionService();

    private Object[] buildArgs(Method method, Map<String, Object> params) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            Class<?> paramType = parameters[i].getType();
            if (params != null && params.containsKey(paramName)) {
                Object rawValue = params.get(paramName);
                args[i] = convertArg(rawValue, paramType, paramName);
            }
        }
        return args;
    }

    private Object convertArg(Object rawValue, Class<?> targetType, String paramName) {
        if (rawValue == null || targetType.isInstance(rawValue)) {
            return rawValue;
        }
        if (conversionService.canConvert(rawValue.getClass(), targetType)) {
            return conversionService.convert(rawValue, targetType);
        }
        log.warn("McpToolRegistry: 无法转换参数类型 param={}, from={}, to={}", paramName,
                rawValue.getClass().getSimpleName(), targetType.getSimpleName());
        return rawValue;
    }

    /**
     * 工具信息（API 响应格式）
     *
     * @param beanName   Spring Bean 名称
     * @param methodName 方法名称
     * @param description 工具描述
     */
    public record ToolInfo(String beanName, String methodName, String description) {
    }
}
package com.istream.ai.mcp;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MCP 工具扫描结果
 *
 * <p>启动时一次性扫描所有 {@code @Tool} 注解方法，供 {@link McpToolRegistry} 和
 * Spring AI {@code ToolCallbackProvider} 共享，避免重复扫描。</p>
 *
 * @author istream
 * @since 2026-09-25
 */
public record ToolScanResult(
        Object[] toolObjects,
        List<ToolDefinition> toolDefinitions
) {

    /**
     * 工具定义记录
     *
     * @param beanName   Spring Bean 名称
     * @param method     @Tool 注解方法（用于反射调用）
     * @param description 工具描述
     */
    public record ToolDefinition(String beanName, Method method, String description) {
    }
}
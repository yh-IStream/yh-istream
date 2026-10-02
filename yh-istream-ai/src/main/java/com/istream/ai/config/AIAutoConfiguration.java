package com.istream.ai.config;

import com.istream.ai.aspect.AnomalyGuardAspect;
import com.istream.ai.mcp.ActionSuggestionManager;
import com.istream.ai.mcp.McpToolRegistry;
import com.istream.ai.mcp.ToolScanResult;
import com.istream.ai.provider.AIProvider;
import com.istream.ai.provider.NoOpAIProvider;
import com.istream.ai.provider.SpringAIProvider;
import com.istream.ai.mapper.AiSuggestionMapper;
import com.istream.framework.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 模块自动配置
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时注册相关 Bean，
 * 关闭时仅注册 {@link NoOpAIProvider}，零开销（渐进式架构 L3 可拆卸）。</p>
 *
 * <p>基于 Spring AI 2.0 {@link ChatClient} 统一调用所有模型后端，
 * 模型切换只需改 {@code spring.ai.openai} 或 {@code spring.ai.ollama} 配置，零代码改动。</p>
 *
 * <p>MCP 工具扫描由 {@link #toolScanResult} 统一完成，
 * {@link McpToolRegistry} 和 {@code ToolCallbackProvider} 共享扫描结果，避免重复扫描。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AIProperties.class)
public class AIAutoConfiguration {

    /**
     * AI 提供者（单一入口）
     *
     * <p>使用 {@link ObjectProvider} 懒加载 {@link ChatModel}，消除与 Spring AI
     * 自动配置之间的时序依赖。ChatModel 已就绪则走真实 AI，否则自动降级 NoOp。</p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
    public AIProvider aiProvider(AIProperties properties,
                                  ObjectProvider<ChatModel> chatModelProvider,
                                  ObjectProvider<ToolScanResult> toolScanResultProvider) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            log.warn("AIAutoConfiguration: 未检测到 ChatModel，使用 NoOp 降级提供者");
            return new NoOpAIProvider(properties);
        }

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是 iStream 智能守护的 AI 分析引擎。")
                .build();

        List<ToolScanResult.ToolDefinition> definitions = toolScanResultProvider
                .getIfAvailable() != null ? toolScanResultProvider.getIfAvailable().toolDefinitions()
                : Collections.emptyList();
        log.info("AIAutoConfiguration: 注册 SpringAIProvider，provider={}，工具索引={}",
                properties.getProvider(), definitions.size());
        return new SpringAIProvider(chatClient, properties, definitions);
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
    public AnomalyGuardAspect anomalyGuardAspect(AIProvider aiProvider, AIProperties properties,
                                                 ApplicationEventPublisher eventPublisher,
                                                 ObjectProvider<SseService> sseServiceProvider) {
        log.info("AIAutoConfiguration: 注册 AnomalyGuardAspect");
        return new AnomalyGuardAspect(aiProvider, properties, eventPublisher, sseServiceProvider);
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
    public ActionSuggestionManager actionSuggestionManager(AiSuggestionMapper aiSuggestionMapper) {
        return new ActionSuggestionManager(aiSuggestionMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
    public ToolScanResult toolScanResult(ApplicationContext applicationContext) {
        List<Object> toolObjectList = new ArrayList<>();
        List<ToolScanResult.ToolDefinition> definitions = new ArrayList<>();

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                boolean hasTool = false;
                for (Method method : bean.getClass().getDeclaredMethods()) {
                    Tool annotation = method.getAnnotation(Tool.class);
                    if (annotation != null) {
                        definitions.add(new ToolScanResult.ToolDefinition(beanName, method, annotation.description()));
                        hasTool = true;
                    }
                }
                if (hasTool) {
                    toolObjectList.add(bean);
                }
            } catch (Exception ignored) {
            }
        }

        log.info("AIAutoConfiguration: 扫描到 {} 个 @Tool 方法，{} 个工具组件",
                definitions.size(), toolObjectList.size());

        return new ToolScanResult(toolObjectList.toArray(), definitions);
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.ai.mcp", name = "enabled", havingValue = "true")
    public ToolCallbackProvider istreamToolCallbackProvider(ToolScanResult toolScanResult) {
        if (toolScanResult.toolObjects().length == 0) {
            log.info("AIAutoConfiguration: 未发现 @Tool 注解方法，MCP Server 注册空工具集");
        } else {
            log.info("AIAutoConfiguration: 注册 {} 个工具组件为 MCP 工具", toolScanResult.toolObjects().length);
        }
        return MethodToolCallbackProvider.builder().toolObjects(toolScanResult.toolObjects()).build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
    public McpToolRegistry mcpToolRegistry(ApplicationContext applicationContext,
                                          AIProperties properties,
                                          ActionSuggestionManager suggestionManager,
                                          ObjectProvider<ToolScanResult> toolScanResultProvider) {
        ToolScanResult scanResult = toolScanResultProvider.getIfAvailable();
        List<ToolScanResult.ToolDefinition> definitions = scanResult != null
                ? scanResult.toolDefinitions()
                : Collections.emptyList();

        if (scanResult == null) {
            log.info("AIAutoConfiguration: MCP Server 未启用，McpToolRegistry 索引空工具集");
        }

        return new McpToolRegistry(applicationContext, suggestionManager, definitions);
    }
}
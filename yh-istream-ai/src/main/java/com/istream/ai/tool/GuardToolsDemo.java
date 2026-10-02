package com.istream.ai.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 异常守护演示工具集（执行器）
 *
 * <p>定义 AI / 人工可调用的处置操作。所有方法均标注 {@link Tool} 注解，
 * 启动时由 {@code ToolScanResult} 自动扫描并注册到 {@code McpToolRegistry}。</p>
 *
 * <p>安全红线：所有处置操作必须经人工确认后才执行，
 * 入口为 {@code AISuggestionController.confirm()} → {@code McpToolRegistry.executeConfirmed()}。</p>
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时注册，AI 关闭时零开销。</p>
 *
 * <p>演示数据：</p>
 * <pre>{@code
 *   POST /ai/suggestion/pending           → 查看待确认的 AI 建议
 *   POST /ai/suggestion/{id}/confirm      → 人工确认后执行对应 @Tool 方法
 *   GET  /ai/suggestion/tools             → 查看所有可用工具
 * }</pre>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class GuardToolsDemo {

    /**
     * 模拟冻结记录：entityType + entityId → 冻结状态
     */
    private final Map<String, Boolean> frozenRegistry = new ConcurrentHashMap<>();

    /**
     * 冻结异常实体，阻止继续操作
     *
     * <p>AI 发现异常后建议此操作，人工确认后实际执行。
     * 示例：检测到订单金额异常 → AI 建议 freezeEntity(123, "Order", "单笔金额超均值3倍")
     *       → 管理员在前端点击确认 → 本方法被执行。</p>
     *
     * @param entityId   实体ID
     * @param entityType 实体类型（如 "Order"、"User"）
     * @param reason     冻结原因（AI 分析结论）
     * @return 操作结果描述
     */
    @Tool(description = "冻结异常实体，阻止继续操作。需要提供实体ID、实体类型和冻结原因")
    public String freezeEntity(Long entityId, String entityType, String reason) {
        String key = entityType + ":" + entityId;
        frozenRegistry.put(key, true);

        String result = String.format("实体 %s/%s 已于 %s 冻结，原因：%s",
                entityType, entityId, LocalDateTime.now(), reason);
        log.warn("GuardToolsDemo: [处置] {}", result);
        return result;
    }

    /**
     * 解除实体冻结状态
     *
     * @param entityId   实体ID
     * @param entityType 实体类型
     * @return 操作结果描述
     */
    @Tool(description = "解除实体的冻结状态，恢复正常操作")
    public String unfreezeEntity(Long entityId, String entityType) {
        String key = entityType + ":" + entityId;
        frozenRegistry.remove(key);

        String result = String.format("实体 %s/%s 已于 %s 解冻，恢复正常操作",
                entityType, entityId, LocalDateTime.now());
        log.warn("GuardToolsDemo: [处置] {}", result);
        return result;
    }

    /**
     * 发送紧急通知给相关负责人
     *
     * <p>AI 发现高风险异常时建议此操作，人工确认后实际发送。
     * 当前为演示实现，仅记录日志；生产环境可接入邮件 / 企业微信 / 短信等通道。</p>
     *
     * @param entityId   实体ID
     * @param entityType 实体类型
     * @param message    通知内容（AI 分析结论）
     * @return 操作结果描述
     */
    @Tool(description = "发送紧急通知给相关负责人，需要提供实体ID、实体类型和通知内容")
    public String escalateToManager(Long entityId, String entityType, String message) {
        String result = String.format("紧急通知已发送（模拟）：实体 %s/%s，内容：%s，时间：%s",
                entityType, entityId, message, LocalDateTime.now());
        log.error("GuardToolsDemo: [处置-紧急升级] {}", result);
        return result;
    }

    /**
     * 标记实体的异常状态为"已确认，无需处置"
     *
     * <p>适用于 AI 误报或人工判定无需处理的场景。</p>
     *
     * @param entityId   实体ID
     * @param entityType 实体类型
     * @param remark     人工备注
     * @return 操作结果描述
     */
    @Tool(description = "标记异常为误报或无需处置，需要提供实体ID、实体类型和备注说明")
    public String markAsFalseAlarm(Long entityId, String entityType, String remark) {
        String result = String.format("实体 %s/%s 已于 %s 标记为误报，管理员备注：%s",
                entityType, entityId, LocalDateTime.now(), remark);
        log.warn("GuardToolsDemo: [处置] {}", result);
        return result;
    }
}
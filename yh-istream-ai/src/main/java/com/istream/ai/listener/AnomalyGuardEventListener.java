package com.istream.ai.listener;

import com.istream.ai.entity.AiAlertRecipient;
import com.istream.ai.mapper.AiAlertRecipientMapper;
import com.istream.ai.mcp.ActionSuggestionManager;
import com.istream.common.event.AnomalyGuardEvent;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 异常守护事件监听器
 *
 * <p>监听 {@link AnomalyGuardEvent}，将 AI 分析结果中的建议操作
 * 创建为 {@code ActionSuggestion}，并通过 SSE 实时推送到所有已配置的告警接收人。</p>
 *
 * <p>推送策略：</p>
 * <ul>
 *   <li>优先推送到 sys_ai_alert_recipient 表中已启用的接收人</li>
 *   <li>同时推送到触发操作的操作人（兜底）</li>
 *   <li>自动去重，避免同一人收到多条通知</li>
 * </ul>
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时注册。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class AnomalyGuardEventListener {

    private final ActionSuggestionManager suggestionManager;
    private final ObjectProvider<SseService> sseServiceProvider;
    private final AiAlertRecipientMapper recipientMapper;

    public AnomalyGuardEventListener(ActionSuggestionManager suggestionManager,
                                     ObjectProvider<SseService> sseServiceProvider,
                                     AiAlertRecipientMapper recipientMapper) {
        this.suggestionManager = suggestionManager;
        this.sseServiceProvider = sseServiceProvider;
        this.recipientMapper = recipientMapper;
    }

    /**
     * AI 发现异常后，创建处置建议并推送 SSE 通知
     *
     * <p>异步执行，不阻塞主业务流。</p>
     *
     * @param event 异常守护事件
     */
    @Async("asyncExecutor")
    @EventListener
    public void onAnomalyGuard(AnomalyGuardEvent event) {
        if (event.suggestedAction() == null || event.suggestedAction().isBlank()) {
            log.debug("AnomalyGuardEventListener: AI 未建议处置操作，跳过 entityType={}/{}",
                    event.entityType(), event.entityId());
            return;
        }

        try {
            suggestionManager.create(
                    event.entityId(),
                    event.entityType(),
                    event.entityId(),
                    event.suggestedAction(),
                    event.actionParams(),
                    event.confidence(),
                    event.conclusion(),
                    event.tenantId()
            );

            log.info("AnomalyGuardEventListener: 创建处置建议 action={}, entityType={}/{}, confidence={}",
                    event.suggestedAction(), event.entityType(), event.entityId(),
                    String.format("%.2f", event.confidence()));
        } catch (Exception e) {
            log.warn("AnomalyGuardEventListener: 创建处置建议失败 entityType={}/{}",
                    event.entityType(), event.entityId(), e);
        }

        pushSseToAllRecipients(event);
    }

    /**
     * 推送 SSE 通知到所有告警接收人
     *
     * <p>汇聚操作人 + 已配置接收人，去重后逐人推送。</p>
     */
    private void pushSseToAllRecipients(AnomalyGuardEvent event) {
        SseService sseService = sseServiceProvider.getIfAvailable();
        if (sseService == null) {
            return;
        }

        Set<Long> userIds = new HashSet<>();

        if (event.operatorId() != null) {
            userIds.add(event.operatorId());
        }

        try {
            List<AiAlertRecipient> recipients = recipientMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiAlertRecipient>()
                            .eq(AiAlertRecipient::getEnabled, 1));
            if (recipients != null) {
                for (AiAlertRecipient r : recipients) {
                    userIds.add(r.getUserId());
                }
            }
        } catch (Exception e) {
            log.warn("AnomalyGuardEventListener: 查询告警接收人失败", e);
        }

        if (userIds.isEmpty()) {
            log.debug("AnomalyGuardEventListener: 无有效接收人，跳过 SSE 推送");
            return;
        }

        SseEvent sseEvent = SseEvent.of("ai_insight", Map.of(
                "title", "AI 异常检测: " + event.entityType(),
                "content", event.conclusion(),
                "eventType", "AI_INSIGHT",
                "entityType", event.entityType(),
                "entityId", event.entityId(),
                "suggestedAction", event.suggestedAction(),
                "confidence", String.format("%.0f%%", event.confidence() * 100)
        ));

        int pushed = 0;
        for (Long userId : userIds) {
            try {
                sseService.sendToUser(userId, sseEvent);
                pushed++;
            } catch (Exception e) {
                log.warn("AnomalyGuardEventListener: SSE 推送失败 userId={}", userId, e);
            }
        }

        log.info("AnomalyGuardEventListener: SSE 通知已推送到 {} 个接收人 entityType={}/{}",
                pushed, event.entityType(), event.entityId());
    }
}
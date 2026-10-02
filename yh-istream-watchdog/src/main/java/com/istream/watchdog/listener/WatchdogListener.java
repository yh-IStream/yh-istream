package com.istream.watchdog.listener;

import com.istream.common.enums.SyncEventType;
import com.istream.common.event.AnomalyGuardEvent;
import com.istream.common.event.DataChangeEvent;
import com.istream.watchdog.aggregator.AlertAggregator;
import com.istream.watchdog.config.WatchdogProperties;
import com.istream.watchdog.engine.RuleEngine;
import com.istream.watchdog.model.AlertEvent;
import com.istream.watchdog.model.AlertRule;
import com.istream.watchdog.router.AlertRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Watchdog 核心监听器
 *
 * <p>监听 {@link DataChangeEvent} 和 {@link AnomalyGuardEvent}，经规则引擎评估后产生告警，
 * 经聚合器去重后由路由器推送到指定通道。</p>
 *
 * <p>事件流：</p>
 * <ul>
 *   <li>DataChangeEvent → RuleEngine.evaluate() → AlertAggregator.shouldEmit() → AlertRouter.route()</li>
 *   <li>AnomalyGuardEvent → 构建 AI 洞察告警 → AlertAggregator.shouldEmit() → AlertRouter.route()</li>
 * </ul>
 *
 * <p>异步执行，不阻塞主业务流。Watchdog 关闭时此监听器不注册（零开销）。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.watchdog", name = "enabled", havingValue = "true")
public class WatchdogListener {

    private final RuleEngine ruleEngine;
    private final AlertAggregator alertAggregator;
    private final AlertRouter alertRouter;
    private final WatchdogProperties properties;
    private final RuleProvider ruleProvider;

    /**
     * 处理数据变更事件
     *
     * @param event 数据变更事件
     */
    @Async("asyncExecutor")
    @EventListener
    public void onDataChange(DataChangeEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            List<AlertRule> rules = ruleProvider.getRules(event.entityType());
            if (rules.isEmpty()) {
                log.debug("WatchdogListener: 实体 {} 无匹配规则，跳过", event.entityType());
                return;
            }

            List<AlertEvent> alerts = ruleEngine.evaluate(rules, event, event.dataSummary());

            for (AlertEvent alert : alerts) {
                if (alertAggregator.shouldEmit(alert)) {
                    alertRouter.route(alert);
                    log.info("WatchdogListener: 告警已推送 rule={}, severity={}, entityType={}/{}",
                            alert.getRuleName(), alert.getSeverity(),
                            alert.getEntityType(), alert.getEntityId());
                }
            }
        } catch (Exception e) {
            log.warn("WatchdogListener: 处理数据变更事件失败 entityType={}, entityId={}",
                    event.entityType(), event.entityId(), e);
        }
    }

    /**
     * 处理异常守护事件（AI 洞察）
     *
     * <p>AI 发现未知异常后，构建 AI 洞察告警走 Watchdog 管道推送。
     * AI 洞察告警的严重级别由置信度决定：≥0.9 紧急，≥0.7 高，≥0.5 中，其余低。</p>
     *
     * @param event 异常守护事件
     */
    @Async("asyncExecutor")
    @EventListener
    public void onAnomalyGuard(AnomalyGuardEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            int severity = resolveSeverity(event.confidence());

            AlertEvent alert = AlertEvent.builder()
                    .id(System.currentTimeMillis())
                    .ruleName("AI_INSIGHT:" + event.entityType())
                    .severity(severity)
                    .entityType(event.entityType())
                    .entityId(event.entityId())
                    .triggerEvent(SyncEventType.UPDATE)
                    .title("AI 洞察：" + event.entityType())
                    .content(event.conclusion())
                    .context(event.context())
                    .alertTime(LocalDateTime.now())
                    .confirmed(0)
                    .tenantId(event.tenantId())
                    .build();

            if (alertAggregator.shouldEmit(alert)) {
                alertRouter.route(alert);
                log.info("WatchdogListener: AI 洞察告警已推送 entityType={}/{}, confidence={}, action={}",
                        event.entityType(), event.entityId(),
                        String.format("%.2f", event.confidence()), event.suggestedAction());
            }
        } catch (Exception e) {
            log.warn("WatchdogListener: 处理异常守护事件失败 entityType={}, entityId={}",
                    event.entityType(), event.entityId(), e);
        }
    }

    private int resolveSeverity(double confidence) {
        if (confidence >= 0.9) {
            return 4;
        }
        if (confidence >= 0.7) {
            return 3;
        }
        if (confidence >= 0.5) {
            return 2;
        }
        return 1;
    }
}
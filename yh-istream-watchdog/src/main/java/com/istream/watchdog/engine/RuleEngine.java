package com.istream.watchdog.engine;

import com.istream.common.enums.SyncEventType;
import com.istream.common.event.DataChangeEvent;
import com.istream.watchdog.model.AlertEvent;
import com.istream.watchdog.model.AlertRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎
 *
 * <p>评估告警规则，当数据变更事件匹配规则条件时产生 {@link AlertEvent}。</p>
 *
 * <h3>评估流程</h3>
 * <ol>
 *   <li>过滤：规则绑定的实体类型与事件实体类型匹配，且变更类型在 onEvents 中</li>
 *   <li>求值：使用 SpEL 表达式评估条件</li>
 *   <li>产出：条件为 true 时构建 AlertEvent</li>
 * </ol>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Component
public class RuleEngine {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 评估所有规则
     *
     * @param rules  告警规则列表
     * @param event  数据变更事件
     * @param data   变更实体数据（供 SpEL 表达式引用）
     * @return 产生的告警事件列表（可能为空）
     */
    public List<AlertEvent> evaluate(List<AlertRule> rules, DataChangeEvent event, Object data) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        List<AlertEvent> alerts = new ArrayList<>();
        for (AlertRule rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }

            if (!matchesEntity(rule, event)) {
                continue;
            }

            if (!matchesEvent(rule, event)) {
                continue;
            }

            if (evaluateCondition(rule, event, data)) {
                alerts.add(buildAlert(rule, event, data));
                log.info("RuleEngine: 规则 [{}] 命中 entityType={}, entityId={}",
                        rule.getName(), event.getEntityType(), event.getEntityId());
            }
        }

        return alerts;
    }

    private boolean matchesEntity(AlertRule rule, DataChangeEvent event) {
        return rule.getEntity().equals(event.getEntityType());
    }

    private boolean matchesEvent(AlertRule rule, DataChangeEvent event) {
        if (rule.getOnEvents() == null || rule.getOnEvents().isEmpty()) {
            return true;
        }
        return rule.getOnEvents().contains(event.getSyncEventType().name());
    }

    private boolean evaluateCondition(AlertRule rule, DataChangeEvent event, Object data) {
        if (rule.getCondition() == null || rule.getCondition().isBlank()) {
            return true;
        }

        try {
            EvaluationContext context = new StandardEvaluationContext();
            context.setVariable("data", data);
            context.setVariable("entityType", event.getEntityType());
            context.setVariable("entityId", event.getEntityId());
            context.setVariable("event", event.getSyncEventType().name());

            return Boolean.TRUE.equals(parser.parseExpression(rule.getCondition()).getValue(context, Boolean.class));
        } catch (Exception e) {
            log.warn("RuleEngine: SpEL 表达式求值失败 rule={}, condition={}", rule.getName(), rule.getCondition(), e);
            return false;
        }
    }

    private AlertEvent buildAlert(AlertRule rule, DataChangeEvent event, Object data) {
        return AlertEvent.builder()
                .ruleName(rule.getName())
                .severity(rule.getSeverity())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .triggerEvent(event.getSyncEventType())
                .title(resolveTemplate(rule.getTitle(), data))
                .content(resolveTemplate(rule.getContent(), data))
                .receiverIds(rule.getReceivers())
                .channels(rule.getChannels())
                .alertTime(LocalDateTime.now())
                .confirmed(0)
                .tenantId(event.getTenantId())
                .build();
    }

    /**
     * 解析模板中的 {{变量}} 占位符
     *
     * @param template 模板字符串
     * @param data     数据对象
     * @return 解析后的字符串
     */
    private String resolveTemplate(String template, Object data) {
        if (template == null || data == null) {
            return template;
        }

        String result = template;
        if (data instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
        }
        return result;
    }
}
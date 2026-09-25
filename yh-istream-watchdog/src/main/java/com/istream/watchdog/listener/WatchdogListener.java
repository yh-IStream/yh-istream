package com.istream.watchdog.listener;

import com.istream.common.event.DataChangeEvent;
import com.istream.watchdog.aggregator.AlertAggregator;
import com.istream.watchdog.config.WatchdogProperties;
import com.istream.watchdog.engine.RuleEngine;
import com.istream.watchdog.model.AlertEvent;
import com.istream.watchdog.model.AlertRule;
import com.istream.watchdog.router.AlertRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Watchdog 核心监听器
 *
 * <p>监听 {@link DataChangeEvent}，经规则引擎评估后产生告警，
 * 经聚合器去重后由路由器推送到指定通道。</p>
 *
 * <p>事件流：DataChangeEvent → RuleEngine.evaluate() → AlertAggregator.shouldEmit() → AlertRouter.route()</p>
 *
 * <p>异步执行，不阻塞主业务流。Watchdog 关闭时此监听器不注册（零开销）。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
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
    @Async
    @EventListener
    public void onDataChange(DataChangeEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            List<AlertRule> rules = ruleProvider.getRules(event.getEntityType());
            if (rules.isEmpty()) {
                log.debug("WatchdogListener: 实体 {} 无匹配规则，跳过", event.getEntityType());
                return;
            }

            List<AlertEvent> alerts = ruleEngine.evaluate(rules, event, null);

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
                    event.getEntityType(), event.getEntityId(), e);
        }
    }
}
package com.istream.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.istream.common.enums.SyncEventType;
import com.istream.common.event.AnomalyGuardEvent;
import com.istream.common.model.R;
import com.istream.framework.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * AI 闭环演示控制器（仅供 demo）
 *
 * <p>提供模拟触发器，无需真实 AI 也能跑通完整闭环：</p>
 * <ol>
 *   <li>本控制器发布 {@link AnomalyGuardEvent}</li>
 *   <li>{@code AnomalyGuardEventListener} 创建 {@code ActionSuggestion}</li>
 *   <li>前端 AI 建议面板展示卡片</li>
 *   <li>人工确认 → {@code McpToolRegistry.executeConfirmed()} 调用 {@code GuardToolsDemo} 方法</li>
 * </ol>
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时注册。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@RestController
@RequestMapping("/ai/demo")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class DemoTriggerController {

    private final ApplicationEventPublisher eventPublisher;

    private final Random random = new Random();

    private static final List<DemoScenario> SCENARIOS = List.of(
            new DemoScenario(
                    "freezeEntity",
                    "检测到异常订单：单笔金额 ¥89,999 超过近30日均值 ¥8,500 的 10.6 倍，且收货地址为新地址",
                    0.92,
                    Map.of("entityId", 2024001L, "entityType", "Order", "reason", "单笔金额超均值10倍")
            ),
            new DemoScenario(
                    "escalateToManager",
                    "检测到高危操作：用户 admin 在 5 分钟内尝试删除 3 张核心配置表，疑似误操作或权限泄露",
                    0.88,
                    Map.of("entityId", 1001L, "entityType", "SysConfig", "message", "用户 admin 5分钟内删除3张配置表，立即核实")
            ),
            new DemoScenario(
                    "freezeEntity",
                    "检测到异常登录：用户 user_38291 在 10 分钟内从 3 个不同城市（北京、深圳、成都）登录",
                    0.85,
                    Map.of("entityId", 38291L, "entityType", "User", "reason", "异地多登，疑似账号被盗")
            ),
            new DemoScenario(
                    "markAsFalseAlarm",
                    "检测到金额波动：订单金额为正常促销价格 ¥1.99，非异常",
                    0.35,
                    Map.of("entityId", 2024050L, "entityType", "Order", "remark", "促销价正常波动")
            ),
            new DemoScenario(
                    "escalateToManager",
                    "检测到数据泄露风险：用户导出操作日志中包含 500+ 条客户手机号，超出日常均值 50 条的 10 倍",
                    0.95,
                    Map.of("entityId", 2001L, "entityType", "OperLog", "message", "用户导出500+条客户敏感数据，疑似数据泄露")
            )
    );

    /**
     * 触发一次随机 AI 检测演示
     *
     * <p>从预设场景中随机选取一条，发布 {@link AnomalyGuardEvent}，
     * 经 {@code AnomalyGuardEventListener} 创建处置建议，前端即可看到新卡片。</p>
     */
    @SaCheckLogin
    @PostMapping("/trigger")
    public R<Map<String, Object>> triggerRandom() {
        DemoScenario scenario = SCENARIOS.get(random.nextInt(SCENARIOS.size()));

        AnomalyGuardEvent event = buildEvent(scenario);
        eventPublisher.publishEvent(event);

        Map<String, Object> result = new HashMap<>();
        result.put("action", scenario.action);
        result.put("conclusion", scenario.conclusion);
        result.put("confidence", scenario.confidence);
        result.put("entityType", scenario.params.get("entityType"));
        result.put("entityId", scenario.params.get("entityId"));
        result.put("timestamp", LocalDateTime.now().toString());

        log.info("DemoTriggerController: 发布模拟异常检测 action={}, confidence={}",
                scenario.action, String.format("%.2f", scenario.confidence));

        return R.ok(result);
    }

    /**
     * 连续触发多条模拟检测（一次性展示多种场景）
     */
    @SaCheckLogin
    @PostMapping("/trigger-batch")
    public R<List<Map<String, Object>>> triggerBatch() {
        List<DemoScenario> batch = SCENARIOS.size() <= 3
                ? SCENARIOS
                : List.of(
                        SCENARIOS.get(0),  // freezeEntity - 订单异常
                        SCENARIOS.get(1),  // escalateToManager - 高危操作
                        SCENARIOS.get(3)   // markAsFalseAlarm - 促销正常
                );

        List<Map<String, Object>> results = batch.stream().map(scenario -> {
            AnomalyGuardEvent event = buildEvent(scenario);
            eventPublisher.publishEvent(event);
            Map<String, Object> r = new HashMap<>();
            r.put("action", scenario.action);
            r.put("conclusion", scenario.conclusion);
            r.put("confidence", scenario.confidence);
            return r;
        }).toList();

        log.info("DemoTriggerController: 批量发布 {} 条模拟检测", results.size());
        return R.ok(results);
    }

    private AnomalyGuardEvent buildEvent(DemoScenario scenario) {
        Map<String, Object> params = new HashMap<>(scenario.params);
        return new AnomalyGuardEvent(
                String.valueOf(params.getOrDefault("entityType", "Unknown")),
                (Long) params.getOrDefault("entityId", 0L),
                "演示提示词：" + scenario.conclusion,
                scenario.conclusion,
                scenario.confidence,
                scenario.action,
                params,
                new HashMap<>(),
                0L,
                TenantContext.getTenantId(),
                LocalDateTime.now()
        );
    }

    private record DemoScenario(
            String action,
            String conclusion,
            double confidence,
            Map<String, Object> params
    ) {}
}
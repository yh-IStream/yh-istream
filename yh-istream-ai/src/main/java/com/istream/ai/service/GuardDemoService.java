package com.istream.ai.service;

import com.istream.common.annotation.AnomalyGuard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 真实 AI 守护演示服务
 *
 * <p>所有方法均标注 {@link AnomalyGuard} 注解，
 * 方法执行成功 → AOP 切面自动调用 AI → 发布 {@code AnomalyGuardEvent}
 * → 创建 {@code ActionSuggestion} → 前端展示卡片。</p>
 *
 * <p>提示词中明确描述了异常检测模式和可用的 {@code @Tool} 方法，
 * Spring AI 模型会根据数据上下文做出真实判断。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class GuardDemoService {

    /**
     * 创建订单（触发 AI 异常检测）
     *
     * <p>AI 检测逻辑：金额超过 50000 或远超均值 → 异常；
     * 信用分 < 500 且大额 → 风险；
     * 异地地址 + 信用分低 → 可疑。</p>
     *
     * <p>可用工具：freezeEntity, escalateToManager, markAsFalseAlarm</p>
     */
    @AnomalyGuard(
            prompt = """
                    你是 iStream 安全守护引擎。分析此订单创建操作，检测异常模式：
                    1. 订单金额 orderAmount >= 50000 → 严重异常（freezeEntity）
                    2. 订单金额 orderAmount >= 10000 且 creditScore < 500 → 可疑（escalateToManager）
                    3. deliveryAddress 非用户常用地址 + creditScore < 400 → 可疑（escalateToManager）
                    4. ordersInLastHour >= 10 → 疑似刷单（freezeEntity）
                    5. 其他看似正常的情况 → 正常（anomalyDetected=false）

                    当确定要处置时，suggestedAction 必须为：freezeEntity, escalateToManager, 或 markAsFalseAlarm 之一。
                    actionParams 包含对应工具所需的全部参数名和值。
                    """,
            confidence = 0.7,
            contextVars = {"orderAmount", "userId", "creditScore", "deliveryAddress", "ordersInLastHour"}
    )
    public Map<String, Object> createOrder(Long orderId, BigDecimal orderAmount, Long userId,
                                           int creditScore, String deliveryAddress,
                                           int ordersInLastHour) {
        log.info("GuardDemoService: 创建订单 orderId={}, amount={}, userId={}, creditScore={}",
                orderId, orderAmount, userId, creditScore);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "created");
        result.put("amount", orderAmount);
        return result;
    }

    /**
     * 修改系统配置（触发 AI 异常检测）
     *
     * <p>AI 检测逻辑：核心配置被修改 + 短时多次修改 → 高危；
     * 非工作时间修改 → 可疑。</p>
     */
    @AnomalyGuard(
            prompt = """
                    你是 iStream 安全守护引擎。分析此系统配置变更操作：
                    1. configKey 包含 "PAYMENT" 或 "SECURITY" → 高危（escalateToManager）
                    2. recentChangesInMinutes >= 3 → 短时频繁修改（freezeEntity）
                    3. operator 非管理员 → 越权操作（escalateToManager）
                    4. 其他情况判断正常（anomalyDetected=false）

                    suggestedAction 必须为可用工具之一，actionParams 必须包含工具所需参数。
                    """,
            confidence = 0.7,
            contextVars = {"configKey", "changeDetail", "operator", "recentChangesInMinutes"}
    )
    public Map<String, Object> updateConfig(Long configId, String configKey,
                                            String changeDetail, String operator,
                                            int recentChangesInMinutes) {
        log.info("GuardDemoService: 修改配置 configId={}, key={}, operator={}",
                configId, configKey, operator);
        Map<String, Object> result = new HashMap<>();
        result.put("configId", configId);
        result.put("status", "updated");
        return result;
    }

    /**
     * 导出数据（触发 AI 异常检测）
     *
     * <p>AI 检测逻辑：导出量远超日均 → 数据泄露风险；
     * 含敏感信息 + 大批量 → 严重异常。</p>
     */
    @AnomalyGuard(
            prompt = """
                    你是 iStream 安全守护引擎。分析此次数据导出操作：
                    1. recordCount >= dailyAvgRecords * 5 → 严重异常（freezeEntity 或 escalateToManager）
                    2. containsPii=true 且 recordCount >= 100 → 敏感数据泄露风险（escalateToManager）
                    3. recordCount < dailyAvgRecords * 2 且不含敏感 → 正常（anomalyDetected=false）
                    4. 中间情况按置信度判断

                    suggestedAction 必须为可用工具之一，actionParams 必须包含工具所需参数。
                    """,
            confidence = 0.7,
            contextVars = {"dataType", "recordCount", "dailyAvgRecords", "containsPii"}
    )
    public Map<String, Object> exportData(Long userId, String dataType, int recordCount,
                                          int dailyAvgRecords, boolean containsPii) {
        log.info("GuardDemoService: 导出数据 userId={}, type={}, count={}, pii={}",
                userId, dataType, recordCount, containsPii);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("dataType", dataType);
        result.put("exported", recordCount);
        return result;
    }

    /**
     * 用户登录（触发 AI 异常检测）
     *
     * <p>AI 检测逻辑：异地登录 → 可疑；
     * 短时间内多地登录 → 严重异常；
     * 登录城市与常用城市不同 → 需关注。</p>
     */
    @AnomalyGuard(
            prompt = """
                    你是 iStream 安全守护引擎。分析此用户登录事件：
                    1. loginCity 与 usualCity 不同 且 recentLoginCities >= 3 → 严重异常（freezeEntity）
                    2. loginCity 与 usualCity 不同 且 secondsSinceLastLogin < 120 → 异地秒切换（escalateToManager）
                    3. loginCity == usualCity → 正常登录（anomalyDetected=false）
                    4. 仅 loginCity 不同但 recentLoginCities < 2 → 低风险关注（markAsFalseAlarm）

                    suggestedAction 必须为可用工具之一，actionParams 必须包含工具所需参数。
                    """,
            confidence = 0.7,
            contextVars = {"loginCity", "usualCity", "recentLoginCities", "secondsSinceLastLogin"}
    )
    public Map<String, Object> loginDetect(Long userId, String loginCity, String usualCity,
                                           int recentLoginCities, int secondsSinceLastLogin) {
        log.info("GuardDemoService: 用户登录 userId={}, city={}, usualCity={}",
                userId, loginCity, usualCity);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("loginCity", loginCity);
        result.put("status", "logged_in");
        return result;
    }
}
package com.istream.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.istream.ai.service.GuardDemoService;
import com.istream.common.model.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 真实 AI 守护演示控制器
 *
 * <p>调用带有 {@code @AnomalyGuard} 注解的业务方法，触发真实的 AI 分析管道。
 * 与 {@link DemoTriggerController}（模拟触发）的区别：</p>
 * <ul>
 *   <li>{@code DemoTriggerController}：跳过 AI，直接发布预设事件 → 适合无 AI 环境演示 UI</li>
 *   <li>{@code GuardDemoController}：走完整 AI 管道 → AI 模型真实分析 → 真实结论 → 前端卡片</li>
 * </ul>
 *
 * <p>前置条件：</p>
 * <ul>
 *   <li>{@code istream.ai.enabled=true}</li>
 *   <li>已配置 Spring AI 模型连接（OpenRouter/Ollama/OpenAI）</li>
 *   <li>{@code GuardToolsDemo} 的 {@code @Tool} 方法已注册</li>
 * </ul>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@RestController
@RequestMapping("/ai/guard")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "istream.ai", name = "enabled", havingValue = "true")
public class GuardDemoController {

    private final GuardDemoService guardDemoService;

    /**
     * 触发订单异常检测（真实 AI）
     *
     * <p>模拟用户创建一笔订单，{@code @AnomalyGuard} 标注的
     * {@code guardDemoService.createOrder()} 执行后自动调用 AI 分析，
     * 检测金额异常、信用风险、地址可疑等。</p>
     */
    @SaCheckLogin
    @PostMapping("/order")
    public R<Map<String, Object>> detectOrder(
            @RequestParam(defaultValue = "2024001") Long orderId,
            @RequestParam(defaultValue = "89999") BigDecimal orderAmount,
            @RequestParam(defaultValue = "1001") Long userId,
            @RequestParam(defaultValue = "420") int creditScore,
            @RequestParam(defaultValue = "新疆阿克苏地区") String deliveryAddress,
            @RequestParam(defaultValue = "6") int ordersInLastHour) {

        Map<String, Object> order = guardDemoService.createOrder(
                orderId, orderAmount, userId, creditScore, deliveryAddress, ordersInLastHour);
        log.info("GuardDemoController: 订单创建完成 orderId={}, 等待 AI 分析...", orderId);
        return R.ok(order);
    }

    /**
     * 触发配置变更检测（真实 AI）
     *
     * <p>模拟管理员修改系统配置，AI 检测是否为高危操作。</p>
     */
    @SaCheckLogin
    @PostMapping("/config")
    public R<Map<String, Object>> detectConfig(
            @RequestParam(defaultValue = "1001") Long configId,
            @RequestParam(defaultValue = "SYS_PAYMENT_ENABLED") String configKey,
            @RequestParam(defaultValue = "true→false") String changeDetail,
            @RequestParam(defaultValue = "root") String operator,
            @RequestParam(defaultValue = "3") int recentChangesInMinutes) {

        Map<String, Object> result = guardDemoService.updateConfig(
                configId, configKey, changeDetail, operator, recentChangesInMinutes);
        log.info("GuardDemoController: 配置变更完成 configId={}，等待 AI 分析...", configId);
        return R.ok(result);
    }

    /**
     * 触发数据导出检测（真实 AI）
     *
     * <p>模拟用户导出业务数据，AI 检测是否存在数据泄露风险。</p>
     */
    @SaCheckLogin
    @PostMapping("/export")
    public R<Map<String, Object>> detectExport(
            @RequestParam(defaultValue = "2001") Long userId,
            @RequestParam(defaultValue = "OperLog") String dataType,
            @RequestParam(defaultValue = "520") int recordCount,
            @RequestParam(defaultValue = "50") int dailyAvgRecords,
            @RequestParam(defaultValue = "true") boolean containsPii) {

        Map<String, Object> result = guardDemoService.exportData(
                userId, dataType, recordCount, dailyAvgRecords, containsPii);
        log.info("GuardDemoController: 数据导出完成 userId={}，等待 AI 分析...", userId);
        return R.ok(result);
    }

    /**
     * 触发登录异常检测（真实 AI）
     *
     * <p>模拟用户从异常地理位置登录，AI 检测账号安全风险。</p>
     */
    @SaCheckLogin
    @PostMapping("/login")
    public R<Map<String, Object>> detectLogin(
            @RequestParam(defaultValue = "38291") Long userId,
            @RequestParam(defaultValue = "深圳") String loginCity,
            @RequestParam(defaultValue = "北京") String usualCity,
            @RequestParam(defaultValue = "3") int recentLoginCities,
            @RequestParam(defaultValue = "600") int secondsSinceLastLogin) {

        Map<String, Object> result = guardDemoService.loginDetect(
                userId, loginCity, usualCity, recentLoginCities, secondsSinceLastLogin);
        log.info("GuardDemoController: 登录完成 userId={}，等待 AI 分析...", userId);
        return R.ok(result);
    }
}
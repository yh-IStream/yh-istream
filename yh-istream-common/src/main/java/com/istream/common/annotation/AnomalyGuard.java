package com.istream.common.annotation;

import com.istream.common.enums.EventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常守护注解（传感器）
 *
 * <p>标注在 Service 方法上，方法执行成功后自动调用 AI 分析数据变更，
 * 发现规则引擎之外的未知异常。分析结果作为 {@link EventType#AI_INSIGHT}
 * 事件走 Watchdog 管道推送。</p>
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时生效，关闭时 AOP 切面不注册，零开销。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @AnomalyGuard(prompt = "检测金额异常：单笔超过均值3倍或同用户1小时内超过5笔",
 *               confidence = 0.7)
 * @Transactional
 * public void createOrder(SysOrder order) { ... }
 * }</pre>
 *
 * @author istream
 * @since 2026-09-26
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnomalyGuard {

    /**
     * AI 分析提示词
     *
     * <p>描述期望 AI 检测的异常模式，AI 将基于此提示词分析方法参数和返回值。</p>
     */
    String prompt();

    /**
     * 置信度阈值（0.0 ~ 1.0）
     *
     * <p>AI 分析结果的置信度低于此阈值时，仅写日志不推送告警。</p>
     */
    double confidence() default 0.7;

    /**
     * 分析上下文变量名
     *
     * <p>指定方法参数中哪些变量作为 AI 分析的上下文数据。
     * 为空时自动提取所有参数。</p>
     */
    String[] contextVars() default {};
}
package com.istream.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 *
 * <p>基于 Redisson RRateLimiter 令牌桶算法实现分布式限流。
 * 标注在 Controller 方法上，超过限制后返回 {@code ResultCode.RATE_LIMIT}。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   @RateLimit(key = "login", rate = 5, timeout = 0)
 *   @PostMapping("/login")
 *   public R<LoginDTO> login(@RequestBody LoginRequest request) { ... }
 * }</pre>
 *
 * @author isteam
 * @since 2026-08-17
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流标识 Key，默认使用 {@code className.methodName} 自动生成
     */
    String key() default "";

    /**
     * 每秒允许的请求数（令牌产生速率），默认 10 QPS
     */
    int rate() default 10;

    /**
     * 获取令牌的最大等待时间（秒），默认 1 秒
     * <p>设为 0 表示不等待，立即返回限流结果</p>
     */
    long timeout() default 1;
}
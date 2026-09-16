package com.istream.framework.aspect;

import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.ResultCode;
import com.istream.common.model.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口限流切面
 *
 * <p>基于 Redisson RRateLimiter 令牌桶算法实现分布式限流。
 * 通过 {@link RateLimit} 注解标注需要限流的方法。</p>
 *
 * <p>执行顺序：@Order(0)，在 {@link OperLogAspect} 之前执行，
 * 确保被限流的请求不会产生操作日志。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Aspect
@Order(0)
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String RATE_LIMITER_KEY_PREFIX = "rate_limit:";

    private final RedissonClient redissonClient;

    private final ConcurrentHashMap<String, Boolean> initializedKeys = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        String fullKey = RATE_LIMITER_KEY_PREFIX + key;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(fullKey);
        initializedKeys.computeIfAbsent(fullKey, k -> {
            rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofSeconds(1));
            return Boolean.TRUE;
        });

        if (rateLimiter.tryAcquire(Duration.ofSeconds(rateLimit.timeout()))) {
            return joinPoint.proceed();
        }

        log.warn("接口限流触发: key={}, rate={}/s", key, rateLimit.rate());
        return R.fail(ResultCode.RATE_LIMIT);
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key();
        }
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }
}
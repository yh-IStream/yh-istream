package com.istream.framework.aspect;

import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.LimitType;
import com.istream.common.enums.ResultCode;
import com.istream.common.model.R;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 接口限流切面
 *
 * <p>基于 Redisson RRateLimiter 令牌桶算法实现分布式限流。
 * 通过 {@link RateLimit} 注解标注需要限流的方法，支持三种限流维度。</p>
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

    /**
     * 动态维度限流器的过期时间，长时间无请求的 key 自动清理
     */
    private static final Duration DIMENSION_KEY_EXPIRE = Duration.ofHours(1);

    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        String fullKey = RATE_LIMITER_KEY_PREFIX + key;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(fullKey);
        rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), Duration.ofSeconds(1));

        if (rateLimit.limitType() != LimitType.GLOBAL) {
            rateLimiter.expire(DIMENSION_KEY_EXPIRE);
        }

        boolean acquired;
        try {
            acquired = rateLimiter.tryAcquire(Duration.ofSeconds(rateLimit.timeout()));
        } catch (Exception e) {
            log.warn("限流器异常，降级放行: key={}", key, e);
            return joinPoint.proceed();
        }

        if (!acquired) {
            log.warn("接口限流触发: key={}, rate={}/s, limitType={}", key, rateLimit.rate(), rateLimit.limitType());
            return R.fail(ResultCode.RATE_LIMIT);
        }

        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String baseKey = rateLimit.key().isBlank()
                ? joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName()
                : rateLimit.key();

        if (rateLimit.limitType() == LimitType.GLOBAL) {
            return baseKey;
        }

        HttpServletRequest request = getRequest();
        if (rateLimit.limitType() == LimitType.IP) {
            return baseKey + ":ip:" + IpUtils.getClientIp(request);
        }

        Long userId = SecurityUtils.getLoginUserId();
        if (userId != null) {
            return baseKey + ":user:" + userId;
        }
        return baseKey + ":ip:" + IpUtils.getClientIp(request);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
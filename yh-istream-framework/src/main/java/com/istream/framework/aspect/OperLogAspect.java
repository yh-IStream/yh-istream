package com.istream.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.istream.common.annotation.OperLog;
import com.istream.common.constant.Constants;
import com.istream.common.enums.ResultCode;
import com.istream.common.event.OperLogEvent;
import com.istream.common.model.R;
import com.istream.framework.util.IpUtils;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.util.IpRegionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 *
 * <p>拦截 {@link OperLog} 注解标注的方法，自动记录操作日志。
 * 日志通过 Spring {@link ApplicationEventPublisher} 异步发布，
 * 由 {@code OperLogListener} 异步持久化。</p>
 *
 * <p>执行顺序：@Order(2)，在 {@link RateLimitAspect}(@Order=0) 和
 * {@link com.istream.system.aspect.DataScopeAspect}(@Order=1) 之后执行。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final ApplicationEventPublisher eventPublisher;

    private static final int MAX_PARAM_LENGTH = 2000;
    private static final int MAX_ERROR_MSG_LENGTH = 2000;

    /**
     * 环绕通知：记录操作日志
     *
     * @param joinPoint 切点
     * @param operLog   操作日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();

        String title = operLog.title();
        Integer businessType = operLog.businessType().getCode();
        String method = joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName();
        LocalDateTime operTime = LocalDateTime.now();

        String requestMethod = null;
        String operUrl = null;
        String operIp = null;
        String operLocation = null;
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestMethod = request.getMethod();
            operUrl = request.getRequestURI();
            operIp = IpUtils.getClientIp(request);
            operLocation = IpRegionUtils.parseRegion(operIp);
        }

        Long operBy = null;
        String operName = null;
        try {
            operBy = SecurityUtils.getLoginUserId();
            operName = (String) StpUtil.getSession().get(Constants.SESSION_USERNAME_KEY);
        } catch (Exception e) {
            log.debug("操作日志获取当前用户信息失败（可能为匿名访问）: {}", e.getMessage());
        }

        String operParam = serializeParams(joinPoint.getArgs());

        int status = 0;
        String jsonResult = "{}";
        String errorMsg = null;
        Object result;

        try {
            result = joinPoint.proceed();
            jsonResult = serializeResult(result);
        } catch (Exception e) {
            status = 1;
            String msg = e.getMessage();
            errorMsg = msg != null && msg.length() > MAX_ERROR_MSG_LENGTH
                    ? msg.substring(0, MAX_ERROR_MSG_LENGTH) + "..." : msg;
            try {
                jsonResult = JSONUtil.toJsonStr(R.fail(ResultCode.ERROR.getCode(), errorMsg));
            } catch (Exception ex) {
                jsonResult = "{}";
            }
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - start;
            eventPublisher.publishEvent(new OperLogEvent(
                    title, businessType, method, requestMethod, operUrl, operIp, operLocation,
                    operParam, jsonResult, status, errorMsg, costTime, operBy, operName, operTime));
        }

        return result;
    }

    private String serializeParams(Object[] args) {
        try {
            String paramJson = JSONUtil.toJsonStr(filterSerializableArgs(args));
            return paramJson.length() > MAX_PARAM_LENGTH
                    ? paramJson.substring(0, MAX_PARAM_LENGTH) + "..." : paramJson;
        } catch (Exception e) {
            log.debug("操作日志序列化请求参数失败: {}", e.getMessage());
            return "[]";
        }
    }

    private String serializeResult(Object result) {
        try {
            String resultJson = result != null ? JSONUtil.toJsonStr(result) : "{}";
            return resultJson.length() > MAX_PARAM_LENGTH
                    ? resultJson.substring(0, MAX_PARAM_LENGTH) + "..." : resultJson;
        } catch (Exception e) {
            log.debug("操作日志序列化返回结果失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 过滤不可序列化的参数（HttpServletRequest、HttpServletResponse、MultipartFile）
     */
    private Object[] filterSerializableArgs(Object[] args) {
        if (args == null) {
            return new Object[0];
        }
        Object[] filtered = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                filtered[i] = null;
            } else {
                filtered[i] = arg;
            }
        }
        return filtered;
    }
}
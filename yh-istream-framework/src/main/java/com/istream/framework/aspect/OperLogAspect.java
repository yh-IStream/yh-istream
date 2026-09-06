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
 * 日志通过 Spring {@link org.springframework.context.ApplicationEventPublisher} 异步发布，
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

        OperLogEvent event = new OperLogEvent();
        event.setTitle(operLog.title());
        event.setBusinessType(operLog.businessType().getCode());
        event.setMethod(joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName());
        event.setOperTime(LocalDateTime.now());

        fillRequestInfo(event);
        fillOperatorInfo(event);
        fillParamInfo(event, joinPoint.getArgs());

        Object result;
        try {
            result = joinPoint.proceed();
            event.setStatus(0);
            fillResultInfo(event, result);
        } catch (Exception e) {
            event.setStatus(1);
            String errorMsg = e.getMessage();
            event.setErrorMsg(errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                    ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH) + "..." : errorMsg);
            try {
                event.setJsonResult(JSONUtil.toJsonStr(R.fail(ResultCode.ERROR.getCode(), event.getErrorMsg())));
            } catch (Exception ex) {
                event.setJsonResult("{}");
            }
            throw e;
        } finally {
            event.setCostTime(System.currentTimeMillis() - start);
            eventPublisher.publishEvent(event);
        }

        return result;
    }

    /**
     * 填充请求信息（URL、IP、请求方式等）
     */
    private void fillRequestInfo(OperLogEvent event) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            event.setRequestMethod(request.getMethod());
            event.setOperUrl(request.getRequestURI());
            event.setOperIp(IpUtils.getClientIp(request));
            event.setOperLocation(IpRegionUtils.parseRegion(event.getOperIp()));
        }
    }

    /**
     * 填充操作人信息（用户ID、用户名）
     */
    private void fillOperatorInfo(OperLogEvent event) {
        try {
            event.setOperBy(SecurityUtils.getLoginUserId());
            event.setOperName((String) StpUtil.getSession().get(Constants.SESSION_USERNAME_KEY));
        } catch (Exception e) {
            log.debug("操作日志获取当前用户信息失败（可能为匿名访问）: {}", e.getMessage());
        }
    }

    /**
     * 填充请求参数信息
     */
    private void fillParamInfo(OperLogEvent event, Object[] args) {
        try {
            String paramJson = JSONUtil.toJsonStr(filterSerializableArgs(args));
            event.setOperParam(paramJson.length() > MAX_PARAM_LENGTH
                    ? paramJson.substring(0, MAX_PARAM_LENGTH) + "..." : paramJson);
        } catch (Exception e) {
            log.debug("操作日志序列化请求参数失败: {}", e.getMessage());
            event.setOperParam("[]");
        }
    }

    /**
     * 填充返回结果信息
     */
    private void fillResultInfo(OperLogEvent event, Object result) {
        try {
            String resultJson = result != null ? JSONUtil.toJsonStr(result) : "{}";
            event.setJsonResult(resultJson.length() > MAX_PARAM_LENGTH
                    ? resultJson.substring(0, MAX_PARAM_LENGTH) + "..." : resultJson);
        } catch (Exception e) {
            log.debug("操作日志序列化返回结果失败: {}", e.getMessage());
            event.setJsonResult("{}");
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
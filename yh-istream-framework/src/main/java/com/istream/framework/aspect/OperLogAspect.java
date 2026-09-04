package com.istream.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.istream.common.annotation.OperLog;
import com.istream.common.constant.Constants;
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
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final ApplicationEventPublisher eventPublisher;

    private static final int MAX_PARAM_LENGTH = 2000;
    private static final int MAX_ERROR_MSG_LENGTH = 2000;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();

        OperLogEvent event = new OperLogEvent();
        event.setTitle(operLog.title());
        event.setBusinessType(operLog.businessType().getCode());
        event.setMethod(joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName());
        event.setOperTime(LocalDateTime.now());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            event.setRequestMethod(request.getMethod());
            event.setOperUrl(request.getRequestURI());
            event.setOperIp(IpUtils.getClientIp(request));
            event.setOperLocation(IpRegionUtils.parseRegion(event.getOperIp()));
        }

        try {
            event.setOperBy(SecurityUtils.getLoginUserId());
            event.setOperName((String) StpUtil.getSession().get(Constants.SESSION_USERNAME_KEY));
        } catch (Exception ignored) {
        }

        try {
            String paramJson = JSONUtil.toJsonStr(filterSerializableArgs(joinPoint.getArgs()));
            event.setOperParam(paramJson.length() > MAX_PARAM_LENGTH
                    ? paramJson.substring(0, MAX_PARAM_LENGTH) + "..." : paramJson);
        } catch (Exception e) {
            event.setOperParam("[]");
        }

        Object result;
        try {
            result = joinPoint.proceed();
            event.setStatus(0);
            try {
                String resultJson = result != null ? JSONUtil.toJsonStr(result) : "{}";
                event.setJsonResult(resultJson.length() > MAX_PARAM_LENGTH
                        ? resultJson.substring(0, MAX_PARAM_LENGTH) + "..." : resultJson);
            } catch (Exception e) {
                event.setJsonResult("{}");
            }
        } catch (Exception e) {
            event.setStatus(1);
            String errorMsg = e.getMessage();
            event.setErrorMsg(errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                    ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH) + "..." : errorMsg);
            try {
                event.setJsonResult(JSONUtil.toJsonStr(R.fail(500, event.getErrorMsg())));
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
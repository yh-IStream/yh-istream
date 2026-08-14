package com.istream.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.istream.common.annotation.OperLog;
import com.istream.common.event.OperLogEvent;
import com.istream.common.model.R;
import com.istream.common.util.IpUtils;
import com.istream.framework.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final ApplicationEventPublisher eventPublisher;

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
        }

        try {
            event.setOperBy(SecurityUtils.getLoginUserId());
            event.setOperName((String) StpUtil.getSession().get("username"));
        } catch (Exception ignored) {
        }

        try {
            event.setOperParam(JSONUtil.toJsonStr(joinPoint.getArgs()));
        } catch (Exception e) {
            event.setOperParam("[]");
        }

        Object result;
        try {
            result = joinPoint.proceed();
            event.setStatus(0);
            try {
                event.setJsonResult(JSONUtil.toJsonStr(result));
            } catch (Exception e) {
                event.setJsonResult("{}");
            }
        } catch (Exception e) {
            event.setStatus(1);
            event.setErrorMsg(e.getMessage());
            try {
                event.setJsonResult(JSONUtil.toJsonStr(R.fail(500, e.getMessage())));
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

}
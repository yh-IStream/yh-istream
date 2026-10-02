package com.istream.ai.aspect;

import com.istream.common.annotation.AnomalyGuard;
import com.istream.common.event.AnomalyGuardEvent;
import com.istream.common.model.sse.SseEvent;
import com.istream.ai.config.AIProperties;
import com.istream.ai.provider.AnomalyGuardRequest;
import com.istream.ai.provider.AnomalyGuardResult;
import com.istream.ai.provider.AIProvider;
import com.istream.framework.aspect.AspectUtils;
import com.istream.framework.sse.SseService;
import com.istream.framework.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 异常守护 AOP 切面（传感器）
 *
 * <p>拦截 {@link AnomalyGuard} 注解标注的方法，在方法执行成功后自动调用 AI 分析。
 * 分析结果作为 {@link AnomalyGuardEvent} 发布，由 Watchdog 监听并走多通道推送。</p>
 *
 * <p>仅当 {@code istream.ai.enabled=true} 时注册此切面，关闭时零开销。</p>
 *
 * @author istream
 * @since 2026-09-26
 */
@Slf4j
@Aspect
@Order(4)
public class AnomalyGuardAspect {

    private final AIProvider aiProvider;
    private final AIProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<SseService> sseServiceProvider;

    public AnomalyGuardAspect(AIProvider aiProvider, AIProperties properties,
                              ApplicationEventPublisher eventPublisher,
                              ObjectProvider<SseService> sseServiceProvider) {
        this.aiProvider = aiProvider;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
        this.sseServiceProvider = sseServiceProvider;
    }

    /**
     * 方法执行成功后调用 AI 分析
     *
     * @param joinPoint    切点
     * @param anomalyGuard 异常守护注解
     */
    @AfterReturning("@annotation(anomalyGuard)")
    public void afterReturning(JoinPoint joinPoint, AnomalyGuard anomalyGuard) {
        if (!properties.isEnabled()) {
            return;
        }

        if (!aiProvider.isAvailable()) {
            log.debug("AnomalyGuardAspect: AI 提供者不可用，跳过分析");
            return;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();

            Map<String, Object> context = extractContext(joinPoint, method, anomalyGuard);

            String entityType = resolveEntityType(joinPoint, method);
            Long entityId = AspectUtils.extractEntityId(joinPoint.getArgs());

            AnomalyGuardRequest request = new AnomalyGuardRequest(
                    anomalyGuard.prompt(),
                    entityType,
                    entityId,
                    context,
                    anomalyGuard.confidence()
            );

            AnomalyGuardResult result = aiProvider.analyze(request);

            if (result.anomalyDetected()) {
                Long operatorId = AspectUtils.resolveOperatorId();
                Long tenantId = TenantContext.getTenantId();

                AnomalyGuardEvent event = new AnomalyGuardEvent(
                        entityType, entityId,
                        anomalyGuard.prompt(), result.conclusion(), result.confidence(),
                        result.suggestedAction(), result.actionParams(),
                        context, operatorId, tenantId, LocalDateTime.now());

                eventPublisher.publishEvent(event);

                log.info("AnomalyGuardAspect: AI 发现异常 entityType={}, entityId={}, confidence={}, action={}",
                        entityType, entityId, String.format("%.2f", result.confidence()), result.suggestedAction());
            } else if (result.conclusion() != null && result.conclusion().startsWith("AI 分析失败")) {
                log.warn("AnomalyGuardAspect: AI 调用失败 entityType={}, entityId={}, method={}, 原因={}",
                        entityType, entityId, methodName, result.conclusion());
                SseService sseService = sseServiceProvider.getIfAvailable();
                if (sseService != null) {
                    Long operatorId = AspectUtils.resolveOperatorId();
                    if (operatorId != null) {
                        sseService.sendToUser(operatorId,
                                SseEvent.of("SYSTEM", "AI 调用异常，请检查模型连接。原因: " + result.conclusion()));
                    }
                }
            } else {
                log.debug("AnomalyGuardAspect: AI 未发现异常 method={}", methodName);
            }

        } catch (Exception e) {
            log.warn("AnomalyGuardAspect: AI 分析失败（降级，不影响主业务流）", e);
        }
    }

    private Map<String, Object> extractContext(JoinPoint joinPoint, Method method, AnomalyGuard anomalyGuard) {
        Map<String, Object> context = new HashMap<>();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();

        String[] contextVars = anomalyGuard.contextVars();
        if (contextVars.length > 0) {
            for (String varName : contextVars) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (varName.equals(paramNames[i])) {
                        context.put(varName, args[i]);
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < paramNames.length; i++) {
                context.put(paramNames[i], args[i]);
            }
        }

        return context;
    }

    private String resolveEntityType(JoinPoint joinPoint, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName().replace("Service", "").replace("ServiceImpl", "");
    }

}
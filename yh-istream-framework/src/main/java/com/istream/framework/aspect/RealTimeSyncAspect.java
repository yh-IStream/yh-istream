package com.istream.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.annotation.RealTimeSync;
import com.istream.common.annotation.SyncEvent;
import com.istream.common.enums.SyncEventType;
import com.istream.common.event.DataChangeEvent;
import com.istream.framework.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * 声明式实时同步 AOP 切面
 *
 * <p>拦截 {@link RealTimeSync} 注解标注的 Service 类中的公共方法，
 * 在方法执行成功后自动推断变更类型并发布 {@link DataChangeEvent}。</p>
 *
 * <p>推断优先级：</p>
 * <ol>
 *   <li>方法上的 {@link SyncEvent} 注解显式指定</li>
 *   <li>方法名约定推断（create/add/save/insert → CREATE, update/modify/edit → UPDATE, delete/remove → DELETE）</li>
 * </ol>
 *
 * <p>仅当推断的变更类型在 {@link RealTimeSync#events()} 声明范围内时才发布事件。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Aspect
@Order(3)
@Component
@RequiredArgsConstructor
public class RealTimeSyncAspect {

    private final ApplicationEventPublisher eventPublisher;

    private static final Map<String, SyncEventType> METHOD_PREFIX_MAP = Map.of(
            "create", SyncEventType.CREATE,
            "add", SyncEventType.CREATE,
            "save", SyncEventType.CREATE,
            "insert", SyncEventType.CREATE,
            "update", SyncEventType.UPDATE,
            "modify", SyncEventType.UPDATE,
            "edit", SyncEventType.UPDATE,
            "delete", SyncEventType.DELETE,
            "remove", SyncEventType.DELETE
    );

    /**
     * Service 方法执行成功后发布数据变更事件
     *
     * @param joinPoint 切点
     */
    @AfterReturning("@within(realTimeSync)")
    public void afterReturning(JoinPoint joinPoint, RealTimeSync realTimeSync) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        SyncEventType eventType = resolveEventType(method, methodName);
        if (eventType == null) {
            log.debug("RealTimeSync: 方法 {} 无法推断变更类型，跳过", methodName);
            return;
        }

        Set<SyncEventType> declaredEvents = Set.of(realTimeSync.events());
        if (!declaredEvents.contains(eventType)) {
            log.debug("RealTimeSync: 方法 {} 推断类型 {} 未在 @RealTimeSync.events 声明中，跳过",
                    methodName, eventType);
            return;
        }

        String entityType = realTimeSync.entity().getSimpleName();
        Long entityId = extractEntityId(joinPoint.getArgs(), method);
        Long operatorId = resolveOperatorId();
        Long tenantId = TenantContext.getTenantId();

        DataChangeEvent event = new DataChangeEvent(
                this, entityType, entityId, eventType, null, operatorId, tenantId);
        eventPublisher.publishEvent(event);

        log.info("RealTimeSync: 发布数据变更事件 entityType={}, entityId={}, eventType={}",
                entityType, entityId, eventType);
    }

    /**
     * 推断方法触发的变更类型
     *
     * @param method     方法
     * @param methodName 方法名
     * @return 变更类型，无法推断时返回 null
     */
    private SyncEventType resolveEventType(Method method, String methodName) {
        SyncEvent syncEvent = method.getAnnotation(SyncEvent.class);
        if (syncEvent != null) {
            return syncEvent.value();
        }

        String lowerName = methodName.toLowerCase();
        for (Map.Entry<String, SyncEventType> entry : METHOD_PREFIX_MAP.entrySet()) {
            if (lowerName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 从方法参数中提取实体ID
     *
     * <p>提取策略：优先取第一个 Long 类型参数作为实体ID，
     * 其次取第一个参数的 id 字段（通过反射）。</p>
     *
     * @param args   方法参数
     * @param method 方法
     * @return 实体ID，无法提取时返回 null
     */
    private Long extractEntityId(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof Long id) {
                return id;
            }
        }

        Object firstArg = args[0];
        try {
            var idField = firstArg.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(firstArg);
            if (idValue instanceof Long id) {
                return id;
            }
        } catch (Exception e) {
            log.debug("RealTimeSync: 无法从参数 {} 提取 id 字段", firstArg.getClass().getSimpleName());
        }

        return null;
    }

    /**
     * 获取当前操作人ID
     *
     * @return 操作人ID，未登录时返回 null
     */
    private Long resolveOperatorId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("RealTimeSync: 无法获取当前登录用户ID");
        }
        return null;
    }
}
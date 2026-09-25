package com.istream.common.annotation;

import com.istream.common.enums.SyncEventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式实时同步注解
 *
 * <p>标注在 Service 类上，声明该 Service 管理的实体及其变更事件类型。
 * AOP 切面自动拦截匹配的方法，在方法执行成功后发布 {@link com.istream.common.enums.EventType#DATA_CHANGE} 事件，
 * 经 EventBus → SSE 推送到前端，同时进入 Watchdog 守护管道进行规则检测。</p>
 *
 * <p>方法事件类型推断规则（约定优于配置）：</p>
 * <ul>
 *   <li>方法名以 create/add/save/insert 开头 → {@link SyncEventType#CREATE}</li>
 *   <li>方法名以 update/modify/edit 开头 → {@link SyncEventType#UPDATE}</li>
 *   <li>方法名以 delete/remove 开头 → {@link SyncEventType#DELETE}</li>
 *   <li>可通过 {@link SyncEvent} 注解在方法上显式指定，覆盖约定推断</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @RealTimeSync(entity = SysOrder.class, events = {CREATE, UPDATE})
 * public class SysOrderService { ... }
 * }</pre>
 *
 * @author istream
 * @since 2026-09-24
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RealTimeSync {

    /**
     * 管理的实体类
     *
     * <p>用于提取实体类型名称和主键，作为 DATA_CHANGE 事件的元数据。</p>
     */
    Class<?> entity();

    /**
     * 关注的变更事件类型
     *
     * <p>仅对声明的事件类型发布 DATA_CHANGE，未声明的变更静默忽略。</p>
     */
    SyncEventType[] events();
}
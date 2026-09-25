package com.istream.common.annotation;

import com.istream.common.enums.SyncEventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级同步事件注解
 *
 * <p>标注在 Service 方法上，显式指定该方法触发的同步事件类型，
 * 覆盖 {@link RealTimeSync} 的方法名约定推断。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @RealTimeSync(entity = SysOrder.class, events = {CREATE, UPDATE, DELETE})
 * public class SysOrderService {
 *
 *     @SyncEvent(UPDATE)
 *     public void changeStatus(Long id, Integer status) { ... }
 * }
 * }</pre>
 *
 * @author istream
 * @since 2026-09-24
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncEvent {

    /** 该方法触发的同步事件类型 */
    SyncEventType value();
}
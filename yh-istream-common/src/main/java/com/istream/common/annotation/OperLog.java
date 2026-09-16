package com.istream.common.annotation;

import com.istream.common.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * <p>标注在 Controller 方法上，通过 AOP 切面自动记录操作日志。</p>
 * <p>日志内容包括操作模块、业务类型、请求参数、返回结果、耗时等。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /** 操作模块标题 */
    String title() default "";

    /** 业务类型（新增、修改、删除等） */
    BusinessType businessType() default BusinessType.OTHER;
}
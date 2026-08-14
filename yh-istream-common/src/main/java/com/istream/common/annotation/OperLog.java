package com.istream.common.annotation;

import com.istream.common.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;
}
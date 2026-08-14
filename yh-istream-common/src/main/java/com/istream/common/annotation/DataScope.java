package com.istream.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * <p>
 * 标注在 Service/Mapper 方法上，自动根据当前用户角色拼接数据权限 SQL 条件
 * <pre>{@code
 * @DataScope(deptAlias = "d", userAlias = "u")
 * List<SysUser> selectUserList(SysUserQuery query);
 * }</pre>
 * <p>
 * 注入到 {@link com.istream.common.model.BaseQuery#params} 中的 key 为 {@value #DATA_SCOPE_KEY}
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    String DATA_SCOPE_KEY = "dataScope";

    /** 部门表别名（SQL 中 dept 表的别名） */
    String deptAlias() default "d";

    /** 用户表别名（SQL 中 user 表的别名） */
    String userAlias() default "u";
}
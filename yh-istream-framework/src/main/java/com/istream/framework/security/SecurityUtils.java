package com.istream.framework.security;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.constant.Constants;

/**
 * Sa-Token 安全工具类
 * <p>
 * 封装 Sa-Token 常用操作，统一异常处理，消除各模块中的重复代码
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录时返回 null
     */
    public static Long getLoginUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断当前用户是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return StpUtil.hasRole(Constants.SUPER_ADMIN_ROLE);
    }
}
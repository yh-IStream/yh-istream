package com.istream.framework.security;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Sa-Token 安全工具类
 *
 * <p>封装 Sa-Token 常用操作，统一异常处理，消除各模块中的重复代码</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户ID（可选登录态）
     *
     * <p>适用于审计填充、操作日志等允许匿名访问的场景。</p>
     *
     * @return 用户ID，未登录时返回 null
     */
    public static Long getLoginUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.debug("获取当前登录用户ID失败（可能为匿名访问）", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户ID（必须登录）
     *
     * <p>适用于业务接口等必须登录的场景，
     * 未登录时直接抛出 NotLoginException，由全局异常处理器统一返回 401。</p>
     *
     * @return 用户ID
     */
    public static Long requireLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 判断当前用户是否为超级管理员
     *
     * @return 是否超级管理员
     */
    public static boolean isSuperAdmin() {
        try {
            return StpUtil.hasRole(Constants.SUPER_ADMIN_ROLE);
        } catch (Exception e) {
            log.debug("判断超级管理员失败（可能为匿名访问）", e);
            return false;
        }
    }

    /**
     * 获取当前登录用户名（可选登录态）
     *
     * <p>适用于操作日志等允许匿名访问的场景。</p>
     *
     * @return 用户名，未登录时返回 null
     */
    public static String getLoginUsername() {
        try {
            if (StpUtil.isLogin()) {
                return (String) StpUtil.getSession().get(Constants.SESSION_USERNAME_KEY);
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户名失败（可能为匿名访问）", e);
        }
        return null;
    }

    /**
     * 设置当前会话的用户名
     *
     * <p>登录成功后调用，将用户名存入 Sa-Token Session，
     * 供 {@link #getLoginUsername()} 等方法读取。</p>
     *
     * @param username 用户名
     */
    public static void setSessionUsername(String username) {
        StpUtil.getSession().set(Constants.SESSION_USERNAME_KEY, username);
    }

    /**
     * 搜索在线 Token 会话 ID 列表
     *
     * @param keyword  关键字（空串表示匹配所有）
     * @param start    起始位置
     * @param size     数量（-1 表示全部）
     * @param reverse  是否反向排序
     * @return Token 会话 ID 列表
     */
    public static List<String> searchTokenSessionId(String keyword, int start, int size, boolean reverse) {
        return StpUtil.searchTokenSessionId(keyword, start, size, reverse);
    }
}
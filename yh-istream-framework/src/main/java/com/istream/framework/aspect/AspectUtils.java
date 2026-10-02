package com.istream.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * AOP 切面共享工具
 *
 * <p>提取 {@code RealTimeSyncAspect} 和 {@code AnomalyGuardAspect} 共用的
 * 实体 ID 提取、操作人解析逻辑。</p>
 *
 * @author istream
 * @since 2026-09-25
 */
@Slf4j
public final class AspectUtils {

    private AspectUtils() {
    }

    /**
     * 从方法参数中提取实体 ID
     *
     * <p>提取策略：优先取第一个 Long 类型参数作为实体 ID，
     * 其次取第一个参数的 {@code getId()} 方法返回值。</p>
     *
     * @param args 方法参数
     * @return 实体 ID，无法提取时返回 null
     */
    public static Long extractEntityId(Object[] args) {
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
            var getId = firstArg.getClass().getMethod("getId");
            Object idValue = getId.invoke(firstArg);
            if (idValue instanceof Long id) {
                return id;
            }
        } catch (Exception e) {
            log.debug("AspectUtils: 无法从参数提取 id");
        }

        return null;
    }

    /**
     * 获取当前操作人 ID
     *
     * @return 操作人 ID，未登录时返回 null
     */
    public static Long resolveOperatorId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("AspectUtils: 无法获取当前登录用户ID");
        }
        return null;
    }
}
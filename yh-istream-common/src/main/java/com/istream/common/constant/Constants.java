package com.istream.common.constant;

/**
 * 系统常量
 *
 * <p>集中管理全系统使用的常量值，避免魔法值散落在业务代码中。</p>
 * <p>缓存键前缀统一包含租户占位符，SaaS 模式下通过替换实现租户级缓存隔离。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public final class Constants {

    private Constants() {
    }

    /** 超级管理员角色标识 */
    public static final String SUPER_ADMIN_ROLE = "admin";

    /** 默认普通用户角色标识 */
    public static final String DEFAULT_ROLE_KEY = "user";

    /** 树形结构根节点父ID */
    public static final Long ROOT_PARENT_ID = 0L;

    /** 验证码有效期（秒） */
    public static final long CAPTCHA_EXPIRE_SECONDS = 120;

    /** 登录失败最大次数 */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 登录锁定时间（秒） */
    public static final long LOGIN_LOCK_SECONDS = 600;

    /** 权限缓存前缀（SaaS 模式下追加租户ID） */
    public static final String PERM_CACHE_PREFIX = "perm:cache:";

    /** 角色缓存前缀（SaaS 模式下追加租户ID） */
    public static final String ROLE_CACHE_PREFIX = "role:cache:";

    /** 数据权限缓存前缀（SaaS 模式下追加租户ID） */
    public static final String DATA_SCOPE_CACHE_PREFIX = "data:scope:";

    /** 字典缓存键（SaaS 模式下追加租户ID） */
    public static final String DICT_MAP_KEY = "dict:map";

    /** Session中用户名的Key */
    public static final String SESSION_USERNAME_KEY = "username";

    /** Session中租户ID的Key */
    public static final String SESSION_TENANT_KEY = "tenantId";

    /** 验证码缓存前缀 */
    public static final String CAPTCHA_CACHE_PREFIX = "captcha:";

    /** 登录失败计数缓存前缀 */
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";

    /** 租户缓存前缀 */
    public static final String TENANT_CACHE_PREFIX = "tenant:cache:";

    /** 非租户模式默认租户ID */
    public static final Long DEFAULT_TENANT_ID = 0L;

    /** 默认排序号 */
    public static final int DEFAULT_ORDER_NUM = 0;

    /** 默认是否非常规（0=否） */
    public static final int DEFAULT_NOT_DEFAULT = 0;

    /** 默认配置类型（0=系统内置） */
    public static final int DEFAULT_CONFIG_TYPE = 0;

    /** 逻辑删除标记：正常（未删除） */
    public static final int DEL_FLAG_NORMAL = 0;

    /** 逻辑删除标记：已删除 */
    public static final int DEL_FLAG_DELETED = 1;

    /** 密码最小长度 */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /** 密码最大长度 */
    public static final int PASSWORD_MAX_LENGTH = 32;

    /** 导出分页大小 */
    public static final int EXPORT_PAGE_SIZE = 5000;

    /** 菜单可见性：显示 */
    public static final int MENU_VISIBLE_SHOW = 1;

    /** 菜单可见性：隐藏 */
    public static final int MENU_VISIBLE_HIDE = 0;
}
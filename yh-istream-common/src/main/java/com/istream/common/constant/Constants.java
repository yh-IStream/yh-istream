package com.istream.common.constant;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {
    }

    /** 超级管理员角色标识 */
    public static final String SUPER_ADMIN_ROLE = "admin";

    /** 默认普通用户角色标识 */
    public static final String DEFAULT_ROLE_KEY = "user";

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 树形结构根节点父ID */
    public static final Long ROOT_PARENT_ID = 0L;

    /** 验证码有效期（秒） */
    public static final long CAPTCHA_EXPIRE_SECONDS = 300;

    /** 登录失败最大次数 */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 登录锁定时间（秒） */
    public static final long LOGIN_LOCK_SECONDS = 600;

    /** 权限缓存前缀 */
    public static final String PERM_CACHE_PREFIX = "perm:cache:";

    /** 角色缓存前缀 */
    public static final String ROLE_CACHE_PREFIX = "role:cache:";

    /** 字典缓存键 */
    public static final String DICT_MAP_KEY = "dict:map";

    /** Session中用户名的Key */
    public static final String SESSION_USERNAME_KEY = "username";

    /** 验证码缓存前缀 */
    public static final String CAPTCHA_CACHE_PREFIX = "captcha:";

    /** 登录失败计数缓存前缀 */
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";
}
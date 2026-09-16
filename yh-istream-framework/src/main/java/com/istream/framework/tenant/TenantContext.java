package com.istream.framework.tenant;

/**
 * 租户上下文
 *
 * <p>基于 ThreadLocal 的租户ID持有器，供 MyBatis-Plus TenantLineInnerInterceptor
 * 和 MetaObjectHandler 在当前线程中获取租户ID。</p>
 * <p>当 SaaS 模式未启用时，tenantId 始终为 0（表示非租户模式）。</p>
 *
 * @author istream
 * @since 2026-09-08
 */
public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    private static final Long DEFAULT_TENANT_ID = 0L;

    private TenantContext() {
    }

    /**
     * 设置当前线程的租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 获取当前线程的租户ID
     *
     * <p>如果未设置，返回默认值 0（非租户模式）</p>
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        Long tenantId = TENANT_HOLDER.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    /**
     * 清除当前线程的租户ID
     */
    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
package com.istream.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.istream.framework.security.SecurityUtils;
import com.istream.framework.tenant.TenantContext;
import com.istream.framework.tenant.TenantLineHandlerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置
 *
 * <p>包含分页插件（自动识别数据库类型）、多租户插件、防止全表更新/删除插件、自动填充处理器。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MyBatisPlusConfig {

    private final TenantLineHandlerImpl tenantLineHandler;

    /**
     * 分页插件 + 多租户插件 + 防止全表更新/删除
     *
     * <p>分页插件使用无参构造函数自动识别数据库类型，
     * 无需硬编码 MySQL/PostgreSQL，便于多数据库切换。</p>
     * <p>多租户插件在 saas.enabled=true 时自动追加 tenant_id 条件。</p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充处理器
     *
     * <p>自动填充 tenantId/createTime/updateTime/createBy/updateBy 字段。
     * 当未登录时（如匿名访问），createBy/updateBy 不填充。</p>
     * <p>tenantId 从 TenantContext 获取，SaaS 模式下自动隔离。</p>
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "tenantId", Long.class, TenantContext.getTenantId());
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                Long userId = getSafeUserId();
                if (userId != null) {
                    this.strictInsertFill(metaObject, "createBy", Long.class, userId);
                    this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                Long userId = getSafeUserId();
                if (userId != null) {
                    this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
                }
            }

            private Long getSafeUserId() {
                return SecurityUtils.getLoginUserId();
            }
        };
    }
}
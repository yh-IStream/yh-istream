package com.istream.framework.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * MyBatis-Plus 多租户拦截器
 *
 * <p>自动在 SQL 的 WHERE 条件中追加 tenant_id = ? 实现数据隔离。</p>
 * <p>通过 {@code saas.ignore-tables} 配置需要忽略的表（如系统配置表、日志表等全局表）。</p>
 * <p>当 SaaS 模式未启用时（saas.enabled=false），所有表均被忽略，不追加租户条件。</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantLineHandlerImpl implements TenantLineHandler {

    @Value("${saas.enabled:false}")
    private boolean saasEnabled;

    @Value("${saas.ignore-tables:}")
    private String ignoreTablesConfig;

    private static final String TENANT_ID_COLUMN = "tenant_id";

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContext.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (!saasEnabled) {
            return true;
        }
        return getIgnoreTables().contains(tableName);
    }

    /**
     * 获取忽略租户隔离的表名集合
     *
     * @return 忽略的表名集合
     */
    private Set<String> getIgnoreTables() {
        if (ignoreTablesConfig == null || ignoreTablesConfig.isBlank()) {
            return Set.of();
        }
        return Set.of(ignoreTablesConfig.split(","));
    }
}
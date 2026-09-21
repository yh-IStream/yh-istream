package com.istream.framework.tenant;

import net.sf.jsqlparser.expression.LongValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TenantLineHandlerImpl 单元测试
 *
 * <p>验证 MyBatis-Plus 多租户拦截器的核心行为：租户ID获取、列名、忽略表逻辑。</p>
 *
 * @author istream
 * @since 2026-09-19
 */
@DisplayName("TenantLineHandlerImpl 多租户拦截器")
class TenantLineHandlerImplTest {

    private TenantLineHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new TenantLineHandlerImpl();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("getTenantIdColumn 返回 tenant_id")
    void shouldReturnTenantIdColumn() {
        assertEquals("tenant_id", handler.getTenantIdColumn());
    }

    @Test
    @DisplayName("getTenantId 从 TenantContext 获取当前租户ID")
    void shouldReturnTenantIdFromContext() {
        TenantContext.setTenantId(100L);
        LongValue expression = (LongValue) handler.getTenantId();
        assertEquals(100L, expression.getValue());
    }

    @Test
    @DisplayName("getTenantId 未设置时返回默认值0")
    void shouldReturnDefaultTenantIdWhenNotSet() {
        LongValue expression = (LongValue) handler.getTenantId();
        assertEquals(0L, expression.getValue());
    }

    @Test
    @DisplayName("SaaS 未启用时所有表都被忽略")
    void shouldIgnoreAllTablesWhenSaasDisabled() {
        ReflectionTestUtils.setField(handler, "saasEnabled", false);
        ReflectionTestUtils.setField(handler, "ignoreTablesConfig", "");

        assertTrue(handler.ignoreTable("sys_user"));
        assertTrue(handler.ignoreTable("biz_order"));
    }

    @Test
    @DisplayName("SaaS 启用时未配置忽略表的表不被忽略")
    void shouldNotIgnoreTableWhenSaasEnabledAndNoIgnoreConfig() {
        ReflectionTestUtils.setField(handler, "saasEnabled", true);
        ReflectionTestUtils.setField(handler, "ignoreTablesConfig", "");

        assertFalse(handler.ignoreTable("sys_user"));
    }

    @Test
    @DisplayName("SaaS 启用时配置的忽略表被忽略")
    void shouldIgnoreConfiguredTablesWhenSaasEnabled() {
        ReflectionTestUtils.setField(handler, "saasEnabled", true);
        ReflectionTestUtils.setField(handler, "ignoreTablesConfig", "sys_config,sys_dict_data,sys_dict_type");

        assertTrue(handler.ignoreTable("sys_config"));
        assertTrue(handler.ignoreTable("sys_dict_data"));
        assertTrue(handler.ignoreTable("sys_dict_type"));
        assertFalse(handler.ignoreTable("sys_user"));
    }

    @Test
    @DisplayName("忽略表配置含空格时仍能正确解析")
    void shouldHandleIgnoreConfigWithSpaces() {
        ReflectionTestUtils.setField(handler, "saasEnabled", true);
        ReflectionTestUtils.setField(handler, "ignoreTablesConfig", "sys_config , sys_dict_data ");

        assertTrue(handler.ignoreTable("sys_config"));
        assertFalse(handler.ignoreTable("sys_user"));
    }

    @Test
    @DisplayName("忽略表配置为空字符串时所有表都不忽略")
    void shouldNotIgnoreAnyTableWhenConfigIsBlank() {
        ReflectionTestUtils.setField(handler, "saasEnabled", true);
        ReflectionTestUtils.setField(handler, "ignoreTablesConfig", "");

        assertFalse(handler.ignoreTable("sys_user"));
        assertFalse(handler.ignoreTable("biz_order"));
    }
}
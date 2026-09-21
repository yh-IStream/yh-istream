package com.istream.framework.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TenantContext 单元测试
 *
 * <p>验证 ThreadLocal 租户ID的设置、获取、默认值和清理行为。</p>
 *
 * @author istream
 * @since 2026-09-19
 */
@DisplayName("TenantContext 租户上下文")
class TenantContextTest {

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("未设置租户ID时返回默认值0")
    void shouldReturnDefaultWhenNotSet() {
        assertEquals(0L, TenantContext.getTenantId());
    }

    @Test
    @DisplayName("设置租户ID后能正确获取")
    void shouldReturnSetTenantId() {
        TenantContext.setTenantId(100L);
        assertEquals(100L, TenantContext.getTenantId());
    }

    @Test
    @DisplayName("clear后恢复默认值")
    void shouldResetToDefaultAfterClear() {
        TenantContext.setTenantId(200L);
        assertEquals(200L, TenantContext.getTenantId());

        TenantContext.clear();
        assertEquals(0L, TenantContext.getTenantId());
    }

    @Test
    @DisplayName("多次设置覆盖前值")
    void shouldOverwritePreviousValue() {
        TenantContext.setTenantId(1L);
        assertEquals(1L, TenantContext.getTenantId());

        TenantContext.setTenantId(2L);
        assertEquals(2L, TenantContext.getTenantId());
    }

    @Test
    @DisplayName("不同线程互不干扰")
    void shouldBeIsolatedBetweenThreads() throws InterruptedException {
        TenantContext.setTenantId(1L);

        Thread other = new Thread(() -> {
            TenantContext.setTenantId(2L);
        });
        other.start();
        other.join();

        assertEquals(1L, TenantContext.getTenantId());
    }
}
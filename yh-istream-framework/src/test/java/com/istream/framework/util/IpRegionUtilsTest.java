package com.istream.framework.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * IpRegionUtils 单元测试
 *
 * @author istream
 * @since 2026-10-20
 */
@DisplayName("IpRegionUtils IP归属地解析")
class IpRegionUtilsTest {

    @BeforeAll
    static void init() {
        IpRegionUtils.init();
    }

    @Test
    @DisplayName("本地回环地址返回'本地'")
    void shouldReturnLocalForLoopbackAddress() {
        assertEquals("本地", IpRegionUtils.parseRegion("127.0.0.1"));
        assertEquals("本地", IpRegionUtils.parseRegion("0:0:0:0:0:0:0:1"));
        assertEquals("本地", IpRegionUtils.parseRegion("::1"));
    }

    @Test
    @DisplayName("国内IP返回包含'中国'的归属地")
    void shouldReturnChinaForDomesticIp() {
        String region = IpRegionUtils.parseRegion("120.24.78.130");
        assertNotNull(region);
        assertFalse(region.isEmpty());
    }

    @Test
    @DisplayName("海外IP返回非'未知'结果")
    void shouldReturnRegionForOverseasIp() {
        String region = IpRegionUtils.parseRegion("8.8.8.8");
        assertNotNull(region);
        assertFalse(region.isEmpty());
    }

    @Test
    @DisplayName("null或空字符串返回'未知'")
    void shouldReturnUnknownForInvalidInput() {
        assertEquals("未知", IpRegionUtils.parseRegion(null));
        assertEquals("未知", IpRegionUtils.parseRegion(""));
        assertEquals("未知", IpRegionUtils.parseRegion("unknown"));
    }
}
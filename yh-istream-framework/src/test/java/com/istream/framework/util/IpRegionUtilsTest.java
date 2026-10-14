package com.istream.framework.util;


import org.junit.jupiter.api.Test;

/**
 * IpRegionUtils 测试类
 * <p>用于验证不同运营商、不同地区的 IP 解析结果</p>
 */
public class IpRegionUtilsTest {

    @Test
    public void testParseRegion() {
        String[] testIps = {
                "120.24.78.130",    // 广东深圳，阿里云
                "114.114.114.114",  // 江苏南京，电信（知名公共 DNS）
                "223.5.5.5",        // 阿里云 DNS
                "111.55.11.120",    // 中国移动（大概率只有运营商）
                "117.136.0.1",      // 北京，中国移动
                "8.8.8.8",          // 美国，Google（测试海外 IP）
                "127.0.0.1"         // 本地回环地址
        };

        System.out.println("========== 开始测试 ip2region 解析 ==========");
        
        for (String ip : testIps) {
            long start = System.currentTimeMillis();
            String region = IpRegionUtils.parseRegion(ip);
            long cost = System.currentTimeMillis() - start;
            
            System.out.printf("IP: %-18s => 解析结果: %-20s (耗时: %d ms)%n", ip, region, cost);
        }
        
        System.out.println("========== 测试结束 ==========");
    }
}
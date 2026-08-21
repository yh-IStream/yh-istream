package com.istream.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * IP 归属地解析工具类
 *
 * <p>基于 ip2region 离线库，支持将 IP 地址解析为"国家|区域|省份|城市|ISP"格式。</p>
 *
 * <p>使用前需将 {@code ip2region.xdb} 文件放入 {@code src/main/resources/ip2region/} 目录。
 * 下载地址：<a href="https://github.com/lionsoul2014/ip2region">ip2region GitHub</a></p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   String region = IpRegionUtils.parseRegion("120.24.78.130");
 *   // 返回: "中国|广东省|深圳市"
 * }</pre>
 *
 * @author isteam
 * @since 2026-08-17
 */
@Slf4j
public final class IpRegionUtils {

    private static final String XDB_CLASSPATH = "ip2region/ip2region.xdb";
    private static final String UNKNOWN = "未知";

    private static volatile Searcher searcher;
    private static volatile boolean initialized;
    private static final Object LOCK = new Object();

    private IpRegionUtils() {
    }

    /**
     * 解析 IP 归属地，返回格式：国家|省份|城市
     *
     * @param ip IP 地址，如 "120.24.78.130"
     * @return 归属地字符串，如 "中国|广东省|深圳市"；解析失败返回 "未知"
     */
    public static String parseRegion(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return UNKNOWN;
        }
        ensureInitialized();
        if (searcher == null) {
            return UNKNOWN;
        }
        try {
            String region = searcher.search(ip);
            return formatRegion(region);
        } catch (Exception e) {
            log.debug("IP 归属地解析失败: ip={}", ip, e);
            return UNKNOWN;
        }
    }

    private static void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            initialized = true;
            try {
                InputStream is = IpRegionUtils.class.getClassLoader()
                        .getResourceAsStream(XDB_CLASSPATH);
                if (is == null) {
                    log.warn("ip2region.xdb 文件未找到，IP 归属地解析功能不可用。"
                            + "请将文件放入 src/main/resources/ip2region/ip2region.xdb");
                    return;
                }
                byte[] bytes = is.readAllBytes();
                searcher = Searcher.newWithBuffer(bytes);
                log.info("ip2region 初始化成功，数据文件大小: {} KB", bytes.length / 1024);
            } catch (Exception e) {
                log.error("ip2region 初始化失败", e);
            }
        }
    }

    /**
     * 格式化 ip2region 返回的原始字符串
     * <p>原始格式：国家|区域|省份|城市|ISP</p>
     * <p>目标格式：国家|省份|城市（去除空段和 0）</p>
     */
    private static String formatRegion(String raw) {
        if (raw == null || raw.isEmpty()) {
            return UNKNOWN;
        }
        String[] parts = raw.split("\\|");
        if (parts.length < 5) {
            return UNKNOWN;
        }
        StringBuilder sb = new StringBuilder();
        appendNonEmpty(sb, parts[0]); // 国家
        appendNonEmpty(sb, parts[2]); // 省份
        appendNonEmpty(sb, parts[3]); // 城市
        if (sb.isEmpty()) {
            return UNKNOWN;
        }
        return sb.toString();
    }

    private static void appendNonEmpty(StringBuilder sb, String part) {
        if (part == null || part.isEmpty() || "0".equals(part)) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append('|');
        }
        sb.append(part);
    }
}
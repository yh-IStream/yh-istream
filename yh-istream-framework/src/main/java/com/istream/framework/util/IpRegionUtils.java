package com.istream.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.InputStream;

/**
 * IP 归属地解析工具类
 *
 * <p>基于 ip2region 离线库，支持将 IP 地址解析为"国家|区域|省份|城市|ISP"格式。</p>
 * <p>使用示例：</p>
 * <pre>{@code
 *   String region = IpRegionUtils.parseRegion("120.24.78.130");
 *   // 返回: "中国|广东省|深圳市"
 * }</pre>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
public final class IpRegionUtils {

    private static final String XDB_CLASSPATH = "ip2region/ip2region.xdb";
    private static final String UNKNOWN = "未知";

    /**
     * ISP 关键词字典（区分城市字段和运营商字段）
     */
    private static final String[] ISP_KEYWORDS = {
            "移动", "联通", "电信", "铁通", "教育网", "广电网",
            "阿里", "腾讯", "华为", "亚马逊", "百度",
            "Google", "Level3", "CloudFlare", "Microsoft"
    };

    private static volatile Searcher searcher;
    private static volatile boolean initialized;
    private static final Object LOCK = new Object();

    private IpRegionUtils() {
    }

    public static void init() {
        ensureInitialized();
    }

    /**
     * 解析 IP 归属地，返回格式：国家|省份|城市 或 国家|ISP
     *
     * @param ip IP 地址，如 "120.24.78.130"
     * @return 归属地字符串，如 "中国|广东省|深圳市"；解析失败返回 "未知"
     */
    public static String parseRegion(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return UNKNOWN;
        }
        // 本地回环地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            return "本地";
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
            try (InputStream is = IpRegionUtils.class.getClassLoader()
                    .getResourceAsStream(XDB_CLASSPATH)) {
                if (is == null) {
                    log.warn("ip2region.xdb 文件未找到，IP 归属地解析功能不可用。");
                    return;
                }
                byte[] bytes = is.readAllBytes();
                searcher = Searcher.newWithBuffer(bytes);
                initialized = true;
                log.info("ip2region 初始化成功，数据文件大小: {} KB", bytes.length / 1024);
            } catch (Exception e) {
                log.error("ip2region 初始化失败", e);
            }
        }
    }

    /**
     * 格式化 ip2region 返回的原始字符串
     * <p>原始格式：国家|区域|省份|城市|ISP</p>
     */
    private static String formatRegion(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }
        String[] parts = raw.split("\\|");
        if (parts.length < 5) {
            return UNKNOWN;
        }

        StringBuilder sb = new StringBuilder();
        // 1. 国家
        appendNonEmpty(sb, parts[0]);

        String ispFallback = null;

        // 2. 遍历中间字段 (从索引 1 到倒数第二个)
        for (int i = 1; i < parts.length - 1; i++) {
            String part = parts[i].trim();

            // 过滤无效占位符
            if (part.isBlank() || "0".equals(part) || "Reserved".equalsIgnoreCase(part)) {
                continue;
            }

            // 识别是否是 ISP，暂存兜底
            if (isIsp(part)) {
                ispFallback = part;
                continue;
            }

            appendNonEmpty(sb, part);
        }

        // 3. 兜底
        if (sb.indexOf("|") == -1 && ispFallback != null) {
            sb.append('|').append(ispFallback);
        }

        return sb.isEmpty() ? UNKNOWN : sb.toString();
    }

    /**
     * 判断是否属于运营商/ISP
     */
    private static boolean isIsp(String str) {
        for (String isp : ISP_KEYWORDS) {
            if (str.contains(isp)) {
                return true;
            }
        }
        return false;
    }

    private static void appendNonEmpty(StringBuilder sb, String part) {
        if (part == null || part.isBlank() || "0".equals(part)) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append('|');
        }
        sb.append(part);
    }
}
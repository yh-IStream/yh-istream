package com.istream.framework.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类
 * <p>
 * 从 HttpServletRequest 中提取客户端真实 IP，支持反向代理场景
 * （X-Forwarded-For / X-Real-IP）
 *
 * @author istream
 * @since 2026-08-17
 */
public final class IpUtils {

    private IpUtils() {
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 优先级：X-Forwarded-For → X-Real-IP → remoteAddr
     *
     * @param request HTTP 请求
     * @return 客户端 IP，无法获取时返回 "unknown"
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalid(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalid(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private static boolean isInvalid(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}
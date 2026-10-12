package com.istream.framework.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent 解析工具类
 *
 * <p>从 HTTP 请求的 User-Agent 头中提取浏览器名称和操作系统信息，
 * 覆盖主流浏览器和操作系统。</p>
 *
 * @author istream
 * @since 2026-10-12
 */
public final class UserAgentUtils {

    private static final String UNKNOWN = "未知";

    private UserAgentUtils() {
    }

    private static final Pattern OS_PATTERN = Pattern.compile(
            "Windows NT (?<ver>\\d+[._]\\d+)|"
                    + "Windows (?<win>\\d+)|"
                    + "Mac OS X (?<mac>[\\d._]+)|"
                    + "Android (?<android>[\\d.]+)|"
                    + "iPhone OS (?<ios>\\d+[._]\\d+)|"
                    + "iPad.*?OS (?<ipados>\\d+[._]\\d+)|"
                    + "Linux"
    );

    private static final Pattern BROWSER_PATTERN = Pattern.compile(
            "Edg/(?<edge>[\\d.]+)|"
                    + "OPR/(?<opera>[\\d.]+)|"
                    + "Vivaldi/(?<vivaldi>[\\d.]+)|"
                    + "Firefox/(?<firefox>[\\d.]+)|"
                    + "Chrome/(?<chrome>[\\d.]+)|"
                    + "Version/(?<safari>[\\d.]+) Safari/"
    );

    /**
     * 从 HTTP 请求中解析浏览器名称
     *
     * @param request HTTP 请求
     * @return 浏览器名称及版本，如 "Chrome 120.0"；无法识别时返回 "未知"
     */
    public static String parseBrowser(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        return parseBrowser(request.getHeader("User-Agent"));
    }

    /**
     * 从 User-Agent 字符串中解析浏览器名称
     *
     * @param ua User-Agent 字符串
     * @return 浏览器名称及版本
     */
    public static String parseBrowser(String ua) {
        if (ua == null || ua.isBlank()) {
            return UNKNOWN;
        }
        Matcher m = BROWSER_PATTERN.matcher(ua);
        if (m.find()) {
            if (m.group("edge") != null) {
                return "Edge " + m.group("edge");
            }
            if (m.group("opera") != null) {
                return "Opera " + m.group("opera");
            }
            if (m.group("vivaldi") != null) {
                return "Vivaldi " + m.group("vivaldi");
            }
            if (m.group("firefox") != null) {
                return "Firefox " + m.group("firefox");
            }
            if (m.group("chrome") != null) {
                return "Chrome " + m.group("chrome");
            }
            if (m.group("safari") != null) {
                return "Safari " + m.group("safari");
            }
        }
        return UNKNOWN;
    }

    /**
     * 从 HTTP 请求中解析操作系统
     *
     * @param request HTTP 请求
     * @return 操作系统名称及版本，如 "Windows 10"；无法识别时返回 "未知"
     */
    public static String parseOs(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        return parseOs(request.getHeader("User-Agent"));
    }

    /**
     * 从 User-Agent 字符串中解析操作系统
     *
     * @param ua User-Agent 字符串
     * @return 操作系统名称及版本
     */
    public static String parseOs(String ua) {
        if (ua == null || ua.isBlank()) {
            return UNKNOWN;
        }
        Matcher m = OS_PATTERN.matcher(ua);
        if (m.find()) {
            if (m.group("win") != null) {
                return "Windows " + m.group("win");
            }
            if (m.group("ver") != null) {
                return "Windows " + mapWindowsNtVersion(m.group("ver"));
            }
            if (m.group("mac") != null) {
                return "Mac OS X " + m.group("mac").replace('_', '.');
            }
            if (m.group("android") != null) {
                return "Android " + m.group("android");
            }
            if (m.group("ios") != null) {
                return "iOS " + m.group("ios").replace('_', '.');
            }
            if (m.group("ipados") != null) {
                return "iPadOS " + m.group("ipados").replace('_', '.');
            }
            return "Linux";
        }
        return UNKNOWN;
    }

    private static String mapWindowsNtVersion(String ver) {
        return switch (ver) {
            case "10.0" -> "10/11";
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            case "6.0" -> "Vista";
            case "5.1" -> "XP";
            default -> "NT " + ver;
        };
    }
}
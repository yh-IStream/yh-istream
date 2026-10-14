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

    /**
     * 按优先级从高到低排列
     */
    private static final Pattern[] BROWSER_PATTERNS = {
            Pattern.compile("Edg/([\\d.]+)"),
            Pattern.compile("OPR/([\\d.]+)"),
            Pattern.compile("Vivaldi/([\\d.]+)"),
            Pattern.compile("Firefox/([\\d.]+)"),
            Pattern.compile("MicroMessenger/([\\d.]+)"),
            Pattern.compile("QQBrowser/([\\d.]+)"),
            Pattern.compile("UCBrowser/([\\d.]+)"),
            Pattern.compile("Chrome/([\\d.]+)"),
            Pattern.compile("Version/([\\d.]+) Safari/")
    };

    private static final String[] BROWSER_NAMES = {
            "Edge", "Opera", "Vivaldi", "Firefox", "MicroMessenger", "QQBrowser", "UCBrowser", "Chrome", "Safari"
    };


    /**
     * 命名捕获组，按匹配位置扫描
     */
    private static final Pattern OS_PATTERN = Pattern.compile(
            "Windows NT (?<ver>\\d+[._]\\d+)|"
                    + "Windows (?<win>\\d+)|"
                    + "Macintosh.*?Mobile.*?OS (?<ipadosDesktop>\\d+[._]\\d+)|"
                    + "iPad.*?OS (?<ipados>\\d+[._]\\d+)|"
                    + "iPhone OS (?<ios>\\d+[._]\\d+)|"
                    + "Mac OS X (?<mac>[\\d._]+)|"
                    + "Android (?<android>[\\d.]+)|"
                    + "CrOS \\S+ (?<cros>[\\d.]+)|"
                    + "HarmonyOS (?<harmony>[\\d.]+)|"
                    + "Linux"
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
        for (int i = 0; i < BROWSER_PATTERNS.length; i++) {
            Matcher m = BROWSER_PATTERNS[i].matcher(ua);
            if (m.find()) {
                return BROWSER_NAMES[i] + " " + m.group(1);
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
            if (m.group("ver") != null) {
                return "Windows " + mapWindowsNtVersion(m.group("ver"));
            }
            if (m.group("win") != null) {
                return "Windows " + m.group("win");
            }
            if (m.group("ipadosDesktop") != null) {
                return "iPadOS " + normalizeVersion(m.group("ipadosDesktop"));
            }
            if (m.group("ipados") != null) {
                return "iPadOS " + normalizeVersion(m.group("ipados"));
            }
            if (m.group("ios") != null) {
                return "iOS " + normalizeVersion(m.group("ios"));
            }
            if (m.group("mac") != null) {
                return "Mac OS X " + normalizeVersion(m.group("mac"));
            }
            if (m.group("android") != null) {
                return "Android " + m.group("android");
            }
            if (m.group("cros") != null) {
                return "ChromeOS " + m.group("cros");
            }
            if (m.group("harmony") != null) {
                return "HarmonyOS " + m.group("harmony");
            }
            return "Linux";
        }
        return UNKNOWN;
    }

    /**
     * Windows NT 版本号 → 市场名称
     */
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

    /**
     * 下划线规范化，如 "17_2" → "17.2"
     */
    private static String normalizeVersion(String ver) {
        return ver.replace('_', '.');
    }
}
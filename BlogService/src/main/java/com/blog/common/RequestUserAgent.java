package com.blog.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestUserAgent {
    private static final Logger log = LoggerFactory.getLogger(RequestUserAgent.class);

    public static String clientIp(HttpServletRequest request) {
        //以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
        String ipAddress = request.getHeader("X-Original-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Forwarded-For");
        }
        // 获取nginx等代理的IP
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("x-forwarded-for");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error("unknown host exception, {}", e.getMessage());
                }
                if (inet != null) {
                    ipAddress = inet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress != null && ipAddress.indexOf(",") > 0) { //"***.***.***.***".length() = 15
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        return ipAddress;
    }

    public static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    public static String parseBrowser(String ua) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        if (containsToken(ua, "Edg/")) {
            return "Microsoft Edge " + versionAfter(ua, "Edg/");
        }
        if (containsToken(ua, "OPR/") || containsToken(ua, "Opera/")) {
            return "Opera " + versionAfter(ua, containsToken(ua, "OPR/") ? "OPR/" : "Opera/");
        }
        if (containsToken(ua, "Firefox/")) {
            return "Firefox " + versionAfter(ua, "Firefox/");
        }
        if (containsToken(ua, "Chrome/")) {
            return "Chrome " + versionAfter(ua, "Chrome/");
        }
        if (containsToken(ua, "Version/") && containsToken(ua, "Safari/")) {
            return "Safari " + versionAfter(ua, "Version/");
        }
        if (containsToken(ua, "MSIE ") || containsToken(ua, "Trident/")) {
            return "IE";
        }
        return "Other";
    }

    public static boolean frontVisible(Integer handle, Integer visible) {
        return Integer.valueOf(1).equals(handle) && Integer.valueOf(1).equals(visible);
    }

    public static String parseOs(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return parseOs(request.getHeader("User-Agent"), request.getHeader("Sec-CH-UA-Platform-Version"));
    }

    public static String parseOs(String ua, String platformVersion) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        String text = ua.toLowerCase();
        if (text.contains("windows")) {
            return parseWindows(ua, platformVersion);
        }
        if (text.contains("android")) {
            String v = versionToken(ua, "Android ");
            return StringUtils.hasText(v) ? "Android " + majorVersion(v) : "Android";
        }
        if (text.contains("iphone") || text.contains("ipad") || text.contains("ios")) {
            String v = versionToken(ua, "iPhone OS ");
            if (!StringUtils.hasText(v)) {
                v = versionToken(ua, "CPU OS ");
            }
            return StringUtils.hasText(v) ? "iOS " + dottedVersion(v) : "iOS";
        }
        if (text.contains("mac os") || text.contains("macintosh")) {
            String v = versionToken(ua, "Mac OS X ");
            return StringUtils.hasText(v) ? "macOS " + dottedVersion(v) : "macOS";
        }
        if (text.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    private static String parseWindows(String ua, String platformVersion) {
        Integer chMajor = platformMajor(platformVersion);
        if (chMajor != null && chMajor >= 13) {
            return "Windows 11";
        }
        String nt = versionToken(ua, "Windows NT ");
        if (nt.startsWith("10.") || "10".equals(nt)) {
            return "Windows 10";
        }
        if (nt.startsWith("6.3")) {
            return "Windows 8.1";
        }
        if (nt.startsWith("6.2")) {
            return "Windows 8";
        }
        if (nt.startsWith("6.1")) {
            return "Windows 7";
        }
        if (nt.startsWith("6.0")) {
            return "Windows Vista";
        }
        if (nt.startsWith("5.")) {
            return "Windows XP";
        }
        return "Windows";
    }

    private static Integer platformMajor(String platformVersion) {
        if (!StringUtils.hasText(platformVersion)) {
            return null;
        }
        String value = platformVersion.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        int dot = value.indexOf('.');
        String major = dot < 0 ? value : value.substring(0, dot);
        try {
            return Integer.parseInt(major);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String versionToken(String ua, String token) {
        int start = ua.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();
        int end = start;
        while (end < ua.length()) {
            char ch = ua.charAt(end);
            if (!Character.isDigit(ch) && ch != '.' && ch != '_') {
                break;
            }
            end++;
        }
        return ua.substring(start, end);
    }

    private static String majorVersion(String version) {
        int cut = version.length();
        int dot = version.indexOf('.');
        int under = version.indexOf('_');
        if (dot >= 0) {
            cut = Math.min(cut, dot);
        }
        if (under >= 0) {
            cut = Math.min(cut, under);
        }
        return version.substring(0, cut);
    }

    private static String dottedVersion(String version) {
        String dotted = version.replace('_', '.');
        String[] parts = dotted.split("\\.");
        if (parts.length >= 2 && StringUtils.hasText(parts[0]) && StringUtils.hasText(parts[1])) {
            return parts[0] + "." + parts[1];
        }
        return parts[0];
    }

    private static boolean containsToken(String ua, String token) {
        return ua.contains(token);
    }

    private static String versionAfter(String ua, String token) {
        int start = ua.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();
        int end = start;
        while (end < ua.length()) {
            char ch = ua.charAt(end);
            if (!Character.isDigit(ch)) {
                break;
            }
            end++;
        }
        return ua.substring(start, end);
    }
}

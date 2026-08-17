package com.blog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@Service
public class IpLocationService {
    public record Location(String province, String city, String district) {
        static Location empty() {
            return new Location(null, null, null);
        }
    }

    @Value("${blog.map.qq-key:}")
    private String qqKey;

    @Resource
    private ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        restClient = RestClient.builder().requestFactory(factory).build();
    }

    public Location lookup(String ip) {
        String queryIp = normalizeIp(ip);
        if (!StringUtils.hasText(queryIp) || isPrivate(queryIp) || !StringUtils.hasText(qqKey)) {
            return Location.empty();
        }
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://apis.map.qq.com/ws/location/v1/ip")
                    .queryParam("ip", queryIp)
                    .queryParam("key", qqKey)
                    .encode()
                    .build()
                    .toUri();
            String json = restClient.get().uri(uri).retrieve().body(String.class);
            if (!StringUtils.hasText(json)) {
                return Location.empty();
            }
            JsonNode root = objectMapper.readTree(json);
            if (root.path("status").asInt(-1) != 0) {
                return Location.empty();
            }
            JsonNode info = root.path("result").path("ad_info");
            return new Location(
                    text(info, "province"),
                    text(info, "city"),
                    text(info, "district")
            );
        } catch (Exception ignored) {
            return Location.empty();
        }
    }

    private static String normalizeIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return null;
        }
        String value = ip.trim();
        if (value.startsWith("::ffff:")) {
            value = value.substring(7);
        }
        return value;
    }

    private static boolean isPrivate(String ip) {
        String lower = ip.toLowerCase();
        return "127.0.0.1".equals(ip)
                || "localhost".equals(lower)
                || "::1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("169.254.")
                || ip.startsWith("fe80:")
                || isCarrierGradeOrPrivate172(ip);
    }

    private static boolean isCarrierGradeOrPrivate172(String ip) {
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String text(JsonNode info, String field) {
        String value = info.path(field).asText(null);
        if (!StringUtils.hasText(value) || "未知".equals(value)) {
            return null;
        }
        return value.length() > 30 ? value.substring(0, 30) : value;
    }
}

package com.blog.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public interface ObjectStorage {
    int MAX_DELETE_BATCH = 1000;

    String put(String key, byte[] data, String contentType);

    void delete(String key);

    default void deleteAll(List<String> keys) {
        List<String> unique = distinctKeys(keys);
        if (unique.isEmpty()) {
            return;
        }
        for (String key : unique) {
            delete(key);
        }
    }

    String publicUrl(String key);

    String extractKey(String url);

    static String joinUrl(String domain, String key) {
        String base = domain == null ? "" : domain.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = key == null ? "" : key.trim();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (base.isEmpty()) {
            return "/" + path;
        }
        return base + "/" + path;
    }

    static String stripPrefix(String url, String prefix) {
        if (url == null || prefix == null || prefix.isBlank()) {
            return null;
        }
        String value = url.trim();
        String base = prefix.trim();
        if (value.startsWith(base)) {
            String key = value.substring(base.length());
            while (key.startsWith("/")) {
                key = key.substring(1);
            }
            return key.isBlank() ? null : key;
        }
        return null;
    }

    static List<String> distinctKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = key.trim();
            while (value.startsWith("/")) {
                value = value.substring(1);
            }
            if (!value.isBlank()) {
                unique.add(value);
            }
        }
        return new ArrayList<>(unique);
    }
}

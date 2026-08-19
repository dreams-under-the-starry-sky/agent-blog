package com.blog.storage;

public interface ObjectStorage {
    String put(String key, byte[] data, String contentType);

    void delete(String key);

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
}

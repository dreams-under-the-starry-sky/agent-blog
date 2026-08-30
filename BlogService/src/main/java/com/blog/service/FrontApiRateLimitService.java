package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FrontApiRateLimitService {
    static final int MAX_HITS = 8;
    static final long WINDOW_MS = 10_000;

    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public void assertAllowed(String method, String servletPath) {
        String key = method.toUpperCase() + " " + normalize(servletPath);
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(false);
        hits.compute(key, (ignored, queue) -> {
            Deque<Long> window = queue == null ? new ArrayDeque<>() : queue;
            long cutoff = now - WINDOW_MS;
            while (!window.isEmpty() && window.peekFirst() <= cutoff) {
                window.pollFirst();
            }
            if (window.size() >= MAX_HITS) {
                return window;
            }
            window.addLast(now);
            allowed.set(true);
            return window;
        });
        if (!allowed.get()) {
            throw new BizException(ErrorCode.FRONT_API_TOO_FREQUENT);
        }
    }

    static String normalize(String servletPath) {
        if (servletPath == null || servletPath.isEmpty()) {
            return "/";
        }
        StringBuilder out = new StringBuilder();
        for (String part : servletPath.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            out.append('/');
            if (isIdSegment(part)) {
                out.append("{id}");
            } else {
                out.append(part);
            }
        }
        return out.isEmpty() ? "/" : out.toString();
    }

    private static boolean isIdSegment(String part) {
        if (part.isEmpty()) {
            return false;
        }
        for (int i = 0; i < part.length(); i++) {
            if (!Character.isDigit(part.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

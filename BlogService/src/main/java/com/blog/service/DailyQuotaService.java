package com.blog.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DailyQuotaService {
    private volatile LocalDate day = LocalDate.now();
    private final AtomicInteger qq = new AtomicInteger();
    private final AtomicInteger ip = new AtomicInteger();

    public boolean tryAcquireQq(int limit) {
        return tryAcquire(qq, limit);
    }

    public boolean tryAcquireIp(int limit) {
        return tryAcquire(ip, limit);
    }

    private synchronized boolean tryAcquire(AtomicInteger counter, int limit) {
        LocalDate today = LocalDate.now();
        if (!today.equals(day)) {
            day = today;
            qq.set(0);
            ip.set(0);
        }
        return counter.incrementAndGet() <= limit;
    }
}

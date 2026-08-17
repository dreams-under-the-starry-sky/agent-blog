package com.blog.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class IdGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private IdGenerator() {
    }

    public static long nextId() {
        String time = LocalDateTime.now().format(FORMATTER);
        int suffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return Long.parseLong(time + suffix);
    }
}

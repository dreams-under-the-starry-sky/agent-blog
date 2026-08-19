package com.blog.service;

import com.blog.entity.BlogLog;
import com.blog.mapper.BlogLogMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LogService {
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    @Resource
    private BlogLogMapper blogLogMapper;

    public void record(String event, String status, String detail) {
        BlogLog row = new BlogLog();
        row.setEvent(event);
        row.setStatus(status);
        row.setExeTime(0);
        row.setDetail(clip(detail));
        blogLogMapper.insert(row);
    }

    public void recordFail(String event, String detail) {
        writeFail(event, detail);
    }

    public void recordFail(String event, Throwable error) {
        writeFail(event, describe(error));
    }

    public void recordFail(String event, String hint, Throwable error) {
        writeFail(event, join(hint, describe(error)));
    }

    private void writeFail(String event, String detail) {
        try {
            record(event, "失败", detail);
        } catch (Exception e) {
            log.warn("写入运行日志失败", e);
        }
    }

    static String describe(Throwable error) {
        if (error == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Throwable current = error;
        for (int depth = 0; current != null && depth < 3; depth++) {
            if (depth > 0) {
                builder.append(" <- ");
            }
            builder.append(current.getClass().getSimpleName());
            String message = current.getMessage();
            if (StringUtils.hasText(message)) {
                builder.append(": ").append(message.replaceAll("\\s+", " ").trim());
            }
            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
        return builder.isEmpty() ? error.getClass().getSimpleName() : builder.toString();
    }

    private static String join(String hint, String detail) {
        if (!StringUtils.hasText(hint)) {
            return detail;
        }
        if (!StringUtils.hasText(detail)) {
            return hint;
        }
        return hint + " | " + detail;
    }

    private static String clip(String detail) {
        if (detail == null) {
            return null;
        }
        return detail.length() > 255 ? detail.substring(0, 255) : detail;
    }
}

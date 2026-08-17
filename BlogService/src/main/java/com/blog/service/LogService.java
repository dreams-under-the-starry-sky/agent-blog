package com.blog.service;

import com.blog.entity.BlogLog;
import com.blog.mapper.BlogLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    @Resource
    private BlogLogMapper blogLogMapper;

    public void record(String event, String status, String detail) {
        BlogLog log = new BlogLog();
        log.setEvent(event);
        log.setStatus(status);
        log.setExeTime(0);
        log.setDetail(detail == null ? null : (detail.length() > 255 ? detail.substring(0, 255) : detail));
        blogLogMapper.insert(log);
    }
}

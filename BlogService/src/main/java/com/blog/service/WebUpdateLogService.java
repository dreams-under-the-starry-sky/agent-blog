package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.entity.WebUpdateLog;
import com.blog.mapper.WebUpdateLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebUpdateLogService {
    @Resource
    private WebUpdateLogMapper webUpdateLogMapper;
    @Resource
    private LogService logService;

    public List<WebUpdateLog> list() {
        return webUpdateLogMapper.selectAll();
    }

    public Long save(WebUpdateLog log) {
        log.setTitle(log.getTitle().trim());
        String description = log.getDescription() == null ? "" : log.getDescription().trim();
        log.setDescription(description.length() > 500 ? description.substring(0, 500) : description);
        if (log.getTitle().length() > 80) {
            log.setTitle(log.getTitle().substring(0, 80));
        }
        if (log.getId() == null) {
            log.setId(IdGenerator.nextId());
            webUpdateLogMapper.insert(log);
            logService.record("新增功能日志", "成功", log.getTitle());
        } else {
            webUpdateLogMapper.update(log);
            logService.record("修改功能日志", "成功", log.getTitle());
        }
        return log.getId();
    }

    public void delete(Long id) {
        webUpdateLogMapper.deleteById(id);
        logService.record("删除功能日志", "成功", String.valueOf(id));
    }
}

package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.entity.Black;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.MessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FrontReplyLimitService {
    private static final Logger log = LoggerFactory.getLogger(FrontReplyLimitService.class);
    private static final int DAILY_LIMIT = 5;

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private IpLocationService ipLocationService;
    @Resource
    private LogService logService;

    public void assertVisitorAllowed(HttpServletRequest request, String nickname, String email) {
        String ip = CommentService.clientIp(request);
        if (blackMapper.countMatch(ip, nickname, email) > 0) {
            throw new BizException(ErrorCode.USER_BLACKLISTED);
        }
        assertAllowed(request, nickname, email);
    }

    public void assertAllowed(HttpServletRequest request, String nickname, String email) {
        String ip = CommentService.clientIp(request);
        LocalDateTime start = LocalDate.now().atStartOfDay();
        int byIp = commentMapper.countByIpSince(ip, start) + messageMapper.countByIpSince(ip, start);
        int byEmail = 0;
        if (StringUtils.hasText(email)) {
            String mail = email.trim();
            byEmail = commentMapper.countByEmailSince(mail, start) + messageMapper.countByEmailSince(mail, start);
        }
        if (byIp < DAILY_LIMIT && byEmail < DAILY_LIMIT) {
            return;
        }
        recordBlack(ip, nickname, email);
        throw new BizException(ErrorCode.POST_RATE_LIMITED);
    }

    private void recordBlack(String ip, String nickname, String email) {
        if (StringUtils.hasText(ip) && blackMapper.countMatch(ip, null, null) > 0) {
            return;
        }
        try {
            IpLocationService.Location location = ipLocationService.lookup(ip);
            String position = join(location.province(), location.city(), location.district());
            Black black = new Black();
            black.setId((int) (System.currentTimeMillis() / 1000) + ThreadLocalRandom.current().nextInt(10, 99));
            black.setIp(CommentService.trim(ip, 15));
            black.setPosition(CommentService.trim(position, 50));
            black.setNickname(CommentService.trim(nickname, 20));
            black.setEmail(CommentService.trim(email, 30));
            blackMapper.insert(black);
            logService.record("加入黑名单", "成功", ip);
        } catch (Exception e) {
            log.warn("记录黑名单失败", e);
            logService.recordFail("加入黑名单", e);
        }
    }

    private static String join(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
        return builder.isEmpty() ? null : builder.toString();
    }
}

package com.blog.service;

import com.blog.common.PageQuery;
import com.blog.dto.DashboardVO;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.BlogLogMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.FriendMapper;
import com.blog.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private BlogLogMapper blogLogMapper;

    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        PageQuery unhandled = new PageQuery();
        unhandled.setHandle(0);
        vo.setArticleCount(articleMapper.countAll());
        vo.setFriendCount(friendMapper.countAll());
        vo.setMessageCount(messageMapper.countPage(unhandled));
        vo.setCommentCount(commentMapper.countPage(unhandled));
        vo.setBlackCount(blackMapper.countAll());
        vo.setErrorLogCount(blogLogMapper.countFailed());
        vo.setHotArticles(articleMapper.selectHot(10));
        vo.setRecentBlacks(blackMapper.selectSince(LocalDate.now().minusDays(1).atStartOfDay()));
        return vo;
    }
}

package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.CommentSubmitRequest;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.CommentMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private IpLocationService ipLocationService;
    @Resource
    private FrontReplyLimitService frontReplyLimitService;

    public List<Comment> treeByArticle(Long articleId) {
        return toTree(commentMapper.selectByArticleId(articleId));
    }

    public PageResult<Comment> page(PageQuery query) {
        return new PageResult<>(commentMapper.countPage(query), commentMapper.selectPage(query));
    }

    public void submit(CommentSubmitRequest req, HttpServletRequest request, boolean asBlogger) {
        Article article = articleMapper.selectById(req.getArticleId());
        if (article == null) {
            throw new BizException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (!asBlogger && !Integer.valueOf(1).equals(article.getComment())) {
            throw new BizException(ErrorCode.COMMENT_CLOSED);
        }
        String ip = clientIp(request);
        assertNotBlacklisted(ip, req.getNickname(), req.getEmail());
        if (!asBlogger) {
            frontReplyLimitService.assertAllowed(request, req.getNickname(), req.getEmail());
        }
        Comment comment = new Comment();
        comment.setId(IdGenerator.nextId());
        comment.setArticleId(req.getArticleId());
        comment.setContent(req.getContent());
        comment.setBlogger(asBlogger ? 1 : 0);
        fillReplyMeta(comment, req.getParentId());
        comment.setNickname(trim(req.getNickname(), 20));
        comment.setEmail(trim(req.getEmail(), 30));
        comment.setWebsite(trim(req.getWebsite(), 50));
        comment.setHandle(asBlogger ? 1 : 0);
        comment.setNotice(req.getNotice() == null ? 0 : req.getNotice());
        comment.setSend(0);
        comment.setVisible(1);
        String ua = request.getHeader("User-Agent");
        comment.setBrowser(trim(parseBrowser(ua), 50));
        comment.setSystemInfo(trim(parseOs(ua), 20));
        comment.setIp(trim(ip, 15));
        IpLocationService.Location location = ipLocationService.lookup(ip);
        comment.setProvince(location.province());
        comment.setCity(location.city());
        comment.setDistrict(location.district());
        commentMapper.insert(comment);
        articleMapper.incrementComments(req.getArticleId(), 1);
        if (asBlogger && req.getParentId() != null) {
            commentMapper.updateHandle(req.getParentId(), 1);
        }
    }

    public void handle(Long id, Integer handle) {
        commentMapper.updateHandle(id, handle);
    }

    public void visible(Long id, Integer visible) {
        Comment old = commentMapper.selectById(id);
        commentMapper.updateVisible(id, visible);
        if (old == null || old.getArticleId() == null || visible == null || visible.equals(old.getVisible())) {
            return;
        }
        articleMapper.incrementComments(old.getArticleId(), Integer.valueOf(1).equals(visible) ? 1 : -1);
    }

    public void delete(Long id) {
        visible(id, 0);
    }

    private void assertNotBlacklisted(String ip, String nickname, String email) {
        if (blackMapper.countMatch(ip, nickname, email) > 0) {
            throw new BizException(ErrorCode.USER_BLACKLISTED);
        }
    }

    private void fillReplyMeta(Comment comment, Long parentId) {
        if (parentId == null) {
            comment.setRootId(comment.getId());
            return;
        }
        Comment parent = commentMapper.selectById(parentId);
        if (parent == null) {
            throw new BizException(ErrorCode.COMMENT_PARENT_NOT_FOUND);
        }
        comment.setParentId(parent.getId());
        comment.setParentNickname(parent.getNickname());
        comment.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
    }

    private List<Comment> toTree(List<Comment> list) {
        Map<Long, Comment> map = new LinkedHashMap<>();
        for (Comment c : list) {
            c.setChildren(new ArrayList<>());
            map.put(c.getId(), c);
        }
        List<Comment> roots = new ArrayList<>();
        for (Comment c : list) {
            if (isTopLevel(c.getParentId())) {
                roots.add(c);
                continue;
            }
            Comment parent = map.get(c.getParentId());
            if (parent == null) {
                continue;
            }
            Comment root = map.get(c.getRootId());
            if (root == null || !isTopLevel(root.getParentId())) {
                root = parent;
                while (root != null && !isTopLevel(root.getParentId()) && map.containsKey(root.getParentId())) {
                    root = map.get(root.getParentId());
                }
            }
            if (root != null && !root.getId().equals(c.getId())) {
                root.getChildren().add(c);
            }
        }
        return roots;
    }

    static boolean isTopLevel(Long parentId) {
        return parentId == null || parentId == 0L;
    }

    static String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    static String parseBrowser(String ua) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        if (containsToken(ua, "Edg/")) {
            return "Microsoft Edge " + versionAfter(ua, "Edg/");
        }
        if (containsToken(ua, "OPR/") || containsToken(ua, "Opera/")) {
            return "Opera " + versionAfter(ua, containsToken(ua, "OPR/") ? "OPR/" : "Opera/");
        }
        if (containsToken(ua, "Firefox/")) {
            return "Firefox " + versionAfter(ua, "Firefox/");
        }
        if (containsToken(ua, "Chrome/")) {
            return "Chrome " + versionAfter(ua, "Chrome/");
        }
        if (containsToken(ua, "Version/") && containsToken(ua, "Safari/")) {
            return "Safari " + versionAfter(ua, "Version/");
        }
        if (containsToken(ua, "MSIE ") || containsToken(ua, "Trident/")) {
            return "IE";
        }
        return "Other";
    }

    static String parseOs(String ua) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        String text = ua.toLowerCase();
        if (text.contains("windows")) {
            return "Windows";
        }
        if (text.contains("mac os") || text.contains("macintosh")) {
            return "macOS";
        }
        if (text.contains("android")) {
            return "Android";
        }
        if (text.contains("iphone") || text.contains("ipad") || text.contains("ios")) {
            return "iOS";
        }
        if (text.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    private static boolean containsToken(String ua, String token) {
        return ua.contains(token);
    }

    private static String versionAfter(String ua, String token) {
        int start = ua.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();
        int end = start;
        while (end < ua.length()) {
            char ch = ua.charAt(end);
            if (!Character.isDigit(ch) && ch != '.') {
                break;
            }
            end++;
        }
        return ua.substring(start, end);
    }
}

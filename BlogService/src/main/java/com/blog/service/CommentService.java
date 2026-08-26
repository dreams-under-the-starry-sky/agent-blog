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
import com.blog.mapper.CommentMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private IpLocationService ipLocationService;
    @Resource
    private FrontReplyLimitService frontReplyLimitService;
    @Resource
    private MailNotificationService mailNotificationService;
    @Value("${spring.mail.username:}")
    private String mailUsername;
    @Value("${blog.site.title:长路漫漫}")
    private String siteTitle;
    @Value("${blog.site.msg-avatar}")
    private String msgAvatar;

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
        if (!asBlogger) {
            frontReplyLimitService.assertVisitorAllowed(request, req.getNickname(), req.getEmail());
        }
        Comment comment = new Comment();
        comment.setId(IdGenerator.nextId());
        comment.setArticleId(req.getArticleId());
        comment.setContent(req.getContent());
        comment.setBlogger(asBlogger ? 1 : 0);
        Comment parent = fillReplyMeta(comment, req.getParentId());
        comment.setNickname(asBlogger ? siteTitle : trim(req.getNickname(), 20));
        comment.setEmail(trim(asBlogger && !StringUtils.hasText(req.getEmail()) ? mailUsername : req.getEmail(), 30));
        comment.setWebsite(trim(req.getWebsite(), 50));
        comment.setAvatar(asBlogger ? msgAvatar : trim(req.getAvatar(), 255));
        comment.setHandle(asBlogger ? 1 : 0);
        comment.setNotice(req.getNotice() == null ? 0 : req.getNotice());
        comment.setSend(0);
        comment.setVisible(1);
        String ua = request.getHeader("User-Agent");
        comment.setBrowser(trim(parseBrowser(ua), 50));
        comment.setSystemInfo(trim(parseOs(request), 20));
        comment.setIp(trim(ip, 15));
        IpLocationService.Location location = ipLocationService.lookup(ip);
        comment.setProvince(location.province());
        comment.setCity(location.city());
        comment.setDistrict(location.district());
        commentMapper.insert(comment);
        if (frontVisible(comment.getHandle(), comment.getVisible())) {
            articleMapper.incrementComments(req.getArticleId(), 1);
        }
        if (asBlogger && req.getParentId() != null) {
            commentMapper.updateHandle(req.getParentId(), 1);
            if (parent != null && !frontVisible(parent.getHandle(), parent.getVisible())
                    && Integer.valueOf(1).equals(parent.getVisible())) {
                articleMapper.incrementComments(req.getArticleId(), 1);
            }
            if (parent != null) {
                parent.setHandle(1);
                mailNotificationService.notifyCommentReply(parent);
            }
            mailNotificationService.notifyCommentReply(comment);
        }
    }

    public void handle(Long id, Integer handle) {
        commentMapper.updateHandle(id, handle);
    }

    public void review(Long id, boolean approved) {
        Comment old = commentMapper.selectById(id);
        if (old == null) {
            throw new BizException(ErrorCode.COMMENT_PARENT_NOT_FOUND);
        }
        int visible = approved ? 1 : 0;
        boolean wasShown = frontVisible(old.getHandle(), old.getVisible());
        commentMapper.updateReview(id, visible);
        old.setHandle(1);
        old.setVisible(visible);
        boolean nowShown = approved;
        if (old.getArticleId() != null && wasShown != nowShown) {
            articleMapper.incrementComments(old.getArticleId(), nowShown ? 1 : -1);
        }
        if (approved) {
            mailNotificationService.notifyCommentReply(old);
        }
    }

    public void visible(Long id, Integer visible) {
        Comment old = commentMapper.selectById(id);
        commentMapper.updateVisible(id, visible);
        if (old == null || old.getArticleId() == null || visible == null || visible.equals(old.getVisible())
                || !Integer.valueOf(1).equals(old.getHandle())) {
            return;
        }
        articleMapper.incrementComments(old.getArticleId(), Integer.valueOf(1).equals(visible) ? 1 : -1);
    }

    public void delete(Long id) {
        visible(id, 0);
    }

    private Comment fillReplyMeta(Comment comment, Long parentId) {
        if (parentId == null) {
            comment.setRootId(comment.getId());
            return null;
        }
        Comment parent = commentMapper.selectById(parentId);
        if (parent == null) {
            throw new BizException(ErrorCode.COMMENT_PARENT_NOT_FOUND);
        }
        comment.setParentId(parent.getId());
        comment.setParentNickname(parent.getNickname());
        comment.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
        return parent;
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
        //以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
        String ipAddress = request.getHeader("X-Original-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Forwarded-For");
        }
        // 获取nginx等代理的IP
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("x-forwarded-for");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error("unknown host exception, {}", e.getMessage());
                }
                if (inet != null) {
                    ipAddress = inet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress != null && ipAddress.indexOf(",") > 0) { //"***.***.***.***".length() = 15
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        return ipAddress;
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

    static boolean frontVisible(Integer handle, Integer visible) {
        return Integer.valueOf(1).equals(handle) && Integer.valueOf(1).equals(visible);
    }

    static String parseOs(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return parseOs(request.getHeader("User-Agent"), request.getHeader("Sec-CH-UA-Platform-Version"));
    }

    static String parseOs(String ua) {
        return parseOs(ua, null);
    }

    static String parseOs(String ua, String platformVersion) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        String text = ua.toLowerCase();
        if (text.contains("windows")) {
            return parseWindows(ua, platformVersion);
        }
        if (text.contains("android")) {
            String v = versionToken(ua, "Android ");
            return StringUtils.hasText(v) ? "Android " + majorVersion(v) : "Android";
        }
        if (text.contains("iphone") || text.contains("ipad") || text.contains("ios")) {
            String v = versionToken(ua, "iPhone OS ");
            if (!StringUtils.hasText(v)) {
                v = versionToken(ua, "CPU OS ");
            }
            return StringUtils.hasText(v) ? "iOS " + dottedVersion(v) : "iOS";
        }
        if (text.contains("mac os") || text.contains("macintosh")) {
            String v = versionToken(ua, "Mac OS X ");
            return StringUtils.hasText(v) ? "macOS " + dottedVersion(v) : "macOS";
        }
        if (text.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    private static String parseWindows(String ua, String platformVersion) {
        Integer chMajor = platformMajor(platformVersion);
        if (chMajor != null && chMajor >= 13) {
            return "Windows 11";
        }
        String nt = versionToken(ua, "Windows NT ");
        if (nt.startsWith("10.") || "10".equals(nt)) {
            return "Windows 10";
        }
        if (nt.startsWith("6.3")) {
            return "Windows 8.1";
        }
        if (nt.startsWith("6.2")) {
            return "Windows 8";
        }
        if (nt.startsWith("6.1")) {
            return "Windows 7";
        }
        if (nt.startsWith("6.0")) {
            return "Windows Vista";
        }
        if (nt.startsWith("5.")) {
            return "Windows XP";
        }
        return "Windows";
    }

    private static Integer platformMajor(String platformVersion) {
        if (!StringUtils.hasText(platformVersion)) {
            return null;
        }
        String value = platformVersion.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        int dot = value.indexOf('.');
        String major = dot < 0 ? value : value.substring(0, dot);
        try {
            return Integer.parseInt(major);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String versionToken(String ua, String token) {
        int start = ua.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();
        int end = start;
        while (end < ua.length()) {
            char ch = ua.charAt(end);
            if (!Character.isDigit(ch) && ch != '.' && ch != '_') {
                break;
            }
            end++;
        }
        return ua.substring(start, end);
    }

    private static String majorVersion(String version) {
        int cut = version.length();
        int dot = version.indexOf('.');
        int under = version.indexOf('_');
        if (dot >= 0) {
            cut = Math.min(cut, dot);
        }
        if (under >= 0) {
            cut = Math.min(cut, under);
        }
        return version.substring(0, cut);
    }

    private static String dottedVersion(String version) {
        String dotted = version.replace('_', '.');
        String[] parts = dotted.split("\\.");
        if (parts.length >= 2 && StringUtils.hasText(parts[0]) && StringUtils.hasText(parts[1])) {
            return parts[0] + "." + parts[1];
        }
        return parts[0];
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

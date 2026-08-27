package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.MessageSubmitRequest;
import com.blog.entity.Message;
import com.blog.mapper.MessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.blog.common.RequestUserAgent.*;

@Service
public class MessageService {
    private static final Set<Integer> FRONT_PAGE_IDS = Set.of(36, 37);

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private IpLocationService ipLocationService;
    @Resource
    private MailNotificationService mailNotificationService;
    @Resource
    private FrontReplyLimitService frontReplyLimitService;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${blog.site.title}")
    private String siteTitle;
    @Value("${blog.site.msg-avatar}")
    private String msgAvatar;

    public PageResult<Message> treeVisiblePage(Integer pageId, PageQuery query) {
        if (pageId == null) {
            return new PageResult<>(0, List.of());
        }
        List<Message> roots = toTree(messageMapper.selectVisibleByPageId(pageId));
        int from = query.getOffset();
        if (from >= roots.size()) {
            return new PageResult<>(roots.size(), List.of());
        }
        int to = Math.min(from + query.getLimit(), roots.size());
        return new PageResult<>(roots.size(), new ArrayList<>(roots.subList(from, to)));
    }

    public PageResult<Message> page(PageQuery query) {
        return new PageResult<>(messageMapper.countPage(query), messageMapper.selectPage(query));
    }

    public void submit(MessageSubmitRequest req, HttpServletRequest request, boolean asBlogger) {
        String ip = clientIp(request);
        if (!asBlogger) {
            frontReplyLimitService.assertVisitorAllowed(request, req.getNickname(), req.getEmail());
        }
        Message message = new Message();
        message.setId(IdGenerator.nextId());
        message.setContent(req.getContent());
        message.setBlogger(asBlogger ? 1 : 0);
        Message parent = fillReplyMeta(message, req.getParentId());
        if (message.getPageId() == null) {
            Integer pageId = req.getPageId();
            if (pageId == null || (!asBlogger && !FRONT_PAGE_IDS.contains(pageId))) {
                throw new BizException(ErrorCode.MESSAGE_PAGE_INVALID);
            }
            message.setPageId(pageId);
        }
        message.setNickname(asBlogger ? siteTitle : trim(req.getNickname(), 20));
        message.setEmail(trim(asBlogger && !StringUtils.hasText(req.getEmail()) ? mailUsername : req.getEmail(), 30));
        message.setWebsite(trim(req.getWebsite(), 50));
        message.setAvatar(asBlogger ? msgAvatar : trim(req.getAvatar(), 255));
        message.setHandle(asBlogger ? 1 : 0);
        message.setNotice(req.getNotice() == null ? 0 : req.getNotice());
        message.setSend(0);
        message.setVisible(1);
        String ua = request.getHeader("User-Agent");
        message.setBrowser(trim(parseBrowser(ua), 50));
        message.setSystemInfo(trim(parseOs(request), 20));
        message.setIp(trim(ip, 15));
        IpLocationService.Location location = ipLocationService.lookup(ip);
        message.setProvince(location.province());
        message.setCity(location.city());
        message.setDistrict(location.district());
        messageMapper.insert(message);
        if (asBlogger && req.getParentId() != null) {
            messageMapper.updateHandle(req.getParentId(), 1);
            if (parent != null) {
                parent.setHandle(1);
                mailNotificationService.notifyMessageReply(parent);
            }
            mailNotificationService.notifyMessageReply(message);
        }
    }

    public void handle(Long id, Integer handle) {
        messageMapper.updateHandle(id, handle);
    }

    public void review(Long id, boolean approved) {
        Message old = messageMapper.selectById(id);
        if (old == null) {
            throw new BizException(ErrorCode.MESSAGE_PARENT_NOT_FOUND);
        }
        messageMapper.updateReview(id, approved ? 1 : 0);
        old.setHandle(1);
        old.setVisible(approved ? 1 : 0);
        if (approved) {
            mailNotificationService.notifyMessageReply(old);
        }
    }

    public void visible(Long id, Integer visible) {
        messageMapper.updateVisible(id, visible);
    }

    public void delete(Long id) {
        visible(id, 0);
    }

    private Message fillReplyMeta(Message message, Long parentId) {
        if (parentId == null) {
            message.setRootId(message.getId());
            return null;
        }
        Message parent = messageMapper.selectById(parentId);
        if (parent == null) {
            throw new BizException(ErrorCode.MESSAGE_PARENT_NOT_FOUND);
        }
        message.setParentId(parent.getId());
        message.setParentNickname(parent.getNickname());
        message.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
        message.setPageId(parent.getPageId());
        return parent;
    }

    private List<Message> toTree(List<Message> list) {
        Map<Long, Message> map = new LinkedHashMap<>();
        for (Message m : list) {
            m.setChildren(new ArrayList<>());
            map.put(m.getId(), m);
        }
        List<Message> roots = new ArrayList<>();
        for (Message m : list) {
            if (CommentService.isTopLevel(m.getParentId())) {
                roots.add(m);
                continue;
            }
            Message parent = map.get(m.getParentId());
            if (parent == null) {
                continue;
            }
            Message root = map.get(m.getRootId());
            if (root == null || !CommentService.isTopLevel(root.getParentId())) {
                root = parent;
                while (root != null && !CommentService.isTopLevel(root.getParentId()) && map.containsKey(root.getParentId())) {
                    root = map.get(root.getParentId());
                }
            }
            if (root != null && !root.getId().equals(m.getId())) {
                root.getChildren().add(m);
            }
        }
        return roots;
    }
}

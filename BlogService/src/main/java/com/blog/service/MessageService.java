package com.blog.service;

import com.blog.common.IdGenerator;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.MessageSubmitRequest;
import com.blog.entity.Message;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.MessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private IpLocationService ipLocationService;

    public List<Message> treeVisible() {
        return toTree(messageMapper.selectVisible());
    }

    public PageResult<Message> page(PageQuery query) {
        return new PageResult<>(messageMapper.countPage(query), messageMapper.selectPage(query));
    }

    public void submit(MessageSubmitRequest req, HttpServletRequest request, boolean asBlogger) {
        CommentService.validateSubmit(req.getNickname(), req.getEmail(), req.getContent());
        String ip = CommentService.clientIp(request);
        if (blackMapper.countMatch(ip, req.getNickname(), req.getEmail()) > 0) {
            throw new com.blog.common.BizException("当前用户已被限制发言");
        }
        Message message = new Message();
        message.setId(IdGenerator.nextId());
        message.setPageId(req.getPageId() == null ? 1 : req.getPageId());
        message.setContent(req.getContent());
        message.setBlogger(asBlogger ? 1 : 0);
        fillReplyMeta(message, req.getParentId());
        message.setNickname(CommentService.trim(req.getNickname(), 20));
        message.setEmail(CommentService.trim(req.getEmail(), 30));
        message.setWebsite(CommentService.trim(req.getWebsite(), 50));
        message.setHandle(asBlogger ? 1 : 0);
        message.setNotice(req.getNotice() == null ? 0 : req.getNotice());
        message.setSend(0);
        message.setVisible(1);
        String ua = request.getHeader("User-Agent");
        message.setBrowser(CommentService.trim(CommentService.parseBrowser(ua), 50));
        message.setSystemInfo(CommentService.trim(CommentService.parseOs(ua), 20));
        message.setIp(CommentService.trim(ip, 15));
        IpLocationService.Location location = ipLocationService.lookup(ip);
        message.setProvince(location.province());
        message.setCity(location.city());
        message.setDistrict(location.district());
        messageMapper.insert(message);
        if (asBlogger && req.getParentId() != null) {
            messageMapper.updateHandle(req.getParentId(), 1);
        }
    }

    public void handle(Long id, Integer handle) {
        messageMapper.updateHandle(id, handle);
    }

    public void visible(Long id, Integer visible) {
        messageMapper.updateVisible(id, visible);
    }

    public void delete(Long id) {
        visible(id, 0);
    }

    private void fillReplyMeta(Message message, Long parentId) {
        if (parentId == null) {
            message.setRootId(message.getId());
            return;
        }
        Message parent = messageMapper.selectById(parentId);
        if (parent == null) {
            throw new com.blog.common.BizException("回复的留言不存在");
        }
        message.setParentId(parent.getId());
        message.setParentNickname(parent.getNickname());
        message.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
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

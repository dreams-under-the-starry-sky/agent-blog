package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.MessageSubmitRequest;
import com.blog.dto.ReviewRequest;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.entity.Message;
import com.blog.service.FriendService;
import com.blog.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    @Resource
    private MessageService messageService;
    @Resource
    private FriendService friendService;

    @GetMapping("/messages")
    public ResponseEntity<PageResult<Message>> messages(PageQuery query) {
        return ResponseEntity.ok(messageService.page(query));
    }

    @PostMapping("/messages")
    public ResponseEntity<Void> replyMessage(@Valid @RequestBody MessageSubmitRequest req, HttpServletRequest request) {
        messageService.submit(req, request, true);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{id}/handle")
    public ResponseEntity<Void> handleMessage(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        messageService.handle(id, body.get("handle"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{id}/review")
    public ResponseEntity<Void> reviewMessage(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        messageService.review(id, Boolean.TRUE.equals(req.getApproved()));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{id}/visible")
    public ResponseEntity<Void> visibleMessage(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        messageService.visible(id, body.get("visible"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friends")
    public ResponseEntity<List<Friend>> friends() {
        return ResponseEntity.ok(friendService.friends());
    }

    @PostMapping("/friends")
    public ResponseEntity<Long> saveFriend(@RequestBody Friend friend) {
        return ResponseEntity.ok(friendService.saveFriend(friend));
    }

    @DeleteMapping("/friends/{id}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friend-categories")
    public ResponseEntity<List<FriendCategory>> friendCategories() {
        return ResponseEntity.ok(friendService.friendCategories());
    }

    @PostMapping("/friend-categories")
    public ResponseEntity<Long> saveFriendCategory(@Valid @RequestBody FriendCategory category) {
        return ResponseEntity.ok(friendService.saveFriendCategory(category));
    }

    @DeleteMapping("/friend-categories/{id}")
    public ResponseEntity<Void> deleteFriendCategory(@PathVariable Long id) {
        friendService.deleteFriendCategory(id);
        return ResponseEntity.ok().build();
    }
}

package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.MessageSubmitRequest;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.entity.Message;
import com.blog.service.MessageService;
import com.blog.service.MiscService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private MiscService miscService;

    @GetMapping("/messages")
    public Result<PageResult<Message>> messages(PageQuery query) {
        return Result.ok(messageService.page(query));
    }

    @PostMapping("/messages")
    public Result<Void> replyMessage(@Valid @RequestBody MessageSubmitRequest req, HttpServletRequest request) {
        messageService.submit(req, request, true);
        return Result.ok();
    }

    @PutMapping("/messages/{id}/handle")
    public Result<Void> handleMessage(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        messageService.handle(id, body.get("handle"));
        return Result.ok();
    }

    @PutMapping("/messages/{id}/visible")
    public Result<Void> visibleMessage(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        messageService.visible(id, body.get("visible"));
        return Result.ok();
    }

    @DeleteMapping("/messages/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.delete(id);
        return Result.ok();
    }

    @GetMapping("/friends")
    public Result<List<Friend>> friends() {
        return Result.ok(miscService.friends());
    }

    @PostMapping("/friends")
    public Result<Long> saveFriend(@RequestBody Friend friend) {
        return Result.ok(miscService.saveFriend(friend));
    }

    @DeleteMapping("/friends/{id}")
    public Result<Void> deleteFriend(@PathVariable Long id) {
        miscService.deleteFriend(id);
        return Result.ok();
    }

    @GetMapping("/friend-categories")
    public Result<List<FriendCategory>> friendCategories() {
        return Result.ok(miscService.friendCategories());
    }

    @PostMapping("/friend-categories")
    public Result<Long> saveFriendCategory(@RequestBody FriendCategory category) {
        return Result.ok(miscService.saveFriendCategory(category));
    }

    @DeleteMapping("/friend-categories/{id}")
    public Result<Void> deleteFriendCategory(@PathVariable Long id) {
        miscService.deleteFriendCategory(id);
        return Result.ok();
    }
}

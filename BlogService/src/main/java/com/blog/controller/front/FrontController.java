package com.blog.controller.front;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CommentSubmitRequest;
import com.blog.dto.MessageSubmitRequest;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Comment;
import com.blog.entity.Essay;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.entity.Message;
import com.blog.entity.Music;
import com.blog.entity.Record;
import com.blog.entity.RecordCategory;
import com.blog.entity.Tag;
import com.blog.entity.WebUpdateLog;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.EssayService;
import com.blog.service.MessageService;
import com.blog.service.MetaService;
import com.blog.service.MiscService;
import com.blog.service.RecordService;
import com.blog.service.WebUpdateLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/front")
public class FrontController {
    @Resource
    private ArticleService articleService;
    @Resource
    private MetaService metaService;
    @Resource
    private CommentService commentService;
    @Resource
    private MessageService messageService;
    @Resource
    private EssayService essayService;
    @Resource
    private RecordService recordService;
    @Resource
    private MiscService miscService;
    @Resource
    private WebUpdateLogService webUpdateLogService;

    @GetMapping("/articles")
    public Result<PageResult<Article>> articles(PageQuery query) {
        query.setStatus(1);
        return Result.ok(articleService.page(query));
    }

    @GetMapping("/articles/{id}")
    public Result<Article> article(@PathVariable Long id) {
        Article article = articleService.detail(id, true);
        if (!Integer.valueOf(1).equals(article.getStatus())) {
            return Result.fail("文章不存在");
        }
        return Result.ok(article);
    }

    @GetMapping("/articles/archive")
    public Result<List<Article>> archive() {
        return Result.ok(articleService.archive());
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(metaService.categories());
    }

    @GetMapping("/tags")
    public Result<List<Tag>> tags() {
        return Result.ok(metaService.tags());
    }

    @GetMapping("/sidebar")
    public Result<Map<String, Object>> sidebar() {
        Map<String, Object> data = new HashMap<>();
        data.put("hotArticles", articleService.hot(5));
        data.put("categories", metaService.categories());
        data.put("tags", metaService.tags());
        return Result.ok(data);
    }

    @GetMapping("/comments")
    public Result<List<Comment>> comments(Long articleId) {
        return Result.ok(commentService.treeByArticle(articleId));
    }

    @PostMapping("/comments")
    public Result<Void> submitComment(@Valid @RequestBody CommentSubmitRequest req, HttpServletRequest request) {
        commentService.submit(req, request, false);
        return Result.ok();
    }

    @GetMapping("/messages")
    public Result<List<Message>> messages() {
        return Result.ok(messageService.treeVisible());
    }

    @PostMapping("/messages")
    public Result<Void> submitMessage(@Valid @RequestBody MessageSubmitRequest req, HttpServletRequest request) {
        messageService.submit(req, request, false);
        return Result.ok();
    }

    @GetMapping("/essays")
    public Result<PageResult<Essay>> essays(PageQuery query) {
        query.setStatus(1);
        return Result.ok(essayService.page(query));
    }

    @GetMapping("/records")
    public Result<PageResult<Record>> records(PageQuery query) {
        query.setStatus(1);
        return Result.ok(recordService.page(query));
    }

    @GetMapping("/record-categories")
    public Result<List<RecordCategory>> recordCategories() {
        return Result.ok(recordService.categories());
    }

    @GetMapping("/friends")
    public Result<List<Friend>> friends() {
        return Result.ok(miscService.friends());
    }

    @GetMapping("/friend-categories")
    public Result<List<FriendCategory>> friendCategories() {
        return Result.ok(miscService.friendCategories());
    }

    @GetMapping("/music")
    public Result<List<Music>> music() {
        return Result.ok(miscService.musicList());
    }

    @GetMapping("/web-update-logs")
    public Result<List<WebUpdateLog>> webUpdateLogs() {
        return Result.ok(webUpdateLogService.list());
    }
}

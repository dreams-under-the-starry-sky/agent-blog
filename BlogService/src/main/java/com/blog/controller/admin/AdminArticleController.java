package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleSaveRequest;
import com.blog.dto.CommentSubmitRequest;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Comment;
import com.blog.entity.Tag;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.MetaService;
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
public class AdminArticleController {
    @Resource
    private ArticleService articleService;
    @Resource
    private MetaService metaService;
    @Resource
    private CommentService commentService;

    @GetMapping("/articles")
    public Result<PageResult<Article>> articles(PageQuery query) {
        return Result.ok(articleService.page(query));
    }

    @GetMapping("/articles/{id}")
    public Result<Article> article(@PathVariable Long id) {
        return Result.ok(articleService.detail(id, false));
    }

    @PostMapping("/articles")
    public Result<Long> createArticle(@RequestBody ArticleSaveRequest req) {
        req.setId(null);
        return Result.ok(articleService.save(req));
    }

    @PutMapping("/articles/{id}")
    public Result<Long> updateArticle(@PathVariable Long id, @RequestBody ArticleSaveRequest req) {
        req.setId(id);
        return Result.ok(articleService.save(req));
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(metaService.categories());
    }

    @PostMapping("/categories")
    public Result<Long> saveCategory(@RequestBody Category category) {
        return Result.ok(metaService.saveCategory(category));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        metaService.deleteCategory(id);
        return Result.ok();
    }

    @GetMapping("/tags")
    public Result<List<Tag>> tags() {
        return Result.ok(metaService.tags());
    }

    @PostMapping("/tags")
    public Result<Long> saveTag(@RequestBody Tag tag) {
        return Result.ok(metaService.saveTag(tag));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        metaService.deleteTag(id);
        return Result.ok();
    }

    @GetMapping("/comments")
    public Result<PageResult<Comment>> comments(PageQuery query) {
        return Result.ok(commentService.page(query));
    }

    @PostMapping("/comments")
    public Result<Void> replyComment(@Valid @RequestBody CommentSubmitRequest req, HttpServletRequest request) {
        commentService.submit(req, request, true);
        return Result.ok();
    }

    @PutMapping("/comments/{id}/handle")
    public Result<Void> handleComment(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        commentService.handle(id, body.get("handle"));
        return Result.ok();
    }

    @PutMapping("/comments/{id}/visible")
    public Result<Void> visibleComment(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        commentService.visible(id, body.get("visible"));
        return Result.ok();
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return Result.ok();
    }
}

package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.EssaySaveRequest;
import com.blog.dto.ImageSaveItem;
import com.blog.dto.LoginResponse;
import com.blog.dto.RecordSaveRequest;
import com.blog.dto.UpdatePasswordRequest;
import com.blog.dto.UpdateUsernameRequest;
import com.blog.entity.Essay;
import com.blog.entity.Record;
import com.blog.entity.RecordCategory;
import com.blog.service.AuthService;
import com.blog.service.EssayService;
import com.blog.service.RecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminPersonalController {
    @Resource
    private AuthService authService;
    @Resource
    private EssayService essayService;
    @Resource
    private RecordService recordService;

    @PutMapping("/account/username")
    public Result<LoginResponse> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        return Result.ok(authService.updateUsername(currentUsername(), request));
    }

    @PutMapping("/account/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(currentUsername(), request);
        return Result.ok();
    }

    @GetMapping("/essays")
    public Result<PageResult<Essay>> essays(PageQuery query) {
        return Result.ok(essayService.page(query));
    }

    @PostMapping("/essays")
    public Result<Long> saveEssay(@RequestBody EssaySaveRequest req) {
        Essay essay = new Essay();
        essay.setId(req.getId());
        essay.setContent(req.getContent());
        essay.setStatus(req.getStatus());
        essay.setIp(req.getIp());
        essay.setProvince(req.getProvince());
        essay.setCity(req.getCity());
        essay.setDistrict(req.getDistrict());
        return Result.ok(essayService.save(essay, ImageSaveItem.normalize(req.getImages(), req.getImageUrls())));
    }

    @DeleteMapping("/essays/{id}")
    public Result<Void> deleteEssay(@PathVariable Long id) {
        essayService.delete(id);
        return Result.ok();
    }

    @GetMapping("/records")
    public Result<PageResult<Record>> records(PageQuery query) {
        return Result.ok(recordService.page(query));
    }

    @PostMapping("/records")
    public Result<Long> saveRecord(@RequestBody RecordSaveRequest req) {
        Record record = new Record();
        record.setId(req.getId());
        record.setCategoryId(req.getCategoryId());
        record.setHappenTime(req.getHappenTime());
        record.setContent(req.getContent());
        record.setStatus(req.getStatus());
        return Result.ok(recordService.save(record, ImageSaveItem.normalize(req.getImages(), req.getImageUrls())));
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        recordService.delete(id);
        return Result.ok();
    }

    @GetMapping("/record-categories")
    public Result<List<RecordCategory>> recordCategories() {
        return Result.ok(recordService.categories());
    }

    @PostMapping("/record-categories")
    public Result<Long> saveRecordCategory(@RequestBody RecordCategory category) {
        return Result.ok(recordService.saveCategory(category));
    }

    @DeleteMapping("/record-categories/{id}")
    public Result<Void> deleteRecordCategory(@PathVariable Long id) {
        recordService.deleteCategory(id);
        return Result.ok();
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

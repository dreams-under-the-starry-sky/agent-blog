package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<LoginResponse> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(authService.updateUsername(currentUsername(), request));
    }

    @PutMapping("/account/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(currentUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/essays")
    public ResponseEntity<PageResult<Essay>> essays(PageQuery query) {
        return ResponseEntity.ok(essayService.page(query));
    }

    @PostMapping("/essays")
    public ResponseEntity<Long> saveEssay(@RequestBody EssaySaveRequest req) {
        Essay essay = new Essay();
        essay.setId(req.getId());
        essay.setContent(req.getContent());
        essay.setStatus(req.getStatus());
        essay.setIp(req.getIp());
        essay.setProvince(req.getProvince());
        essay.setCity(req.getCity());
        essay.setDistrict(req.getDistrict());
        return ResponseEntity.ok(essayService.save(essay, ImageSaveItem.normalize(req.getImages(), req.getImageUrls())));
    }

    @DeleteMapping("/essays/{id}")
    public ResponseEntity<Void> deleteEssay(@PathVariable Long id) {
        essayService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/records")
    public ResponseEntity<PageResult<Record>> records(PageQuery query) {
        return ResponseEntity.ok(recordService.page(query));
    }

    @PostMapping("/records")
    public ResponseEntity<Long> saveRecord(@RequestBody RecordSaveRequest req) {
        Record record = new Record();
        record.setId(req.getId());
        record.setCategoryId(req.getCategoryId());
        record.setHappenTime(req.getHappenTime());
        record.setContent(req.getContent());
        record.setStatus(req.getStatus());
        return ResponseEntity.ok(recordService.save(record, ImageSaveItem.normalize(req.getImages(), req.getImageUrls())));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/record-categories")
    public ResponseEntity<List<RecordCategory>> recordCategories() {
        return ResponseEntity.ok(recordService.categories());
    }

    @PostMapping("/record-categories")
    public ResponseEntity<Long> saveRecordCategory(@Valid @RequestBody RecordCategory category) {
        return ResponseEntity.ok(recordService.saveCategory(category));
    }

    @DeleteMapping("/record-categories/{id}")
    public ResponseEntity<Void> deleteRecordCategory(@PathVariable Long id) {
        recordService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

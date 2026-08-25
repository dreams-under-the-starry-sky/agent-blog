package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.entity.Black;
import com.blog.entity.BlogLog;
import com.blog.entity.EmailFail;
import com.blog.entity.EmailRecord;
import com.blog.entity.FileDelFail;
import com.blog.entity.Music;
import com.blog.entity.WebUpdateLog;
import com.blog.service.MailNotificationService;
import com.blog.service.MiscService;
import com.blog.service.WebUpdateLogService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminSiteController {
    @Resource
    private MiscService miscService;
    @Resource
    private WebUpdateLogService webUpdateLogService;
    @Resource
    private MailNotificationService mailNotificationService;

    @GetMapping("/logs")
    public ResponseEntity<PageResult<BlogLog>> logs(PageQuery query) {
        return ResponseEntity.ok(miscService.logs(query));
    }

    @GetMapping("/web-update-logs")
    public ResponseEntity<List<WebUpdateLog>> webUpdateLogs() {
        return ResponseEntity.ok(webUpdateLogService.list());
    }

    @PostMapping("/web-update-logs")
    public ResponseEntity<Long> saveWebUpdateLog(@Valid @RequestBody WebUpdateLog log) {
        return ResponseEntity.ok(webUpdateLogService.save(log));
    }

    @DeleteMapping("/web-update-logs/{id}")
    public ResponseEntity<Void> deleteWebUpdateLog(@PathVariable Long id) {
        webUpdateLogService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/music")
    public ResponseEntity<List<Music>> music() {
        return ResponseEntity.ok(miscService.musicList());
    }

    @PostMapping("/music")
    public ResponseEntity<Long> saveMusic(@Valid @RequestBody Music music) {
        return ResponseEntity.ok(miscService.saveMusic(music));
    }

    @DeleteMapping("/music/{id}")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        miscService.deleteMusic(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/blacks")
    public ResponseEntity<List<Black>> blacks() {
        return ResponseEntity.ok(miscService.blacks());
    }

    @PostMapping("/blacks")
    public ResponseEntity<Integer> saveBlack(@RequestBody Black black) {
        return ResponseEntity.ok(miscService.saveBlack(black));
    }

    @DeleteMapping("/blacks/{id}")
    public ResponseEntity<Void> deleteBlack(@PathVariable Integer id) {
        miscService.deleteBlack(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/emails")
    public ResponseEntity<PageResult<EmailRecord>> emails(PageQuery query) {
        return ResponseEntity.ok(miscService.emails(query));
    }

    @GetMapping("/email-fails")
    public ResponseEntity<PageResult<EmailFail>> emailFails(PageQuery query) {
        return ResponseEntity.ok(mailNotificationService.failPage(query));
    }

    @PostMapping("/email-fails/{id}/resend")
    public ResponseEntity<Void> resendEmailFail(@PathVariable Integer id) {
        mailNotificationService.resend(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file-del-fails")
    public ResponseEntity<List<FileDelFail>> fileDelFails() {
        return ResponseEntity.ok(miscService.fileDelFails());
    }

    @DeleteMapping("/file-del-fails/{id}")
    public ResponseEntity<Void> deleteFileDelFail(@PathVariable Integer id) {
        miscService.deleteFileDelFail(id);
        return ResponseEntity.ok().build();
    }
}

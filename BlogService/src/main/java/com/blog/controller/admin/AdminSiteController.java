package com.blog.controller.admin;

import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Black;
import com.blog.entity.BlogLog;
import com.blog.entity.EmailRecord;
import com.blog.entity.FileDelFail;
import com.blog.entity.Music;
import com.blog.entity.WebUpdateLog;
import com.blog.service.MiscService;
import com.blog.service.WebUpdateLogService;
import jakarta.annotation.Resource;
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

    @GetMapping("/logs")
    public Result<PageResult<BlogLog>> logs(PageQuery query) {
        return Result.ok(miscService.logs(query));
    }

    @GetMapping("/web-update-logs")
    public Result<List<WebUpdateLog>> webUpdateLogs() {
        return Result.ok(webUpdateLogService.list());
    }

    @PostMapping("/web-update-logs")
    public Result<Long> saveWebUpdateLog(@RequestBody WebUpdateLog log) {
        return Result.ok(webUpdateLogService.save(log));
    }

    @DeleteMapping("/web-update-logs/{id}")
    public Result<Void> deleteWebUpdateLog(@PathVariable Long id) {
        webUpdateLogService.delete(id);
        return Result.ok();
    }

    @GetMapping("/music")
    public Result<List<Music>> music() {
        return Result.ok(miscService.musicList());
    }

    @PostMapping("/music")
    public Result<Long> saveMusic(@RequestBody Music music) {
        return Result.ok(miscService.saveMusic(music));
    }

    @DeleteMapping("/music/{id}")
    public Result<Void> deleteMusic(@PathVariable Long id) {
        miscService.deleteMusic(id);
        return Result.ok();
    }

    @GetMapping("/blacks")
    public Result<List<Black>> blacks() {
        return Result.ok(miscService.blacks());
    }

    @PostMapping("/blacks")
    public Result<Integer> saveBlack(@RequestBody Black black) {
        return Result.ok(miscService.saveBlack(black));
    }

    @DeleteMapping("/blacks/{id}")
    public Result<Void> deleteBlack(@PathVariable Integer id) {
        miscService.deleteBlack(id);
        return Result.ok();
    }

    @GetMapping("/emails")
    public Result<PageResult<EmailRecord>> emails(PageQuery query) {
        return Result.ok(miscService.emails(query));
    }

    @GetMapping("/file-del-fails")
    public Result<List<FileDelFail>> fileDelFails() {
        return Result.ok(miscService.fileDelFails());
    }

    @DeleteMapping("/file-del-fails/{id}")
    public Result<Void> deleteFileDelFail(@PathVariable Integer id) {
        miscService.deleteFileDelFail(id);
        return Result.ok();
    }
}

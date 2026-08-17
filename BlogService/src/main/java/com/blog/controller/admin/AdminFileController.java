package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.UploadResult;
import com.blog.service.MiscService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminFileController {
    @Resource
    private MiscService miscService;

    @PostMapping("/upload")
    public Result<UploadResult> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.ok(miscService.upload(file));
    }

    @PostMapping("/upload/delete")
    public Result<Void> deleteUpload(@RequestBody Map<String, String> body) {
        if (body == null) {
            return Result.ok();
        }
        miscService.tryDeleteFiles(body.get("url"), body.get("thumbnailUrl"));
        return Result.ok();
    }
}

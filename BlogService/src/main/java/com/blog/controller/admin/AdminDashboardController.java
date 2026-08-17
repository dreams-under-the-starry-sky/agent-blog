package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.DashboardVO;
import com.blog.service.MiscService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {
    @Resource
    private MiscService miscService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(miscService.dashboard());
    }
}

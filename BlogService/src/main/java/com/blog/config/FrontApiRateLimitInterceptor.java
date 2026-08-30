package com.blog.config;

import com.blog.service.FrontApiRateLimitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FrontApiRateLimitInterceptor implements HandlerInterceptor {
    @Resource
    private FrontApiRateLimitService frontApiRateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        frontApiRateLimitService.assertAllowed(request.getMethod(), request.getServletPath());
        return true;
    }
}

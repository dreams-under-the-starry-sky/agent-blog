package com.blog.service;

import com.blog.common.BizException;
import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.UpdatePasswordRequest;
import com.blog.dto.UpdateUsernameRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.security.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        if (Boolean.TRUE.equals(user.getDisable())) {
            throw new BizException("账号已被禁用");
        }
        if (!matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (isLegacyPassword(user.getPassword())) {
            userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getPassword()));
        }
        return new LoginResponse(jwtUtil.generate(user.getUsername()), user.getUsername(), user.getRole());
    }

    public LoginResponse updateUsername(String currentUsername, UpdateUsernameRequest request) {
        User user = requireCurrent(currentUsername);
        if (!matches(request.getPassword(), user.getPassword())) {
            throw new BizException("当前密码错误");
        }
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (!StringUtils.hasText(username)) {
            throw new BizException("用户名不能为空");
        }
        if (username.length() > 15) {
            throw new BizException("用户名不能超过15个字符");
        }
        if (username.equals(user.getUsername())) {
            throw new BizException("用户名未修改");
        }
        if (userMapper.countByUsername(username, user.getId()) > 0) {
            throw new BizException("用户名已存在");
        }
        userMapper.updateUsername(user.getId(), username);
        return new LoginResponse(jwtUtil.generate(username), username, user.getRole());
    }

    public void updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = requireCurrent(currentUsername);
        if (!matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException("当前密码错误");
        }
        String next = request.getNewPassword() == null ? "" : request.getNewPassword().trim();
        if (next.length() < 6) {
            throw new BizException("新密码至少6位");
        }
        if (next.length() > 72) {
            throw new BizException("新密码过长");
        }
        if (matches(next, user.getPassword())) {
            throw new BizException("新密码不能与当前密码相同");
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(next));
    }

    private User requireCurrent(String currentUsername) {
        User user = userMapper.findByUsername(currentUsername);
        if (user == null) {
            throw new BizException("账号不存在");
        }
        return user;
    }

    private boolean matches(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try {
                return passwordEncoder.matches(raw, stored);
            } catch (Exception ex) {
                return false;
            }
        }
        return raw.equals(stored);
    }

    private boolean isLegacyPassword(String stored) {
        return stored != null && !stored.startsWith("$2");
    }
}

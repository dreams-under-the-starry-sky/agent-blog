package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.UpdatePasswordRequest;
import com.blog.dto.UpdateUsernameRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.security.JwtUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BizException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (Boolean.TRUE.equals(user.getDisable())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (isLegacyPassword(user.getPassword())) {
            userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getPassword()));
        }
        return new LoginResponse(jwtUtil.generate(user.getUsername()), user.getUsername(), user.getRole());
    }

    public LoginResponse updateUsername(String currentUsername, UpdateUsernameRequest request) {
        User user = requireCurrent(currentUsername);
        if (!matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.CURRENT_PASSWORD_WRONG);
        }
        String username = request.getUsername().trim();
        if (username.equals(user.getUsername())) {
            throw new BizException(ErrorCode.USERNAME_UNCHANGED);
        }
        if (userMapper.countByUsername(username, user.getId()) > 0) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        userMapper.updateUsername(user.getId(), username);
        return new LoginResponse(jwtUtil.generate(username), username, user.getRole());
    }

    public void updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = requireCurrent(currentUsername);
        if (!matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.CURRENT_PASSWORD_WRONG);
        }
        String next = request.getNewPassword().trim();
        if (matches(next, user.getPassword())) {
            throw new BizException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(next));
    }

    private User requireCurrent(String currentUsername) {
        User user = userMapper.findByUsername(currentUsername);
        if (user == null) {
            throw new BizException(ErrorCode.ACCOUNT_NOT_FOUND);
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
                log.warn("校验密码失败");
                return false;
            }
        }
        return raw.equals(stored);
    }

    private boolean isLegacyPassword(String stored) {
        return stored != null && !stored.startsWith("$2");
    }
}

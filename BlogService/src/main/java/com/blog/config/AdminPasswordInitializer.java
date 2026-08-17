package com.blog.config;

import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class AdminPasswordInitializer {
    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    @Bean
    public ApplicationRunner resetAdminPasswordIfNeeded(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            @Value("${blog.admin.reset-password:}") String resetPassword) {
        return args -> {
            if (!StringUtils.hasText(resetPassword)) {
                return;
            }
            User admin = userMapper.findByUsername("admin");
            if (admin == null) {
                log.warn("admin user not found, skip password reset");
                return;
            }
            userMapper.updatePassword(admin.getId(), passwordEncoder.encode(resetPassword));
            log.info("admin password has been reset by blog.admin.reset-password");
        };
    }
}

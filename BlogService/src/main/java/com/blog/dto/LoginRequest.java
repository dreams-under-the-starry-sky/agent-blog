package com.blog.dto;

import com.blog.validation.RequiredText;
import lombok.Data;

@Data
public class LoginRequest {
    @RequiredText(message = "用户名不能为空")
    private String username;
    @RequiredText(message = "请填写密码")
    private String password;
}

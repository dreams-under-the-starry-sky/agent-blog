package com.blog.dto;

import com.blog.validation.RequiredText;
import lombok.Data;

@Data
public class UpdateUsernameRequest {
    @RequiredText(message = "用户名不能为空", max = 15, tooLongMessage = "用户名不能超过15个字符")
    private String username;
    @RequiredText(message = "请填写密码")
    private String password;
}

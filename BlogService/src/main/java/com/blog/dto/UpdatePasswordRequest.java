package com.blog.dto;

import com.blog.validation.RequiredText;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @RequiredText(message = "请填写密码")
    private String oldPassword;
    @RequiredText(message = "请填写密码", min = 6, max = 72, tooShortMessage = "新密码至少6位", tooLongMessage = "新密码过长")
    private String newPassword;
}

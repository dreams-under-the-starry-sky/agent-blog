package com.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSubmitRequest {
    private Integer pageId;
    private Long parentId;
    @NotBlank(message = "请填写内容")
    @Size(max = 255, message = "消息内容不能超过255个字符")
    private String content;
    @NotBlank(message = "请填写昵称")
    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickname;
    @Email(message = "请填写正确的邮箱格式")
    @Size(max = 30, message = "邮箱不能超过30个字符")
    private String email;
    private String website;
    private Integer notice;
}

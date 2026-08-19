package com.blog.entity;

import com.blog.validation.RequiredText;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebUpdateLog {
    private Long id;
    @RequiredText(message = "标题不能为空")
    private String title;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

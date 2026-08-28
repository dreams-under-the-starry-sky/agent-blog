package com.blog.entity;

import com.blog.validation.RequiredText;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WebUpdateLog {
    private Long id;
    @RequiredText(message = "标题不能为空")
    private String title;
    private String description;
    @NotNull(message = "请选择事件时间")
    private LocalDate eventDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebUpdateLog {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

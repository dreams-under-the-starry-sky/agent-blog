package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogLog {
    private Integer id;
    private String event;
    private String status;
    private Integer exeTime;
    private String detail;
    private LocalDateTime createTime;
}

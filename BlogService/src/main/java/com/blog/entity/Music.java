package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Music {
    private Long id;
    private String name;
    private String author;
    private String url;
    private String cover;
    private String lrc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

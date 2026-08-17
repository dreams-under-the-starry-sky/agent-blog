package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friend {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String logo;
    private String href;
    private String cover;
    private String thumbnail;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

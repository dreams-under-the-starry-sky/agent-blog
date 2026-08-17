package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendCategory {
    private Long id;
    private String name;
    private Integer sort;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

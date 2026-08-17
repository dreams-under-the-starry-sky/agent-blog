package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private Integer role;
    private Boolean disable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

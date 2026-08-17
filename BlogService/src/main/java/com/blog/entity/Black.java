package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Black {
    private Integer id;
    private String ip;
    private String position;
    private String nickname;
    private String email;
    private LocalDateTime createTime;
}

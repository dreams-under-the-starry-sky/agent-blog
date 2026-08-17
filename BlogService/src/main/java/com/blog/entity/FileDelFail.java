package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDelFail {
    private Integer id;
    private String fileKey;
    private String extra;
    private LocalDateTime createTime;
}

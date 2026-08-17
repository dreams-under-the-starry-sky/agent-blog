package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Record {
    private Long id;
    private Long categoryId;
    private Integer happenTime;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String categoryName;
    private List<RecordImg> images;
}

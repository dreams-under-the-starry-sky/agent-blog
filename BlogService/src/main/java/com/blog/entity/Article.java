package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Article {
    private Long id;
    private Long categoryId;
    private String title;
    private String description;
    private String cover;
    private String thumbnail;
    private Integer comment;
    private Integer comments;
    private Integer status;
    private Integer recommend;
    private Integer pv;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer yearTime;
    private Integer monthTime;

    private String content;
    private String categoryName;
    private List<Tag> tags;
    private List<String> tagNames;
    private List<Long> tagIds;
    private List<ArticleImg> images;
}

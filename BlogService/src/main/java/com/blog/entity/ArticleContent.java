package com.blog.entity;

import lombok.Data;

@Data
public class ArticleContent {
    private Integer id;
    private Long articleId;
    private String content;
}

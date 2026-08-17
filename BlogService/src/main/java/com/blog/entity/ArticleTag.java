package com.blog.entity;

import lombok.Data;

@Data
public class ArticleTag {
    private Integer id;
    private Long articleId;
    private Long tagId;
}

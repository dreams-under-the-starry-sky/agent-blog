package com.blog.entity;

import lombok.Data;

@Data
public class ArticleImg {
    private Integer id;
    private Long articleId;
    private String imgUrl;
    private String thumbnailUrl;
}

package com.blog.entity;

import lombok.Data;

@Data
public class EssayImg {
    private Integer id;
    private Long essayId;
    private String imgUrl;
    private String thumbnailUrl;
}

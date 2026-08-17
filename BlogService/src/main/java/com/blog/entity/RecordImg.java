package com.blog.entity;

import lombok.Data;

@Data
public class RecordImg {
    private Integer id;
    private Long recordId;
    private String imgUrl;
    private String thumbnailUrl;
}

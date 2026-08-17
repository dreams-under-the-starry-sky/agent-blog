package com.blog.common;

import lombok.Data;

@Data
public class PageQuery {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Long categoryId;
    private Long tagId;
    private Integer status;
    private Integer recommend;
    private Integer handle;
    private Integer visible;
    private Integer notice;
    private Integer send;

    public int getOffset() {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        return (p - 1) * s;
    }

    public int getLimit() {
        return size == null || size < 1 ? 10 : size;
    }
}

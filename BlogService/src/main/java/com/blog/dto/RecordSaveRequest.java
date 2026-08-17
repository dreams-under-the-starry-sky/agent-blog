package com.blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecordSaveRequest {
    private Long id;
    private Long categoryId;
    private Integer happenTime;
    private String content;
    private Integer status;
    private List<String> imageUrls;
    private List<ImageSaveItem> images;
}

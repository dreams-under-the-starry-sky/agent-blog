package com.blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class EssaySaveRequest {
    private Long id;
    private String content;
    private Integer status;
    private String ip;
    private String province;
    private String city;
    private String district;
    private List<String> imageUrls;
    private List<ImageSaveItem> images;
}

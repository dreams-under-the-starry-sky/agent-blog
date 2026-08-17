package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Essay {
    private Long id;
    private String content;
    private Integer status;
    private String ip;
    private String province;
    private String city;
    private String district;
    private String browser;
    private String systemInfo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<EssayImg> images;
}

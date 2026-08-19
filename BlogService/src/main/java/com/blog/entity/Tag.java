package com.blog.entity;

import com.blog.validation.RequiredText;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Tag {
    private Long id;
    @RequiredText(message = "标签名不能为空")
    private String name;
    private Integer articleCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

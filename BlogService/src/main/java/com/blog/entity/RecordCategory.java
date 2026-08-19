package com.blog.entity;

import com.blog.validation.RequiredText;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecordCategory {
    private Long id;
    @RequiredText(message = "分类名不能为空")
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

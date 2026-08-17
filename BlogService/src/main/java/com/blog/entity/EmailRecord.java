package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailRecord {
    private Integer id;
    private Long articleId;
    private Integer pageId;
    private Long messageId;
    private String sendName;
    private String sendEmail;
    private String receiveName;
    private String receiveEmail;
    private String content;
    private LocalDateTime createTime;
}

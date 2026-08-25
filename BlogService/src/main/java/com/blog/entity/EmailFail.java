package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailFail {
    private Integer id;
    private String kind;
    private Long replyId;
    private Long parentId;
    private Long articleId;
    private Integer pageId;
    private String sendName;
    private String sendEmail;
    private String receiveName;
    private String receiveEmail;
    private String content;
    private String originalContent;
    private String link;
    private String extra;
    private LocalDateTime createTime;
}

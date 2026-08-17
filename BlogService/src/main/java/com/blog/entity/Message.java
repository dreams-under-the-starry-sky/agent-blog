package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Message {
    private Long id;
    private Integer pageId;
    private Long parentId;
    private Long rootId;
    private String content;
    private Integer blogger;
    private String parentNickname;
    private String nickname;
    private String email;
    private String avatar;
    private String website;
    private Integer handle;
    private Integer notice;
    private Integer send;
    private Integer visible;
    private String browser;
    private String systemInfo;
    private String ip;
    private String province;
    private String city;
    private String district;
    private LocalDateTime createTime;
    private List<Message> children = new ArrayList<>();
}

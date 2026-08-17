package com.blog.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long articleCount;
    private long publishedCount;
    private long commentCount;
    private long messageCount;
    private long essayCount;
    private long pvTotal;
    private List<Map<String, Object>> categoryStats;
    private List<com.blog.entity.Article> recentArticles;
    private List<com.blog.entity.Comment> recentComments;
}

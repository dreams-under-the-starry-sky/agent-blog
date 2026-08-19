package com.blog.dto;

import com.blog.entity.Article;
import com.blog.entity.Black;
import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {
    private long articleCount;
    private long friendCount;
    private long messageCount;
    private long commentCount;
    private long blackCount;
    private long errorLogCount;
    private List<Article> hotArticles;
    private List<Black> recentBlacks;
}

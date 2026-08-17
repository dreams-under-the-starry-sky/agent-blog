package com.blog.mapper;

import com.blog.entity.ArticleContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleContentMapper {
    ArticleContent selectByArticleId(@Param("articleId") Long articleId);

    int insert(ArticleContent content);

    int updateByArticleId(ArticleContent content);

    int deleteByArticleId(@Param("articleId") Long articleId);
}

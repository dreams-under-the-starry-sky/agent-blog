package com.blog.mapper;

import com.blog.entity.ArticleImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleImgMapper {
    List<ArticleImg> selectByArticleId(@Param("articleId") Long articleId);

    int insert(ArticleImg img);

    int deleteByArticleId(@Param("articleId") Long articleId);
}

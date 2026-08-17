package com.blog.mapper;

import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper {
    List<Tag> selectTagsByArticleId(@Param("articleId") Long articleId);

    int insert(ArticleTag articleTag);

    int deleteByArticleId(@Param("articleId") Long articleId);

    int countByTagId(@Param("tagId") Long tagId);
}

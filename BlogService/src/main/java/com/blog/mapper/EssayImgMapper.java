package com.blog.mapper;

import com.blog.entity.EssayImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EssayImgMapper {
    List<EssayImg> selectByEssayId(@Param("essayId") Long essayId);

    int insert(EssayImg img);

    int deleteByEssayId(@Param("essayId") Long essayId);
}

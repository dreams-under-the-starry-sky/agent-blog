package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.Essay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EssayMapper {
    List<Essay> selectPage(PageQuery query);

    long countPage(PageQuery query);

    Essay selectById(@Param("id") Long id);

    int insert(Essay essay);

    int update(Essay essay);

    int deleteById(@Param("id") Long id);

    long countAll();
}

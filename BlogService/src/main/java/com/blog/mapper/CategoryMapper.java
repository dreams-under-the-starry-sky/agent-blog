package com.blog.mapper;

import com.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> selectAll();

    Category selectById(@Param("id") Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);

    int updateCount(@Param("id") Long id, @Param("count") Integer count);

    int syncPublishedCounts();
}

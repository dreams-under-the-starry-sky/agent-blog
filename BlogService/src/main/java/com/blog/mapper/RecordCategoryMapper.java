package com.blog.mapper;

import com.blog.entity.RecordCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordCategoryMapper {
    List<RecordCategory> selectAll();

    RecordCategory selectById(@Param("id") Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(RecordCategory category);

    int update(RecordCategory category);

    int deleteById(@Param("id") Long id);
}

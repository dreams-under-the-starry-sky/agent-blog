package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordMapper {
    List<Record> selectPage(PageQuery query);

    long countPage(PageQuery query);

    Record selectById(@Param("id") Long id);

    int insert(Record record);

    int update(Record record);

    int deleteById(@Param("id") Long id);

    int countByCategoryId(@Param("categoryId") Long categoryId);
}

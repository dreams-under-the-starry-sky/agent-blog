package com.blog.mapper;

import com.blog.entity.WebUpdateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WebUpdateLogMapper {
    List<WebUpdateLog> selectAll();

    int insert(WebUpdateLog log);

    int update(WebUpdateLog log);

    int deleteById(@Param("id") Long id);
}

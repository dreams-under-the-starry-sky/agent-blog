package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.BlogLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BlogLogMapper {
    List<BlogLog> selectPage(PageQuery query);

    long countPage(PageQuery query);

    long countFailed();

    int insert(BlogLog log);
}

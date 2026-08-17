package com.blog.mapper;

import com.blog.entity.FileDelFail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileDelFailMapper {
    List<FileDelFail> selectAll();

    int insert(FileDelFail fail);

    int deleteById(@Param("id") Integer id);
}

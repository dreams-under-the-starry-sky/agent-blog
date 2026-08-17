package com.blog.mapper;

import com.blog.entity.RecordImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordImgMapper {
    List<RecordImg> selectByRecordId(@Param("recordId") Long recordId);

    int insert(RecordImg img);

    int deleteByRecordId(@Param("recordId") Long recordId);
}

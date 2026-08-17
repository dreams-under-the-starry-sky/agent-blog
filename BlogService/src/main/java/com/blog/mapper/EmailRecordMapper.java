package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmailRecordMapper {
    List<EmailRecord> selectPage(PageQuery query);

    long countPage(PageQuery query);
}

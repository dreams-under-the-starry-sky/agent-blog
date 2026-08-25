package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.EmailFail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmailFailMapper {
    List<EmailFail> selectPage(PageQuery query);

    long countPage(PageQuery query);

    EmailFail selectById(@Param("id") Integer id);

    int insert(EmailFail fail);

    int updateExtra(@Param("id") Integer id, @Param("extra") String extra);

    int deleteById(@Param("id") Integer id);
}

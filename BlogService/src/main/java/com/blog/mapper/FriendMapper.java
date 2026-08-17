package com.blog.mapper;

import com.blog.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectAll();

    Friend selectById(@Param("id") Long id);

    int insert(Friend friend);

    int update(Friend friend);

    int deleteById(@Param("id") Long id);

    int countByCategoryId(@Param("categoryId") Long categoryId);
}

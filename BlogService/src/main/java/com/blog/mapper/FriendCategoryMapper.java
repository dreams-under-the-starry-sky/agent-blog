package com.blog.mapper;

import com.blog.entity.FriendCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendCategoryMapper {
    List<FriendCategory> selectAll();

    FriendCategory selectById(@Param("id") Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(FriendCategory category);

    int update(FriendCategory category);

    int deleteById(@Param("id") Long id);
}

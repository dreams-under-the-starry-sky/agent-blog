package com.blog.mapper;

import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    int countByUsername(@Param("username") String username, @Param("excludeId") Integer excludeId);

    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    int updateUsername(@Param("id") Integer id, @Param("username") String username);
}

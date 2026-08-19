package com.blog.mapper;

import com.blog.entity.Black;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlackMapper {
    List<Black> selectAll();

    Black selectById(@Param("id") Integer id);

    int insert(Black black);

    int deleteById(@Param("id") Integer id);

    int countMatch(@Param("ip") String ip, @Param("nickname") String nickname, @Param("email") String email);

    long countAll();

    List<Black> selectSince(@Param("start") java.time.LocalDateTime start);
}

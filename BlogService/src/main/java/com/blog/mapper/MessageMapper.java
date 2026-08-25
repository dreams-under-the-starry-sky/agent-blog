package com.blog.mapper;

import com.blog.common.PageQuery;
import com.blog.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<Message> selectVisible();

    List<Message> selectVisibleByPageId(@Param("pageId") Integer pageId);

    List<Message> selectPage(PageQuery query);

    long countPage(PageQuery query);

    Message selectById(@Param("id") Long id);

    int insert(Message message);

    int updateHandle(@Param("id") Long id, @Param("handle") Integer handle);

    int updateVisible(@Param("id") Long id, @Param("visible") Integer visible);

    int updateReview(@Param("id") Long id, @Param("visible") Integer visible);

    int updateSend(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    long countAll();

    int countByIpSince(@Param("ip") String ip, @Param("start") java.time.LocalDateTime start);

    int countByEmailSince(@Param("email") String email, @Param("start") java.time.LocalDateTime start);
}

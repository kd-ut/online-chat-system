package com.chat.chat_backend.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.friend.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 好友数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 查询两个用户之间的好友关系
     * @param userId 用户ID
     * @param friendId 好友用户ID
     * @return 好友关系实体，非好友时返回null
     */
    Friend findFriendRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 查询指定用户的所有好友关系
     * @param userId 用户ID
     * @return 好友关系实体列表
     */
    List<Friend> findAllByUserId(@Param("userId") Long userId);
}
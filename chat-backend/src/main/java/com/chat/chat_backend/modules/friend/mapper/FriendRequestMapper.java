package com.chat.chat_backend.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.friend.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 好友请求数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 查询目标用户的所有待处理好友请求
     * @param toUserId 接收方用户ID
     * @return 待处理的好友请求列表
     */
    List<FriendRequest> findPendingRequests(@Param("toUserId") Long toUserId);

    /**
     * 查询两个用户之间特定的待处理好友请求
     * @param fromUserId 发送方用户ID
     * @param toUserId 接收方用户ID
     * @return 待处理的好友请求，不存在时返回null
     */
    FriendRequest findPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 清理已超时的过期好友请求记录
     */
    void expireOldRequests();
}
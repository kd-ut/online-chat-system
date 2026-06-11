package com.chat.chat_backend.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.friend.entity.FriendGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友分组数据访问接口
 * @author chat-backend
 * @since 2026-06-11
 */
@Mapper
public interface FriendGroupMapper extends BaseMapper<FriendGroup> {

    /**
     * 查询用户的所有分组（按排序序号升序）
     * @param userId 用户ID
     * @return 分组列表
     */
    List<FriendGroup> findByUserId(@Param("userId") Long userId);

    /**
     * 查询分组中还有好友的分组ID列表
     * @param userId 用户ID
     * @return 分组ID列表
     */
    List<Long> findNonEmptyGroupIds(@Param("userId") Long userId);
}
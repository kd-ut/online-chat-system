package com.chat.chat_backend.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 群成员数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /**
     * 查询指定群组的所有成员
     * @param groupId 群组ID
     * @return 群成员实体列表
     */
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询用户加入的所有群组关系
     * @param userId 用户ID
     * @return 群成员实体列表
     */
    List<GroupMember> findByUserId(@Param("userId") Long userId);

    /**
     * 增加群内除发送者外所有成员的未读消息计数
     * @param groupId 群组ID
     * @param fromUserId 发送者用户ID（不增加该用户的未读数）
     */
    void incrementUnreadCount(@Param("groupId") Long groupId, @Param("fromUserId") Long fromUserId);

    /**
     * 清除用户在指定群组中的未读消息计数
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    void clearUnreadCount(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
package com.chat.chat_backend.modules.friend.service;

import com.chat.chat_backend.modules.friend.entity.FriendGroup;
import java.util.List;

/**
 * 好友分组服务接口
 * @author chat-backend
 * @since 2026-06-11
 */
public interface FriendGroupService {

    /** 获取用户的所有分组 @param userId 用户ID @return 分组列表 */
    List<FriendGroup> listGroups(Long userId);

    /** 创建分组 @param userId 用户ID @param groupName 分组名称 @return 新分组 */
    FriendGroup createGroup(Long userId, String groupName);

    /** 重命名分组 @param userId 用户ID @param groupId 分组ID @param newName 新名称 */
    void renameGroup(Long userId, Long groupId, String newName);

    /** 删除分组（将分组下的好友移至默认分组） @param userId 用户ID @param groupId 分组ID */
    void deleteGroup(Long userId, Long groupId);
}
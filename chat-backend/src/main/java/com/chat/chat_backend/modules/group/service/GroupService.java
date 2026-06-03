package com.chat.chat_backend.modules.group.service;

import com.chat.chat_backend.modules.group.dto.request.CreateGroupRequest;
import com.chat.chat_backend.modules.group.dto.request.InviteMemberRequest;
import com.chat.chat_backend.modules.group.dto.response.GroupVO;
import java.util.List;

/** 群聊服务接口，提供创建群聊、查询群列表、群成员管理、禁言、公告等功能 @author chat-backend @since 2026-05-12 */
public interface GroupService {

    /** 创建群聊 @param userId 创建者用户ID @param request 创建群聊请求（群名、头像、公告、初始成员） @return 群聊信息 */
    GroupVO createGroup(Long userId, CreateGroupRequest request);

    /** 获取用户加入的群聊列表 @param userId 用户ID @return 群聊列表 */
    List<GroupVO> getUserGroups(Long userId);

    /** 获取群聊详情 @param userId 用户ID @param groupId 群聊ID @return 群聊详情 */
    GroupVO getGroupDetail(Long userId, Long groupId);

    /** 邀请成员加入群聊 @param userId 邀请者用户ID @param request 邀请请求（群聊ID、被邀请用户ID） */
    void inviteMember(Long userId, InviteMemberRequest request);

    /** 退出群聊 @param userId 用户ID @param groupId 群聊ID */
    void quitGroup(Long userId, Long groupId);

    /** 解散群聊（仅群主可操作） @param userId 用户ID @param groupId 群聊ID */
    void disbandGroup(Long userId, Long groupId);

    /** 清除群聊未读消息数 @param userId 用户ID @param groupId 群聊ID */
    void clearUnreadCount(Long userId, Long groupId);

    /** 更新群公告（群主或管理员可操作） @param userId 用户ID @param groupId 群聊ID @param notice 公告内容 */
    void updateNotice(Long userId, Long groupId, String notice);

    /** 设置管理员（仅群主可操作） @param userId 用户ID @param groupId 群聊ID @param memberId 成员ID */
    void setAdmin(Long userId, Long groupId, Long memberId);

    /** 取消管理员（仅群主可操作） @param userId 用户ID @param groupId 群聊ID @param memberId 管理员ID */
    void removeAdmin(Long userId, Long groupId, Long memberId);

    /** 移除成员（群主或管理员可操作） @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被移除成员ID */
    void removeMember(Long userId, Long groupId, Long memberId);

    /** 禁言成员 @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被禁言成员ID @param minutes 禁言时长（分钟） */
    void muteMember(Long userId, Long groupId, Long memberId, Integer minutes);

    /** 取消禁言 @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被禁言成员ID */
    void unmuteMember(Long userId, Long groupId, Long memberId);

    /** 批量禁言 @param userId 操作者用户ID @param groupId 群聊ID @param memberIds 被禁言成员ID列表 @param minutes 禁言时长（分钟） */
    void batchMute(Long userId, Long groupId, List<Long> memberIds, Integer minutes);
}
package com.chat.chat_backend.modules.friend.service;

import com.chat.chat_backend.modules.friend.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendGroupVO;
import com.chat.chat_backend.modules.friend.dto.response.FriendVO;
import java.util.List;

/** 好友关系服务接口，提供搜索用户、好友列表、删除好友、移动分组、修改备注等功能 @author chat-backend @since 2026-05-12 */
public interface FriendRelationService {

    /** 搜索用户（按用户名或昵称模糊匹配，排除当前用户） @param currentUserId 当前用户ID @param keyword 搜索关键词 @return 匹配的用户列表 */
    List<FriendVO> searchUsers(Long currentUserId, String keyword);

    /** 获取好友列表（按分组返回，含在线状态和未读消息数） @param currentUserId 当前用户ID @return 分组好友列表 */
    List<FriendGroupVO> getFriendList(Long currentUserId);

    /** 删除好友（双向删除好友关系） @param currentUserId 当前用户ID @param friendId 好友ID */
    void deleteFriend(Long currentUserId, Long friendId);

    /** 移动好友分组 @param currentUserId 当前用户ID @param friendId 好友ID @param request 新分组名称 */
    void moveFriendGroup(Long currentUserId, Long friendId, MoveFriendGroupRequest request);

    /** 修改好友备注 @param currentUserId 当前用户ID @param friendId 好友ID @param remark 新备注 */
    void updateFriendRemark(Long currentUserId, Long friendId, String remark);
}
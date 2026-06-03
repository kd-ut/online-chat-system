package com.chat.chat_backend.modules.friend.service;

import com.chat.chat_backend.modules.friend.dto.request.HandleFriendRequest;
import com.chat.chat_backend.modules.friend.dto.request.SendFriendRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendRequestVO;
import java.util.List;

/** 好友请求服务接口，提供发送请求、查询待处理请求、处理请求（接受/拒绝）等功能 @author chat-backend @since 2026-05-12 */
public interface FriendRequestService {

    /** 发送好友请求 @param currentUserId 当前用户ID @param request 好友请求（目标用户ID、附言） */
    void sendFriendRequest(Long currentUserId, SendFriendRequest request);

    /** 获取待处理的好友请求列表 @param currentUserId 当前用户ID @return 待处理的好友请求列表 */
    List<FriendRequestVO> getFriendRequests(Long currentUserId);

    /** 处理好友请求（接受/拒绝） @param currentUserId 当前用户ID @param requestId 请求ID @param request 处理结果（1接受/2拒绝） */
    void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request);
}
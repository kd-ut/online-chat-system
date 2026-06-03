package com.chat.chat_backend.modules.friend.service.impl;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.modules.friend.mapper.FriendMapper;
import com.chat.chat_backend.modules.friend.mapper.FriendRequestMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.friend.dto.request.HandleFriendRequest;
import com.chat.chat_backend.modules.friend.dto.request.SendFriendRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendRequestVO;
import com.chat.chat_backend.modules.friend.entity.Friend;
import com.chat.chat_backend.modules.friend.entity.FriendRequest;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.friend.service.FriendRequestService;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 好友请求服务实现，处理发送请求、查询待处理请求、接受/拒绝请求等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    /** 好友数据访问层 */
    private final FriendMapper friendMapper;
    /** 好友请求数据访问层 */
    private final FriendRequestMapper friendRequestMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** WebSocket会话管理器（用于实时推送好友请求通知） */
    private final WebSocketSessionManager sessionManager;

    /** 发送好友请求（校验目标用户存在性、是否已是好友、是否有待处理的请求） @param currentUserId 当前用户ID @param request 好友请求 */
    @Override
    @Transactional
    public void sendFriendRequest(Long currentUserId, SendFriendRequest request) {
        if (currentUserId.equals(request.getToUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能添加自己为好友");
        }

        if (userMapper.selectById(request.getToUserId()) == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (friendMapper.findFriendRelation(currentUserId, request.getToUserId()) != null) {
            throw new BusinessException(ResultCode.ALREADY_FRIEND);
        }

        if (friendRequestMapper.findPendingRequest(currentUserId, request.getToUserId()) != null) {
            throw new BusinessException(ResultCode.REQUEST_EXISTS);
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUserId(currentUserId);
        friendRequest.setToUserId(request.getToUserId());
        friendRequest.setMessage(request.getMessage());
        friendRequest.setStatus(0);
        friendRequest.setExpireTime(LocalDateTime.now().plusDays(7));
        friendRequest.setCreatedAt(LocalDateTime.now());
        friendRequestMapper.insert(friendRequest);

        // 通过WebSocket实时推送好友请求通知给目标用户
        User fromUser = userMapper.selectById(currentUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
        String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;
        String pushMsg = JSONUtil.createObj()
                .set("type", "friend_request")
                .set("requestId", friendRequest.getId())
                .set("fromUserId", currentUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("fromUserAvatar", fromUserAvatar)
                .set("message", request.getMessage())
                .set("createdAt", friendRequest.getCreatedAt().toString())
                .toString();
        sessionManager.sendMessage(request.getToUserId(), pushMsg);
    }

    /** 获取待处理的好友请求列表 @param currentUserId 当前用户ID @return 待处理的好友请求列表 */
    @Override
    public List<FriendRequestVO> getFriendRequests(Long currentUserId) {
        return friendRequestMapper.findPendingRequests(currentUserId).stream()
                .map(req -> {
                    User fromUser = userMapper.selectById(req.getFromUserId());
                    return FriendRequestVO.builder()
                            .id(req.getId())
                            .fromUserId(req.getFromUserId())
                            .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                            .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                            .message(req.getMessage())
                            .status(req.getStatus())
                            .createdAt(req.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 处理好友请求（接受/拒绝），校验请求所有权和有效期 @param currentUserId 当前用户ID @param requestId 请求ID @param request 处理结果 */
    @Override
    @Transactional
    public void handleFriendRequest(Long currentUserId, Long requestId, HandleFriendRequest request) {
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
        if (friendRequest == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }
        if (!friendRequest.getToUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (friendRequest.getExpireTime() != null && friendRequest.getExpireTime().isBefore(LocalDateTime.now())) {
            friendRequest.setStatus(3);
            friendRequestMapper.updateById(friendRequest);
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND.getCode(), "好友申请已过期");
        }

        friendRequest.setStatus(request.getStatus());
        friendRequest.setHandledTime(LocalDateTime.now());
        friendRequestMapper.updateById(friendRequest);

        if (request.getStatus() == 1) {
            Friend friend1 = new Friend();
            friend1.setUserId(currentUserId);
            friend1.setFriendId(friendRequest.getFromUserId());
            friend1.setGroupName("我的好友");
            friend1.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(friendRequest.getFromUserId());
            friend2.setFriendId(currentUserId);
            friend2.setGroupName("我的好友");
            friend2.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(friend2);
        }

        // 通过WebSocket实时推送处理结果给请求发起方
        User handler = userMapper.selectById(currentUserId);
        String handlerNickname = handler != null ? handler.getNickname() : "未知用户";
        String resultJson = JSONUtil.createObj()
                .set("type", "friend_request_handled")
                .set("requestId", requestId)
                .set("status", request.getStatus())
                .set("handledByUserId", currentUserId)
                .set("handledByNickname", handlerNickname)
                .toString();
        sessionManager.sendMessage(friendRequest.getFromUserId(), resultJson);
    }
}
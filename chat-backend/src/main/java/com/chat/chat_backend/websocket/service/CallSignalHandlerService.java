package com.chat.chat_backend.websocket.service;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WebRTC通话信令转发服务，通过WebSocket在端对端之间转发offer/answer/ICE候选消息
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSignalHandlerService {

    /** 用户映射器，用于获取发送方显示名称 */
    private final UserMapper userMapper;

    /** 会话管理器，用于向目标用户发送信令消息 */
    private final WebSocketSessionManager sessionManager;

    /**
     * 构建并通过WebSocket发送通话信令消息给目标用户
     * offer/answer动作包含SDP，ice-candidate动作包含候选详情
     * @param fromUserId 发送方用户ID
     * @param toUserId 目标用户ID
     * @param callType 通话类型（如video/voice）
     * @param action 信令动作（offer/answer/ice-candidate/end）
     * @param sdp offer/answer的SDP字符串
     * @param candidate ICE候选者字符串
     * @param sdpMid ICE候选者的SDP mid
     * @param sdpMLineIndex ICE候选者的SDP m-line索引
     */
    public void forward(Long fromUserId, Long toUserId, String callType, String action,
                        String sdp, String candidate, String sdpMid, Integer sdpMLineIndex) {

        // 获取发送方昵称
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "用户";

        // 构建信令消息
        var response = JSONUtil.createObj()
                .set("type", "call")
                .set("action", action)
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("callType", callType);

        // 根据动作类型添加不同字段
        if ("offer".equals(action) || "answer".equals(action)) {
            response.set("sdp", sdp);
        } else if ("ice-candidate".equals(action)) {
            response.set("candidate", candidate);
            response.set("sdpMid", sdpMid);
            response.set("sdpMLineIndex", sdpMLineIndex);
        }

        // 发送给目标用户
        sessionManager.sendMessage(toUserId, response.toString());
        log.info("转发通话信令: {} -> {}, action={}", fromUserId, toUserId, action);
    }
}
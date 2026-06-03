package com.chat.chat_backend.websocket.handler;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.websocket.service.CallSignalHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebRTC通话信令处理器，处理offer/answer/ICE候选消息
 * 委托CallSignalHandlerService进行信令转发
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallSignalHandler {

    /** 通话信令转发服务 */
    private final CallSignalHandlerService callSignalHandlerService;

    /**
     * 处理通话信令消息并转发给目标用户
     * 校验目标用户存在且与发送方不同
     * @param fromUserId 主叫方用户ID
     * @param json JSON负载，包含action、toUserId、callType和信令数据
     */
    public void handle(Long fromUserId, JSONObject json) {
        String action = json.getStr("action");
        Long toUserId = json.getLong("toUserId");
        String callType = json.getStr("callType");

        // 校验目标用户有效
        if (toUserId == null || toUserId.equals(fromUserId)) {
            log.warn("无效的通话目标用户: from={}, to={}", fromUserId, toUserId);
            return;
        }

        String sdp = json.getStr("sdp");
        String candidate = json.getStr("candidate");
        String sdpMid = json.getStr("sdpMid");
        Integer sdpMLineIndex = json.getInt("sdpMLineIndex");

        callSignalHandlerService.forward(fromUserId, toUserId, callType, action, sdp, candidate, sdpMid, sdpMLineIndex);
    }
}
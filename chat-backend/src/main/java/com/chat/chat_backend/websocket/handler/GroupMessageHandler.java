package com.chat.chat_backend.websocket.handler;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.websocket.service.GroupMessageHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 群聊消息处理器，委托GroupMessageHandlerService进行消息持久化和推送
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupMessageHandler {

    /** 群消息发送与通知服务 */
    private final GroupMessageHandlerService groupMessageHandlerService;

    /**
     * 处理收到的群聊消息，触发消息发送和通知
     * @param fromUserId 发送方用户ID
     * @param json JSON负载，包含groupId、content、messageType和duration
     */
    public void handle(Long fromUserId, JSONObject json) {
        Long groupId = json.getLong("groupId");
        String content = json.getStr("content");
        Integer messageType = json.getInt("messageType", 1);
        Integer duration = json.getInt("duration");
        Long replyToId = json.getLong("replyToId");

        log.info("群聊消息: from={}, groupId={}, type={}, replyTo={}", fromUserId, groupId, messageType, replyToId);

        groupMessageHandlerService.sendAndNotify(fromUserId, groupId, content, messageType, duration, replyToId);
    }
}
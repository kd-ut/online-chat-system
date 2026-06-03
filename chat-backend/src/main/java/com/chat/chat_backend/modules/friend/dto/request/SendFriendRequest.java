package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
/**
 * 发送好友请求请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class SendFriendRequest {
    /** 目标用户ID */
    private Long toUserId;
    /** 验证消息 */
    private String message;
}
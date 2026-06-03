package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
/**
 * 好友请求处理请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class HandleFriendRequest {
    /** 处理状态：1-同意，2-拒绝 */
    private Integer status;
}
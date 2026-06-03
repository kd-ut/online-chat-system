package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
/**
 * 移动好友分组请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class MoveFriendGroupRequest {
    /** 目标分组名称 */
    private String groupName;
}
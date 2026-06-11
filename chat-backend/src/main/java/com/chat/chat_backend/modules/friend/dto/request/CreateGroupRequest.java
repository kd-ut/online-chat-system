package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
/**
 * 创建好友分组请求参数
 * @author chat-backend
 * @since 2026-06-11
 */
public class CreateGroupRequest {
    /** 分组名称 */
    private String groupName;
}
package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
/**
 * 重命名好友分组请求参数
 * @author chat-backend
 * @since 2026-06-11
 */
public class RenameGroupRequest {
    /** 新分组名称 */
    private String groupName;
}
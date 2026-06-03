package com.chat.chat_backend.modules.group.dto.response;

import lombok.Data;

@Data
/**
 * 群未读消息视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UnreadGroupVO {
    /** 发送方用户ID */
    private Long fromUserId;
    /** 未读消息数量 */
    private Integer count;
}
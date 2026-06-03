package com.chat.chat_backend.modules.group.dto.request;

import lombok.Data;

@Data
/**
 * 邀请群成员请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class InviteMemberRequest {
    /** 群组ID */
    private Long groupId;
    /** 被邀请用户ID */
    private Long userId;
}
package com.chat.chat_backend.modules.friend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 好友请求视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class FriendRequestVO {
    /** 请求ID */
    private Long id;
    /** 请求发起方用户ID */
    private Long fromUserId;
    /** 请求发起方昵称 */
    private String fromUserNickname;
    /** 请求发起方头像URL */
    private String fromUserAvatar;
    /** 验证消息 */
    private String message;
    /** 请求状态：0-待处理，1-已同意，2-已拒绝，3-已过期 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
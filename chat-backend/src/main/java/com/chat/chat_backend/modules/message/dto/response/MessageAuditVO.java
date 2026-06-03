package com.chat.chat_backend.modules.message.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 消息审计视图对象（管理员查看所有消息记录）
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class MessageAuditVO {
    /** 消息ID */
    private Long id;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 发送方昵称 */
    private String fromUserNickname;
    /** 接收方用户ID */
    private Long toUserId;
    /** 接收方昵称 */
    private String toUserNickname;
    /** 消息内容 */
    private String content;
    /** 发送时间 */
    private LocalDateTime sendTime;
}
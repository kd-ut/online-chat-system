package com.chat.chat_backend.modules.message.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
/**
 * 未读消息详情视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UnreadMessageDetailVO {
    /** 消息ID */
    private Long id;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 发送方昵称 */
    private String fromUserNickname;
    /** 发送方头像URL */
    private String fromUserAvatar;
    /** 消息类型：1-文字，2-图片，3-系统消息 */
    private Integer messageType;
    /** 消息内容 */
    private String content;
    /** 发送时间 */
    private LocalDateTime sendTime;
}
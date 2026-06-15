package com.chat.chat_backend.modules.message.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 私聊消息视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class MessageVO {
    /** 消息ID */
    private Long id;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 发送方昵称 */
    private String fromUserNickname;
    /** 语音消息时长（秒），仅消息类型为语音时有效 */
    private Integer duration;
    /** 发送方头像URL */
    private String fromUserAvatar;
    /** 接收方用户ID */
    private Long toUserId;
    /** 接收方昵称 */
    private String toUserNickname;
    /** 消息类型：1-文字，2-图片，3-文件，4-语音，5-语音通话，6-视频通话 */
    private Integer messageType;
    /** 消息内容 */
    private String content;
    /** 是否已读 */
    private Boolean isRead;
    /** 是否已撤回 */
    private Boolean isRecalled;
    /** 发送时间 */
    private LocalDateTime sendTime;
    /** 引用的消息ID */
    private Long replyToId;
    /** 被引用的消息信息 */
    private RepliedMessageInfo repliedMessage;

    @Data
    @Builder
    public static class RepliedMessageInfo {
        private Long messageId;
        private String content;
        private String fromUserNickname;
        /** 消息类型 */
        private Integer messageType;
    }
}
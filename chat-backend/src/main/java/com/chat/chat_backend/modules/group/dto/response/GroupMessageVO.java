package com.chat.chat_backend.modules.group.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 群消息视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class GroupMessageVO {
    /** 消息ID */
    private Long id;
    /** 群组ID */
    private Long groupId;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 发送方昵称 */
    private String fromUserNickname;
    /** 发送方头像URL */
    private String fromUserAvatar;
    /** 消息内容 */
    private String content;
    /** 消息类型：1-文字，2-图片，3-文件 */
    private Integer messageType;
    /** 发送时间 */
    private LocalDateTime sendTime;
    /** 是否已撤回 */
    private Boolean isRecalled;
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
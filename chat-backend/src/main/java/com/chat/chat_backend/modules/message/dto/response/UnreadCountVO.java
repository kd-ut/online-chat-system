package com.chat.chat_backend.modules.message.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
/**
 * 未读消息统计响应结果
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UnreadCountVO {
    /** 未读消息总数 */
    private Integer total;
    /** 各好友未读详情列表 */
    private List<UnreadDetail> details;
    /** 未读消息列表 */
    private List<UnreadMessage> messages;

    /**
     * 未读消息详情（按好友维度）
     *
     * @author chat-backend
     * @since 2026-05-12
     */
    @Data
    @Builder
    public static class UnreadDetail {
        /** 好友用户ID */
        private Long friendId;
        /** 好友昵称 */
        private String friendNickname;
        /** 好友头像URL */
        private String friendAvatar;
        /** 未读消息数量 */
        private Integer unreadCount;
    }

    /**
     * 未读消息条目
     *
     * @author chat-backend
     * @since 2026-05-12
     */
    @Data
    @Builder
    public static class UnreadMessage {
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
}
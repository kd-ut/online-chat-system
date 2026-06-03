package com.chat.chat_backend.modules.impression.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 印象/评价视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class ImpressionVO {
    /** 印象ID */
    private Long id;
    /** 评价方用户ID */
    private Long fromUserId;
    /** 评价方昵称 */
    private String fromUserNickname;
    /** 评价方头像URL */
    private String fromUserAvatar;
    /** 被评价方用户ID */
    private Long toUserId;
    /** 被评价方昵称 */
    private String toUserNickname;
    /** 被评价方头像URL */
    private String toUserAvatar;
    /** 印象内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
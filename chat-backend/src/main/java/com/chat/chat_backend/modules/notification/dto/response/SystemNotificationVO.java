package com.chat.chat_backend.modules.notification.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 系统通知视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class SystemNotificationVO {
    /** 通知ID */
    private Long id;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 发布管理员ID */
    private Long adminId;
    /** 发布管理员昵称 */
    private String adminNickname;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

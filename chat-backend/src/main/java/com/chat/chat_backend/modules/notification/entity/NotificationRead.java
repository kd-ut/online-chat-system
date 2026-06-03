package com.chat.chat_backend.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_read")
/**
 * 通知已读记录实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class NotificationRead {
    @TableId(type = IdType.AUTO)
    /** 记录ID，自增主键 */
    private Long id;
    /** 系统通知ID */
    private Long notificationId;
    /** 用户ID */
    private Long userId;
    /** 读取时间 */
    private LocalDateTime readAt;
}

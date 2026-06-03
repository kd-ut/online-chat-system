package com.chat.chat_backend.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_notification")
/**
 * 系统通知实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class SystemNotification {
    @TableId(type = IdType.AUTO)
    /** 通知ID，自增主键 */
    private Long id;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 发布管理员ID */
    private Long adminId;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

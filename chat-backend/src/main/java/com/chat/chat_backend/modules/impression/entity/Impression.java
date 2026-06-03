package com.chat.chat_backend.modules.impression.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("impression")
/**
 * 印象/评价实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class Impression {
    @TableId(type = IdType.AUTO)
    /** 印象ID，自增主键 */
    private Long id;
    /** 评价方用户ID */
    private Long fromUserId;
    /** 被评价方用户ID */
    private Long toUserId;
    /** 印象内容 */
    private String content;
    /** 是否删除：0-未删除，1-已删除 */
    private Integer isDelete;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
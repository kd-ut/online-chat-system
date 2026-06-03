package com.chat.chat_backend.modules.group.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_message")
/**
 * 群消息实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class GroupMessage {
    @TableId(type = IdType.AUTO)
    /** 消息ID，自增主键 */
    private Long id;
    /** 群组ID */
    private Long groupId;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 消息类型：1-文字，2-图片，3-文件 */
    private Integer messageType;
    /** 消息内容 */
    private String content;
    /** 发送时间 */
    private LocalDateTime sendTime;
    /** 撤回时间，为空表示未撤回 */
    private LocalDateTime recallTime;
}
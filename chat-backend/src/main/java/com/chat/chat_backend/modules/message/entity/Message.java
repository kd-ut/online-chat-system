package com.chat.chat_backend.modules.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
/**
 * 私聊消息实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class Message {
    @TableId(type = IdType.AUTO)
    /** 消息ID，自增主键 */
    private Long id;
    /** 发送方用户ID */
    private Long fromUserId;
    /** 接收方用户ID */
    private Long toUserId;
    /** 消息类型：1-文字，2-图片，3-文件，4-语音，5-语音通话，6-视频通话 */
    private Integer messageType;
    /** 消息内容 */
    private String content;
    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;
    /** 读取时间 */
    private LocalDateTime readTime;
    /** 撤回时间，为空表示未撤回 */
    private LocalDateTime recallTime;
    /** 发送时间 */
    private LocalDateTime sendTime;
}
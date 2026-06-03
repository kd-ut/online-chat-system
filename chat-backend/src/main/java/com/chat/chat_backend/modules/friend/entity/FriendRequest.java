package com.chat.chat_backend.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_request")
/**
 * 好友请求实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class FriendRequest {
    @TableId(type = IdType.AUTO)
    /** 请求ID，自增主键 */
    private Long id;
    /** 请求发起方用户ID */
    private Long fromUserId;
    /** 请求接收方用户ID */
    private Long toUserId;
    /** 验证消息 */
    private String message;
    /** 请求状态：0-待处理，1-已同意，2-已拒绝，3-已过期 */
    private Integer status;
    /** 处理时间 */
    private LocalDateTime handledTime;
    /** 过期时间 */
    private LocalDateTime expireTime;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
package com.chat.chat_backend.modules.group.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_group")
/**
 * 群组实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class Group {
    @TableId(type = IdType.AUTO)
    /** 群组ID，自增主键 */
    private Long id;
    /** 群组名称 */
    private String name;
    /** 群组头像URL */
    private String avatar;
    /** 群主用户ID */
    private Long ownerId;
    /** 群公告 */
    private String notice;
    /** 成员数量 */
    private Integer memberCount;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
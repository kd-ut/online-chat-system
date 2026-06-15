package com.chat.chat_backend.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_group")
/**
 * 好友分组实体
 *
 * @author chat-backend
 * @since 2026-06-11
 */
public class FriendGroup {
    @TableId(type = IdType.AUTO)
    /** 分组ID，自增主键 */
    private Long id;
    /** 所属用户ID */
    private Long userId;
    /** 分组名称 */
    private String groupName;
    /** 排序序号（越小越靠前） */
    private Integer sortOrder;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
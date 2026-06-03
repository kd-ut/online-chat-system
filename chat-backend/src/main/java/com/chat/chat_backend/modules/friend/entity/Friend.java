package com.chat.chat_backend.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend")
/**
 * 好友关系实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class Friend {
    @TableId(type = IdType.AUTO)
    /** 好友关系ID，自增主键 */
    private Long id;
    /** 用户ID（所属方） */
    private Long userId;
    /** 好友用户ID */
    private Long friendId;
    /** 所属分组名称 */
    private String groupName;
    /** 好友备注 */
    private String remark;
    /** 是否置顶：0-否，1-是 */
    private Integer isTop;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
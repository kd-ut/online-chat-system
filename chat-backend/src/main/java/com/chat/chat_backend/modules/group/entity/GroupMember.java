package com.chat.chat_backend.modules.group.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_member")
/**
 * 群成员实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class GroupMember {
    @TableId(type = IdType.AUTO)
    /** 成员关系ID，自增主键 */
    private Long id;
    /** 群组ID */
    private Long groupId;
    /** 用户ID */
    private Long userId;
    /** 群内昵称 */
    private String nickname;
    /** 角色：0-普通成员，1-群管理员，2-群主 */
    private Integer role;
    /** 未读消息数 */
    private Integer unreadCount;
    /** 加入时间 */
    private LocalDateTime joinTime;
    /** 最后阅读时间 */
    private LocalDateTime lastReadTime;
    @TableField(exist = false)
    /** 禁言截止时间，为空表示未禁言 */
    private LocalDateTime muteUntil;
}
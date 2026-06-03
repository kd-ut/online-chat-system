package com.chat.chat_backend.modules.emoji.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("emoji")
/**
 * 表情实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class Emoji {
    @TableId(type = IdType.AUTO)
    /** 表情ID，自增主键 */
    private Long id;
    /** 表情名称 */
    private String name;
    /** 表情图片URL */
    private String url;
    /** 表情分类 */
    private String category;
    /** 上传用户ID */
    private Long userId;
    /** 是否系统表情：0-否，1-是 */
    private Integer isSystem;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
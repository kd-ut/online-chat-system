package com.chat.chat_backend.modules.emoji.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 表情视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class EmojiVO {
    /** 表情ID */
    private Long id;
    /** 表情名称 */
    private String name;
    /** 表情图片URL */
    private String url;
    /** 表情分类 */
    private String category;
    /** 上传用户ID */
    private Long userId;
    /** 是否系统表情 */
    private Boolean isSystem;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
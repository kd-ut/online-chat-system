package com.chat.chat_backend.modules.emoji.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表情响应视图对象。
 */
@Data
@Builder
public class EmojiVO {

    /** 表情 ID */
    private Long id;

    /** 表情名称 */
    private String name;

    /** 表情图片 URL */
    private String url;

    /** 表情分类 */
    private String category;

    /** 上传用户 ID */
    private Long userId;

    /** 是否系统表情 */
    private Boolean isSystem;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

package com.chat.chat_backend.modules.impression.dto.request;

import lombok.Data;

@Data
/**
 * 添加印象/评价请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class AddImpressionRequest {
    /** 目标用户ID */
    private Long toUserId;
    /** 印象内容，最多100字 */
    private String content;
}
package com.chat.chat_backend.modules.group.service;

/**
 * 群消息服务接口
 * @author chat-backend
 * @since 2026-06-15
 */
public interface GroupMessageService {
    /** 撤回群消息（仅发送者可操作，2分钟内有效） */
    void recallGroupMessage(Long userId, Long messageId);
}

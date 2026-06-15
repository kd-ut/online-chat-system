package com.chat.chat_backend.modules.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.modules.message.dto.response.MessageVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadCountVO;
import java.util.List;
import java.util.Map;

/** 消息服务接口，提供聊天记录查询、消息下载、标记已读、未读数统计、撤回消息等功能 @author chat-backend @since 2026-05-12 */
public interface MessageService {

    /** 分页查询聊天记录 @param userId 用户ID @param friendId 好友ID @param page 页码 @param size 每页条数 @return 分页消息列表 */
    Page<MessageVO> getChatHistory(Long userId, Long friendId, Integer page, Integer size);

    /** 下载聊天记录 @param userId 用户ID @param friendId 好友ID @param limit 下载条数 @return 消息列表 */
    List<MessageVO> downloadChatHistory(Long userId, Long friendId, Integer limit);

    /** 标记消息为已读 @param userId 用户ID @param friendId 好友ID */
    void markAsRead(Long userId, Long friendId);

    /** 获取未读消息总数（含系统通知） @param userId 用户ID @return 未读消息统计 */
    UnreadCountVO getUnreadCount(Long userId);

    /** 撤回消息 @param userId 用户ID @param messageId 消息ID */
    void recallMessage(Long userId, Long messageId);

    /** 搜索用户相关的文本消息 @param userId 用户ID @param keyword 关键词 @param limit 数量上限 @return 搜索结果列表 */
    List<Map<String, Object>> searchMessages(Long userId, String keyword, Integer limit);
}
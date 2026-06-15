package com.chat.chat_backend.modules.message.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.MessageConstants;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.modules.message.mapper.MessageMapper;
import com.chat.chat_backend.modules.notification.mapper.SystemNotificationMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.message.dto.response.MessageVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadCountVO;
import com.chat.chat_backend.modules.group.dto.response.UnreadGroupVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadMessageDetailVO;
import com.chat.chat_backend.modules.message.entity.Message;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.message.service.MessageService;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/** 消息服务实现，处理聊天记录查询、消息下载、标记已读、未读数统计、撤回消息等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /** 消息数据访问层 */
    private final MessageMapper messageMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 系统通知数据访问层 */
    private final SystemNotificationMapper systemNotificationMapper;
    /** Redis缓存工具类 */
    private final RedisUtil redisUtil;
    /** WebSocket会话管理器 */
    private final WebSocketSessionManager sessionManager;

    /** 分页查询聊天记录（含语音消息时长解析） @param userId 用户ID @param friendId 好友ID @param page 页码 @param size 每页条数 @return 分页消息列表 */
    @Override
    public Page<MessageVO> getChatHistory(Long userId, Long friendId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Message> messages = messageMapper.findChatHistory(userId, friendId, offset, size);
        Long total = messageMapper.countChatHistory(userId, friendId);

        // 批量查询涉及的用户，避免 N+1 问题
        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        userIds.add(friendId);
        for (Message msg : messages) {
            userIds.add(msg.getFromUserId());
            userIds.add(msg.getToUserId());
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        User friend = userMap.get(friendId);
        User currentUser = userMap.get(userId);

        List<MessageVO> voList = messages.stream()
                .map(msg -> {
                    User fromUser = userMap.get(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                    String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;

                    String content = msg.getContent();
                    Integer duration = null;

                    // 解析语音消息的时长
                    if (msg.getMessageType() == 4 && content != null && content.contains("|")) {
                        String[] parts = content.split("\\|");
                        content = parts[0];
                        if (parts.length > 1) {
                            try {
                                duration = Integer.parseInt(parts[1]);
                            } catch (NumberFormatException e) {
                                duration = null;
                            }
                        }
                    }

                    // 加载被引用消息信息
                    MessageVO.RepliedMessageInfo repliedInfo = null;
                    if (msg.getReplyToId() != null) {
                        Message replied = messageMapper.selectById(msg.getReplyToId());
                        if (replied != null) {
                            User repliedUser = userMap.get(replied.getFromUserId());
                            if (repliedUser == null) {
                                repliedUser = userMapper.selectById(replied.getFromUserId());
                                if (repliedUser != null) userMap.put(repliedUser.getId(), repliedUser);
                            }
                            String repliedContent = replied.getRecallTime() != null ? "" : replied.getContent();
                            // 如果是语音消息，去除时长标记
                            if (replied.getMessageType() == 4 && repliedContent != null && repliedContent.contains("|")) {
                                repliedContent = repliedContent.split("\\|")[0];
                            }
                            repliedInfo = MessageVO.RepliedMessageInfo.builder()
                                    .messageId(msg.getReplyToId())
                                    .content(repliedContent)
                                    .fromUserNickname(repliedUser != null ? repliedUser.getNickname() : "未知用户")
                                    .messageType(replied.getMessageType())
                                    .build();
                        }
                    }

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .fromUserAvatar(fromUserAvatar)
                            .toUserId(msg.getToUserId())
                            .toUserNickname(msg.getToUserId().equals(userId) ?
                                    currentUser.getNickname() : friend.getNickname())
                            .messageType(msg.getMessageType())
                            .content(content)
                            .duration(duration)
                            .isRead(msg.getIsRead() == 1)
                            .isRecalled(msg.getRecallTime() != null)
                            .sendTime(msg.getSendTime())
                            .replyToId(msg.getReplyToId())
                            .repliedMessage(repliedInfo)
                            .build();
                })
                .collect(Collectors.toList());

        Page<MessageVO> pageResult = new Page<>(page, size);
        pageResult.setRecords(voList);
        pageResult.setTotal(total);
        return pageResult;
    }

    /** 下载聊天记录（限制下载数量，去除语音时长标记） @param userId 用户ID @param friendId 好友ID @param limit 下载条数 @return 消息列表 */
    @Override
    public List<MessageVO> downloadChatHistory(Long userId, Long friendId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = MessageConstants.DEFAULT_DOWNLOAD_SIZE;
        }
        if (limit > MessageConstants.MAX_DOWNLOAD_SIZE) {
            limit = MessageConstants.MAX_DOWNLOAD_SIZE;
        }

        List<Message> messages = messageMapper.findChatHistory(userId, friendId, 0, limit);

        // 批量查询涉及的用户，避免 N+1 问题
        Set<Long> userIds = new HashSet<>();
        for (Message msg : messages) {
            userIds.add(msg.getFromUserId());
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return messages.stream()
                .map(msg -> {
                    User fromUser = userMap.get(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

                    String content = msg.getContent();
                    // 下载时去掉时长标记
                    if (msg.getMessageType() == 4 && content != null && content.contains("|")) {
                        content = content.split("\\|")[0];
                    }

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .content(content)
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 标记好友消息为已读（同时清除Redis未读数） @param userId 用户ID @param friendId 好友ID */
    @Override
    public void markAsRead(Long userId, Long friendId) {
        messageMapper.markAsRead(userId, friendId);
        String unreadKey = RedisConstants.UNREAD_COUNT + userId;
        redisUtil.hashDelete(unreadKey, String.valueOf(friendId));
    }

    /** 获取未读消息总数（含私聊和系统通知） @param userId 用户ID @return 未读消息统计 */
    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        Integer msgTotal = messageMapper.countUnreadTotal(userId);
        Integer notifTotal = systemNotificationMapper.countUnreadByUserId(userId);
        int total = msgTotal + notifTotal;

        // 使用新的 DTO 类
        List<UnreadGroupVO> groups = messageMapper.groupUnreadByFriend(userId);

        // 批量查询用户信息，避免 N+1
        Set<Long> groupUserIds = new HashSet<>();
        for (UnreadGroupVO group : groups) {
            if (group.getFromUserId() != null) {
                groupUserIds.add(group.getFromUserId());
            }
        }
        Map<Long, User> groupUserMap = groupUserIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(groupUserIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<UnreadCountVO.UnreadDetail> details = new ArrayList<>();
        for (UnreadGroupVO group : groups) {
            if (group.getFromUserId() == null) continue;
            User friend = groupUserMap.get(group.getFromUserId());
            if (friend != null) {
                details.add(UnreadCountVO.UnreadDetail.builder()
                        .friendId(group.getFromUserId())
                        .friendNickname(friend.getNickname())
                        .friendAvatar(friend.getAvatar())
                        .unreadCount(group.getCount())
                        .build());
            }
        }

        // 使用新的 DTO 类
        List<UnreadMessageDetailVO> unreadMessages = messageMapper.findUnreadMessages(userId);
        List<UnreadCountVO.UnreadMessage> messages = new ArrayList<>();
        for (UnreadMessageDetailVO msg : unreadMessages) {
            String content = msg.getContent();
            if (content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }
            messages.add(UnreadCountVO.UnreadMessage.builder()
                    .id(msg.getId())
                    .fromUserId(msg.getFromUserId())
                    .fromUserNickname(msg.getFromUserNickname())
                    .fromUserAvatar(msg.getFromUserAvatar())
                    .messageType(msg.getMessageType())
                    .content(content)
                    .sendTime(msg.getSendTime())
                    .build());
        }

        return UnreadCountVO.builder()
                .total(total)
                .details(details)
                .messages(messages)
                .build();
    }

    /** 撤回消息（仅发送者可操作，超时后不可撤回） @param userId 用户ID @param messageId 消息ID */
    @Override
    public void recallMessage(Long userId, Long messageId) {
        // 先查询消息获取双方用户ID（用于WebSocket通知）
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }

        int updated = messageMapper.recallMessage(messageId, userId);
        if (updated == 0) {
            throw new BusinessException(ResultCode.MESSAGE_RECALL_TIMEOUT);
        }

        // 构建撤回通知并通过WebSocket推送
        var recallNotification = JSONUtil.createObj()
                .set("type", "recall")
                .set("messageId", messageId)
                .set("fromUserId", userId)
                .set("toUserId", msg.getToUserId());

        String notificationStr = recallNotification.toString();

        // 通知发送者（多端同步）
        sessionManager.sendMessage(msg.getFromUserId(), notificationStr);
        // 通知接收者
        sessionManager.sendMessage(msg.getToUserId(), notificationStr);

        log.info("消息撤回成功: messageId={}, userId={}", messageId, userId);
    }

    /** 搜索文本消息 @param userId 用户ID @param keyword 关键词 @param limit 数量上限 @return 搜索结果 */
    @Override
    public List<Map<String, Object>> searchMessages(Long userId, String keyword, Integer limit) {
        if (limit == null || limit <= 0) limit = 20;
        List<Message> messages = messageMapper.searchMessages(userId, keyword, limit);

        // 批量查询涉及的用户，避免 N+1 问题
        Set<Long> userIds = new HashSet<>();
        for (Message msg : messages) {
            userIds.add(msg.getFromUserId());
            userIds.add(msg.getToUserId());
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            // 确定对方（如果是自己发的，对方是toUserId；如果是收到的，对方是fromUserId）
            Long otherUserId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            User otherUser = userMap.get(otherUserId);

            Map<String, Object> item = new HashMap<>();
            item.put("messageId", msg.getId());
            item.put("content", msg.getContent());
            item.put("sendTime", msg.getSendTime() != null ? msg.getSendTime().toString() : null);
            item.put("fromUserId", msg.getFromUserId());
            item.put("toUserId", msg.getToUserId());
            item.put("messageType", msg.getMessageType());
            item.put("otherUserId", otherUserId);
            item.put("otherNickname", otherUser != null ? otherUser.getNickname() : "未知用户");
            item.put("otherAvatar", otherUser != null ? otherUser.getAvatar() : null);
            result.add(item);
        }
        return result;
    }
}
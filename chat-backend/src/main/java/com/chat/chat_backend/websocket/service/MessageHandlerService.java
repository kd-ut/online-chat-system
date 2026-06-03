package com.chat.chat_backend.websocket.service;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.modules.message.mapper.MessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.message.entity.Message;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 私聊消息处理与投递服务
 * 持久化消息，如果接收方在线则通过WebSocket推送
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {

    /** 私聊消息数据映射器 */
    private final MessageMapper messageMapper;

    /** 用户数据映射器，用于获取发送者昵称 */
    private final UserMapper userMapper;

    /** 会话管理器，用于向接收方推送消息 */
    private final WebSocketSessionManager sessionManager;

    /**
     * 保存消息到数据库，如果接收方在线则通过WebSocket推送
     * 语音消息时长会格式化到内容中
     * @param fromUserId 发送方用户ID
     * @param toUserId 接收方用户ID
     * @param content 消息内容
     * @param messageType 消息类型（1=文本，2=图片，3=文件，4=语音）
     * @param duration 语音消息时长（秒，可选）
     */
    public void sendAndNotify(Long fromUserId, Long toUserId, String content, Integer messageType, Integer duration) {
        // 持久化私聊消息
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMessageType(messageType);
        msg.setContent(formatContent(content, messageType, duration));
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        messageMapper.insert(msg);

        // 获取发送者显示名称
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建响应消息
        var response = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", msg.getId())
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString());

        // 语音消息附加时长字段
        if (messageType == 4 && duration != null && duration > 0) {
            response.set("duration", duration);
        }

        // 推送给接收方
        sessionManager.sendMessage(toUserId, response.toString());
    }

    /**
     * 格式化消息内容，语音消息追加时长到内容末尾
     * @param content 消息内容
     * @param messageType 消息类型
     * @param duration 语音时长（秒）
     * @return 格式化后的内容（语音消息追加"|时长"后缀）
     */
    private String formatContent(String content, Integer messageType, Integer duration) {
        if (messageType == 4 && duration != null && duration > 0) {
            return content + "|" + duration;
        }
        return content;
    }
}
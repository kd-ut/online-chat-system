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
 * 持久化消息，推送给收发双方
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
     * 保存消息到数据库，推送给接收方和发送方
     */
    public void sendAndNotify(Long fromUserId, Long toUserId, String content, Integer messageType, Integer duration, Long replyToId) {
        // 持久化私聊消息
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMessageType(messageType);
        msg.setContent(formatContent(content, messageType, duration));
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        msg.setReplyToId(replyToId);
        messageMapper.insert(msg);

        // 获取发送者显示名称
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 预先构建被引用消息信息（供接收方和发送方共用）
        Object repliedInfoObj = null;
        if (replyToId != null) {
            Message repliedMsg = messageMapper.selectById(replyToId);
            if (repliedMsg != null) {
                User repliedUser = userMapper.selectById(repliedMsg.getFromUserId());
                String repliedContent = repliedMsg.getRecallTime() != null ? "" : repliedMsg.getContent();
                if (repliedMsg.getMessageType() == 4 && repliedContent != null && repliedContent.contains("|")) {
                    repliedContent = repliedContent.split("\\|")[0];
                }
                repliedInfoObj = JSONUtil.createObj()
                        .set("messageId", replyToId)
                        .set("content", repliedContent)
                        .set("fromUserNickname", repliedUser != null ? repliedUser.getNickname() : "未知用户")
                        .set("messageType", repliedMsg.getMessageType());
            }
        }

        // 构建推送给接收方的消息
        var response = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", msg.getId())
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString());

        if (messageType == 4 && duration != null && duration > 0) {
            response.set("duration", duration);
        }
        if (replyToId != null) {
            response.set("replyToId", replyToId);
        }
        if (repliedInfoObj != null) {
            response.set("repliedMessage", repliedInfoObj);
        }

        // 推送给接收方
        sessionManager.sendMessage(toUserId, response.toString());

        // 构建推送给发送方的确认消息（含真实 messageId，用于前端更新临时 ID）
        var senderResponse = JSONUtil.createObj()
                .set("type", "message_sent")
                .set("messageId", msg.getId())
                .set("toUserId", toUserId)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString())
                .set("replyToId", replyToId);

        if (repliedInfoObj != null) {
            senderResponse.set("repliedMessage", repliedInfoObj);
        }

        // 推送给发送方
        sessionManager.sendMessage(fromUserId, senderResponse.toString());
    }

    private String formatContent(String content, Integer messageType, Integer duration) {
        if (messageType == 4 && duration != null && duration > 0) {
            return content + "|" + duration;
        }
        return content;
    }
}

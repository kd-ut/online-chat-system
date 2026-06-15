package com.chat.chat_backend.websocket.service;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.modules.group.mapper.GroupMemberMapper;
import com.chat.chat_backend.modules.group.mapper.GroupMessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import com.chat.chat_backend.modules.group.entity.GroupMessage;
import com.chat.chat_backend.modules.group.service.GroupMuteService;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群聊消息处理与投递服务
 * 验证群成员身份和禁言状态，持久化消息，更新未读计数，推送给在线群成员
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageHandlerService {

    /** 群成员数据映射器 */
    private final GroupMemberMapper groupMemberMapper;

    /** 群消息数据映射器 */
    private final GroupMessageMapper groupMessageMapper;

    /** 用户数据映射器，用于获取发送者昵称 */
    private final UserMapper userMapper;

    /** 会话管理器，用于向在线群成员推送消息 */
    private final WebSocketSessionManager sessionManager;

    /** 禁言管理服务 */
    private final GroupMuteService groupMuteService;

    /**
     * 验证群成员身份和禁言状态，持久化消息，更新未读计数，推送给所有在线群成员
     * @param fromUserId 发送方用户ID
     * @param groupId 目标群组ID
     * @param content 消息内容
     * @param messageType 消息类型（1=文本，2=图片等）
     * @param duration 语音消息时长（可选）
     */
    public void sendAndNotify(Long fromUserId, Long groupId, String content, Integer messageType, Integer duration, Long replyToId) {
        // 验证发送者是否群成员
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        boolean isMember = members.stream().anyMatch(m -> m.getUserId().equals(fromUserId));
        if (!isMember) {
            log.error("用户 {} 不是群 {} 的成员", fromUserId, groupId);
            return;
        }

        // 检查发送者是否被禁言
        if (groupMuteService.isMuted(groupId, fromUserId)) {
            log.warn("用户 {} 在群 {} 中被禁言", fromUserId, groupId);
            return;
        }

        // 持久化群消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setFromUserId(fromUserId);
        msg.setMessageType(messageType);
        msg.setContent(content);
        msg.setSendTime(LocalDateTime.now());
        msg.setReplyToId(replyToId);
        groupMessageMapper.insert(msg);

        // 增加群内其他成员的未读计数
        groupMemberMapper.incrementUnreadCount(groupId, fromUserId);

        // 获取发送者显示名称
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建响应消息
        var response = JSONUtil.createObj()
                .set("type", "group_message")
                .set("messageId", msg.getId())
                .set("groupId", groupId)
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("duration", duration)
                .set("sendTime", msg.getSendTime().toString());

        // 加载被引用消息信息
        if (replyToId != null) {
            GroupMessage repliedMsg = groupMessageMapper.selectById(replyToId);
            if (repliedMsg != null) {
                User repliedUser = userMapper.selectById(repliedMsg.getFromUserId());
                String repliedContent = repliedMsg.getRecallTime() != null ? "" : repliedMsg.getContent();
                response.set("replyToId", replyToId);
                var repliedInfo = JSONUtil.createObj()
                        .set("messageId", replyToId)
                        .set("content", repliedContent)
                        .set("fromUserNickname", repliedUser != null ? repliedUser.getNickname() : "未知用户")
                        .set("messageType", repliedMsg.getMessageType());
                response.set("repliedMessage", repliedInfo);
            }
        }

        String responseStr = response.toString();

        // 推送给除发送者外的所有在线群成员
        for (GroupMember member : members) {
            if (!member.getUserId().equals(fromUserId) && sessionManager.isOnline(member.getUserId())) {
                sessionManager.sendMessage(member.getUserId(), responseStr);
            }
        }

        // 推送给发送方确认消息（含真实 messageId，用于前端更新临时 ID）
        var senderResponse = JSONUtil.createObj()
                .set("type", "group_message_sent")
                .set("messageId", msg.getId())
                .set("groupId", groupId)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString())
                .set("replyToId", replyToId);

        if (replyToId != null && response.get("repliedMessage") != null) {
            senderResponse.set("repliedMessage", response.get("repliedMessage"));
        }

        sessionManager.sendMessage(fromUserId, senderResponse.toString());
    }
}
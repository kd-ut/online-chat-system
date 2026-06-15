package com.chat.chat_backend.modules.group.service.impl;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import com.chat.chat_backend.modules.group.entity.GroupMessage;
import com.chat.chat_backend.modules.group.mapper.GroupMemberMapper;
import com.chat.chat_backend.modules.group.mapper.GroupMessageMapper;
import com.chat.chat_backend.modules.group.service.GroupMessageService;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 群消息服务实现，处理群消息撤回等业务逻辑
 * @author chat-backend
 * @since 2026-06-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl implements GroupMessageService {

    private final GroupMessageMapper groupMessageMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final WebSocketSessionManager sessionManager;

    @Override
    public void recallGroupMessage(Long userId, Long messageId) {
        // 先获取消息信息（用于后续WebSocket通知）
        GroupMessage msg = groupMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 执行撤回（SQL已校验2分钟限制和发送者身份）
        int updated = groupMessageMapper.recallGroupMessage(messageId, userId);
        if (updated == 0) {
            throw new BusinessException(ResultCode.MESSAGE_RECALL_TIMEOUT);
        }

        // 构建撤回通知并推送给群成员
        var recallNotification = JSONUtil.createObj()
                .set("type", "group_recall")
                .set("messageId", messageId)
                .set("groupId", msg.getGroupId())
                .set("fromUserId", userId);

        String notificationStr = recallNotification.toString();

        // 推送给所有在线群成员（包括发送者，以便多端同步）
        List<GroupMember> members = groupMemberMapper.findByGroupId(msg.getGroupId());
        for (GroupMember member : members) {
            if (sessionManager.isOnline(member.getUserId())) {
                sessionManager.sendMessage(member.getUserId(), notificationStr);
            }
        }

        log.info("群消息撤回成功: messageId={}, userId={}, groupId={}", messageId, userId, msg.getGroupId());
    }
}

package com.chat.chat_backend.modules.notification.service.impl;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.modules.notification.mapper.SystemNotificationMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.notification.dto.response.SystemNotificationVO;
import com.chat.chat_backend.modules.notification.entity.SystemNotification;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.notification.service.SystemNotificationService;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 系统通知服务实现，处理通知发送、未读通知查询、标记已读等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl implements SystemNotificationService {

    /** 系统通知数据访问层 */
    private final SystemNotificationMapper notificationMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** WebSocket会话管理器（用于实时推送通知） */
    private final WebSocketSessionManager sessionManager;

    /** 发送系统通知（持久化后通过WebSocket推送给所有在线用户） @param adminId 管理员ID @param title 通知标题 @param content 通知内容 */
    @Override
    @Transactional
    public void sendNotification(Long adminId, String title, String content) {
        SystemNotification notification = new SystemNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setAdminId(adminId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);

        User admin = userMapper.selectById(adminId);
        String adminNickname = admin != null ? admin.getNickname() : "系统管理员";

        String jsonMessage = JSONUtil.createObj()
                .set("type", "notification")
                .set("notificationId", notification.getId())
                .set("title", title)
                .set("content", content)
                .set("adminNickname", adminNickname)
                .set("createdAt", notification.getCreatedAt().toString())
                .toString();

        for (Long userId : sessionManager.getAllSessions().keySet()) {
            if (!userId.equals(adminId)) {
                sessionManager.sendMessage(userId, jsonMessage);
            }
        }

        log.info("系统通知已发送: title={}, adminId={}", title, adminId);
    }

    /** 获取用户未读的系统通知列表 @param userId 用户ID @return 未读通知列表 */
    @Override
    public List<SystemNotificationVO> getUnreadNotifications(Long userId) {
        List<SystemNotification> notifications = notificationMapper.findUnreadByUserId(userId);
        return notifications.stream().map(n -> toVO(n)).collect(Collectors.toList());
    }

    /** 获取指定管理员发送的通知列表 @param adminId 管理员ID @return 通知列表 */
    @Override
    public List<SystemNotificationVO> getNotificationsByAdmin(Long adminId) {
        List<SystemNotification> notifications = notificationMapper.findByAdminId(adminId);
        return notifications.stream().map(n -> toVO(n)).collect(Collectors.toList());
    }

    /** 系统通知实体转VO @param n 系统通知实体 @return 系统通知VO */
    private SystemNotificationVO toVO(SystemNotification n) {
        User admin = userMapper.selectById(n.getAdminId());
        return SystemNotificationVO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .adminId(n.getAdminId())
                .adminNickname(admin != null ? admin.getNickname() : "系统管理员")
                .createdAt(n.getCreatedAt())
                .build();
    }

    /** 获取用户未读通知数量 @param userId 用户ID @return 未读数量 */
    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /** 标记系统通知为已读（幂等操作） @param userId 用户ID @param notificationId 通知ID */
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        int exists = notificationMapper.existsRead(notificationId, userId);
        if (exists == 0) {
            notificationMapper.markAsRead(notificationId, userId);
        }
    }
}

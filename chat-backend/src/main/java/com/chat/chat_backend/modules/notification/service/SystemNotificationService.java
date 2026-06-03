package com.chat.chat_backend.modules.notification.service;

import com.chat.chat_backend.modules.notification.dto.response.SystemNotificationVO;
import java.util.List;

/** 系统通知服务接口，提供发送通知、查询未读通知、标记已读等功能 @author chat-backend @since 2026-05-12 */
public interface SystemNotificationService {

    /** 发送系统通知（推送给所有在线用户） @param adminId 管理员ID @param title 通知标题 @param content 通知内容 */
    void sendNotification(Long adminId, String title, String content);

    /** 获取用户未读的系统通知列表 @param userId 用户ID @return 未读通知列表 */
    List<SystemNotificationVO> getUnreadNotifications(Long userId);

    /** 获取指定管理员发送的通知列表 @param adminId 管理员ID @return 通知列表 */
    List<SystemNotificationVO> getNotificationsByAdmin(Long adminId);

    /** 获取用户未读通知数量 @param userId 用户ID @return 未读数量 */
    Integer getUnreadCount(Long userId);

    /** 标记系统通知为已读 @param userId 用户ID @param notificationId 通知ID */
    void markAsRead(Long userId, Long notificationId);
}

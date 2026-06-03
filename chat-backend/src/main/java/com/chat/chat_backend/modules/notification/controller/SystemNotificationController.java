package com.chat.chat_backend.modules.notification.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.notification.dto.response.SystemNotificationVO;
import com.chat.chat_backend.modules.notification.service.SystemNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统通知控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@RestController
@RequestMapping("/system-notification")
@RequiredArgsConstructor
public class SystemNotificationController {

    /** 系统通知服务 */
    private final SystemNotificationService systemNotificationService;

    /**
     * 发送系统通知（仅管理员）
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param body    通知内容（包含 title 和 content）
     * @return 操作结果
     */
    @PostMapping("/send")
    public Result<Void> sendNotification(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long adminId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("只有管理员才能发送系统通知");
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            return Result.error("标题和内容不能为空");
        }
        systemNotificationService.sendNotification(adminId, title, content);
        return Result.success("通知已发送", null);
    }

    /**
     * 获取未读系统通知
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 未读通知列表及总数
     */
    @GetMapping("/unread")
    public Result<Map<String, Object>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SystemNotificationVO> notifications = systemNotificationService.getUnreadNotifications(userId);
        Integer count = systemNotificationService.getUnreadCount(userId);
        return Result.success(Map.of("total", count, "notifications", notifications));
    }

    /**
     * 标记系统通知为已读
     *
     * @param request        HTTP 请求对象（包含用户信息）
     * @param notificationId 通知 ID
     * @return 操作结果
     */
    @PutMapping("/read/{notificationId}")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long notificationId) {
        Long userId = (Long) request.getAttribute("userId");
        systemNotificationService.markAsRead(userId, notificationId);
        return Result.success("已标记已读", null);
    }
}

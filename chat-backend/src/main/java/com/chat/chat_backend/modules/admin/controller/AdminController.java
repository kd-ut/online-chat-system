package com.chat.chat_backend.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.message.dto.response.MessageAuditVO;
import com.chat.chat_backend.modules.admin.dto.response.StatisticsVO;
import com.chat.chat_backend.modules.notification.dto.response.SystemNotificationVO;
import com.chat.chat_backend.modules.admin.dto.response.UserManageVO;
import com.chat.chat_backend.modules.admin.service.AdminService;
import com.chat.chat_backend.modules.notification.service.SystemNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    /** 管理员服务 */
    private final AdminService adminService;
    /** 系统通知服务 */
    private final SystemNotificationService systemNotificationService;

    /**
     * 获取管理员已发送的通知列表
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 通知列表
     */
    @GetMapping("/notifications")
    public Result<List<SystemNotificationVO>> getNotifications(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("无权限");
        }
        Long adminId = (Long) request.getAttribute("userId");
        List<SystemNotificationVO> list = systemNotificationService.getNotificationsByAdmin(adminId);
        return Result.success(list);
    }

    /**
     * 获取用户列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（可选）
     * @return 用户列表分页结果
     */
    @GetMapping("/users")
    public Result<Page<UserManageVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<UserManageVO> result = adminService.getUserList(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 禁用/启用用户
     *
     * @param userId 用户 ID
     * @param status 状态（0-禁用，1-启用）
     * @return 操作结果
     */
    @PutMapping("/user/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return Result.success("操作成功", null);
    }

    /**
     * 获取聊天记录（审计）
     *
     * @param page       页码
     * @param size       每页大小
     * @param fromUserId 发送方用户 ID（可选）
     * @param toUserId   接收方用户 ID（可选）
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 聊天记录分页结果
     */
    @GetMapping("/messages")
    public Result<Page<MessageAuditVO>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long fromUserId,
            @RequestParam(required = false) Long toUserId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<MessageAuditVO> result = adminService.getMessageList(page, size, fromUserId, toUserId, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO result = adminService.getStatistics();
        return Result.success(result);
    }
}
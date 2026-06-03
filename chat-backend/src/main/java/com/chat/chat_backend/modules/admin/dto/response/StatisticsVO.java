package com.chat.chat_backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 系统统计数据视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class StatisticsVO {
    /** 总用户数 */
    private Long totalUsers;
    /** 今日活跃用户数 */
    private Long todayActiveUsers;
    /** 今日消息总数 */
    private Long todayMessages;
    /** 当前在线用户数 */
    private Integer onlineUsers;
}
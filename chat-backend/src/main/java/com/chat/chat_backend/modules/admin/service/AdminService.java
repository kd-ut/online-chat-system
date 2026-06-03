package com.chat.chat_backend.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.modules.message.dto.response.MessageAuditVO;
import com.chat.chat_backend.modules.admin.dto.response.StatisticsVO;
import com.chat.chat_backend.modules.admin.dto.response.UserManageVO;

/** 管理员服务接口，提供用户管理、消息审计、平台统计等功能 @author chat-backend @since 2026-05-12 */
public interface AdminService {

    /** 分页查询用户列表 @param page 页码 @param size 每页条数 @param keyword 搜索关键词 @return 用户管理分页列表 */
    Page<UserManageVO> getUserList(Integer page, Integer size, String keyword);

    /** 启用/禁用用户账号 @param userId 用户ID @param status 状态（1启用/0禁用） */
    void updateUserStatus(Long userId, Integer status);

    /** 分页查询消息审计列表 @param page 页码 @param size 每页条数 @param fromUserId 发送者ID @param toUserId 接收者ID @param startTime 开始日期 @param endTime 结束日期 @return 消息审计分页列表 */
    Page<MessageAuditVO> getMessageList(Integer page, Integer size,
                                        Long fromUserId, Long toUserId,
                                        String startTime, String endTime);

    /** 获取平台统计信息 @return 平台统计数据 */
    StatisticsVO getStatistics();
}
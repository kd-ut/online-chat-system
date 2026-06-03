package com.chat.chat_backend.modules.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.notification.entity.NotificationRead;
import com.chat.chat_backend.modules.notification.entity.SystemNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统通知数据访问接口，继承Mybatis-Plus BaseMapper，包含内联SQL实现已读追踪
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 查询用户的所有未读系统通知（不在notification_read表中的记录）
     * @param userId 用户ID
     * @return 未读系统通知实体列表
     */
    @Select("SELECT sn.* FROM system_notification sn " +
            "WHERE sn.id NOT IN (SELECT notification_id FROM notification_read WHERE user_id = #{userId}) " +
            "ORDER BY sn.created_at DESC")
    List<SystemNotification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 查询指定管理员创建的所有系统通知
     * @param adminId 管理员用户ID
     * @return 系统通知实体列表
     */
    @Select("SELECT * FROM system_notification WHERE admin_id = #{adminId} ORDER BY created_at DESC")
    List<SystemNotification> findByAdminId(@Param("adminId") Long adminId);

    /**
     * 统计用户的未读系统通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM system_notification sn " +
            "WHERE sn.id NOT IN (SELECT notification_id FROM notification_read WHERE user_id = #{userId})")
    Integer countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 将通知标记为已读（向notification_read表插入记录）
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 插入的行数
     */
    @Insert("INSERT INTO notification_read (notification_id, user_id, read_at) VALUES (#{notificationId}, #{userId}, NOW())")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 检查用户是否已读指定通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 计数（0=未读，1=已读）
     */
    @Select("SELECT COUNT(*) FROM notification_read WHERE notification_id = #{notificationId} AND user_id = #{userId}")
    int existsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}

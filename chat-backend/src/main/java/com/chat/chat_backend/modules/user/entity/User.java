package com.chat.chat_backend.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
/**
 * 用户实体
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class User {
    @TableId(type = IdType.AUTO)
    /** 用户ID，自增主键 */
    private Long id;
    /** 用户名，唯一标识 */
    private String username;
    /** 密码（加密存储） */
    private String password;
    /** 用户昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 个性签名 */
    private String signature;
    /** 邮箱地址 */
    private String email;
    /** 最后登录IP */
    private String lastLoginIp;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 角色：USER-普通用户，ADMIN-管理员 */
    private String role;
    /** 状态：0-正常，1-禁用 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
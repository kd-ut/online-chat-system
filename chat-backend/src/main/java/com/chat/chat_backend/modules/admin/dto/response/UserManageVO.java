package com.chat.chat_backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
/**
 * 用户管理视图对象（管理员使用）
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UserManageVO {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 角色：USER-普通用户，ADMIN-管理员 */
    private String role;
    /** 状态：0-正常，1-禁用 */
    private Integer status;
    /** 注册时间 */
    private LocalDateTime createdAt;
}
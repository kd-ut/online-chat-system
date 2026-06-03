package com.chat.chat_backend.modules.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 用户信息响应结果
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UserInfoResponse {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 用户昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 个性签名 */
    private String signature;
    /** 角色：USER-普通用户，ADMIN-管理员 */
    private String role;
}
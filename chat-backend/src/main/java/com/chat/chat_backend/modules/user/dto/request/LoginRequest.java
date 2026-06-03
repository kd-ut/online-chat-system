package com.chat.chat_backend.modules.user.dto.request;

import lombok.Data;

@Data
/**
 * 用户登录请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class LoginRequest {
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
}
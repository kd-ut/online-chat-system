package com.chat.chat_backend.modules.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 登录响应结果
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class LoginResponse {
    /** JWT认证令牌 */
    private String token;
    /** 登录用户基本信息 */
    private UserInfoResponse user;
}
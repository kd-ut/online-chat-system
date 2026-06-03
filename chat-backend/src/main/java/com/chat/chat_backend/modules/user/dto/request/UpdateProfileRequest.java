package com.chat.chat_backend.modules.user.dto.request;

import lombok.Data;

@Data
/**
 * 用户资料更新请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class UpdateProfileRequest {
    /** 用户昵称 */
    private String nickname;
    /** 个性签名 */
    private String signature;
}
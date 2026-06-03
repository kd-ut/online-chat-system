package com.chat.chat_backend.modules.user.service;

import com.chat.chat_backend.modules.user.dto.request.LoginRequest;
import com.chat.chat_backend.modules.user.dto.request.RegisterRequest;
import com.chat.chat_backend.modules.user.dto.request.UpdateProfileRequest;
import com.chat.chat_backend.modules.user.dto.response.LoginResponse;
import com.chat.chat_backend.modules.user.dto.response.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

/** 用户服务接口，提供注册、登录、信息查询、资料修改、头像上传等功能 @author chat-backend @since 2026-05-12 */
public interface UserService {

    /** 用户注册 @param request 注册请求（用户名、密码、昵称） */
    void register(RegisterRequest request);

    /** 用户登录 @param request 登录凭证（用户名、密码） @return 登录响应（JWT令牌 + 用户信息） */
    LoginResponse login(LoginRequest request);

    /** 获取用户公开信息 @param userId 用户ID @return 用户信息响应 */
    UserInfoResponse getUserInfo(Long userId);

    /** 更新用户资料 @param userId 用户ID @param request 更新请求（昵称、个性签名） @return 更新后的用户信息 */
    UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request);

    /** 更新用户头像 @param userId 用户ID @param file 头像图片文件 @return 头像URL */
    String updateAvatar(Long userId, MultipartFile file);

    /** 获取用户公开资料 @param userId 用户ID @return 用户公开资料 */
    UserInfoResponse getUserProfile(Long userId);
}
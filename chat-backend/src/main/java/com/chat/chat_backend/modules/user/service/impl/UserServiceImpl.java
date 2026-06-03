package com.chat.chat_backend.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.JwtUtil;
import com.chat.chat_backend.common.utils.OssUtil;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.user.dto.request.LoginRequest;
import com.chat.chat_backend.modules.user.dto.request.RegisterRequest;
import com.chat.chat_backend.modules.user.dto.request.UpdateProfileRequest;
import com.chat.chat_backend.modules.user.dto.response.LoginResponse;
import com.chat.chat_backend.modules.user.dto.response.UserInfoResponse;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/** 用户服务实现，处理注册、登录、信息查询、资料修改、头像上传等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** JWT令牌工具类 */
    private final JwtUtil jwtUtil;
    /** Redis缓存工具类 */
    private final RedisUtil redisUtil;
    /** OSS对象存储工具类 */
    private final OssUtil ossUtil;

    /** BCrypt密码编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 用户注册（校验用户名唯一性，BCrypt加密存储密码） @param request 注册请求 */
    @Override
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User existUser = userMapper.selectOne(wrapper);

        if (existUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole("user");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
    }

    /** 用户登录验证，校验密码和账号状态，更新登录时间，生成JWT令牌并记录在线状态 @param request 登录请求 @return 登录响应（令牌+用户信息） */
    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 记录在线状态
        redisUtil.addToSet(RedisConstants.ONLINE_USERS, String.valueOf(user.getId()));
        redisUtil.expire(RedisConstants.ONLINE_USERS, RedisConstants.ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 构建响应 - 包含头像
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    /** 根据用户ID获取用户信息 @param userId 用户ID @return 用户信息响应 */
    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .build();
    }

    /** 更新用户昵称和个性签名 @param userId 用户ID @param request 更新请求 @return 更新后的用户信息 */
    @Override
    @Transactional
    public UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .build();
    }

    /** 通过OSS上传更新用户头像 @param userId 用户ID @param file 头像文件 @return 头像URL地址 */
    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        try {
            String avatarUrl = ossUtil.uploadFile(file, "avatar/");

            user.setAvatar(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户 {} 头像更新成功: {}", userId, avatarUrl);
            return avatarUrl;
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException("头像上传失败");
        }
    }

    @Override
    public UserInfoResponse getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .signature(user.getSignature())
                .role(user.getRole())
                .build();
    }
}
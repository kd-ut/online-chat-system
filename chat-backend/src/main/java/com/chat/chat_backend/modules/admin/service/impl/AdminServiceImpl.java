package com.chat.chat_backend.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.modules.message.mapper.MessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.message.dto.response.MessageAuditVO;
import com.chat.chat_backend.modules.admin.dto.response.StatisticsVO;
import com.chat.chat_backend.modules.admin.dto.response.UserManageVO;
import com.chat.chat_backend.modules.message.entity.Message;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/** 管理员服务实现，处理用户管理、消息审计、平台统计等业务逻辑 @author chat-backend @since 2026-05-12 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 消息数据访问层 */
    private final MessageMapper messageMapper;
    /** Redis缓存工具类 */
    private final RedisUtil redisUtil;

    /** 分页查询用户列表（支持关键词搜索） @param page 页码 @param size 每页条数 @param keyword 搜索关键词 @return 用户管理分页列表 */
    @Override
    public Page<UserManageVO> getUserList(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        List<UserManageVO> records = userPage.getRecords().stream()
                .map(user -> UserManageVO.builder()
                        .id(user.getId()).username(user.getUsername()).nickname(user.getNickname())
                        .role(user.getRole()).status(user.getStatus()).createdAt(user.getCreatedAt()).build())
                .collect(Collectors.toList());

        Page<UserManageVO> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(userPage.getTotal());
        return result;
    }

    /** 启用/禁用用户账号（管理员账号不可禁用） @param userId 用户ID @param status 状态（1启用/0禁用） */
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if ("admin".equals(user.getRole()) && status == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能禁用管理员账号");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /** 分页查询消息审计列表（支持发送者、接收者、日期范围过滤） @param page 页码 @param size 每页条数 @param fromUserId 发送者ID @param toUserId 接收者ID @param startTime 开始日期 @param endTime 结束日期 @return 消息审计分页列表 */
    @Override
    public Page<MessageAuditVO> getMessageList(Integer page, Integer size,
                                               Long fromUserId, Long toUserId,
                                               String startTime, String endTime) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (fromUserId != null) wrapper.eq(Message::getFromUserId, fromUserId);
        if (toUserId != null) wrapper.eq(Message::getToUserId, toUserId);
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Message::getSendTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Message::getSendTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        wrapper.orderByDesc(Message::getSendTime);

        Page<Message> msgPage = messageMapper.selectPage(new Page<>(page, size), wrapper);

        List<MessageAuditVO> records = msgPage.getRecords().stream()
                .map(msg -> {
                    User from = userMapper.selectById(msg.getFromUserId());
                    User to = userMapper.selectById(msg.getToUserId());
                    return MessageAuditVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(from != null ? from.getNickname() : "未知")
                            .toUserId(msg.getToUserId())
                            .toUserNickname(to != null ? to.getNickname() : "未知")
                            .content(msg.getRecallTime() != null ? "[已撤回]" : msg.getContent())
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());

        Page<MessageAuditVO> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(msgPage.getTotal());
        return result;
    }

    /** 获取平台统计信息（总用户数、日活用户数、日消息数、在线人数） @return 平台统计数据 */
    @Override
    public StatisticsVO getStatistics() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return StatisticsVO.builder()
                .totalUsers(userMapper.selectCount(null))
                .todayActiveUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getLastLoginTime, todayStart)))
                .todayMessages(messageMapper.selectCount(new LambdaQueryWrapper<Message>().ge(Message::getSendTime, todayStart)))
                .onlineUsers(redisUtil.getSetMembers(RedisConstants.ONLINE_USERS).size())
                .build();
    }
}
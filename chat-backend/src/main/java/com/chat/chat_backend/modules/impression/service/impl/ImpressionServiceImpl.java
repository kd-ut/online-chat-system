package com.chat.chat_backend.modules.impression.service.impl;

import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.modules.impression.mapper.ImpressionMapper;
import com.chat.chat_backend.modules.message.mapper.MessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.impression.dto.request.AddImpressionRequest;
import com.chat.chat_backend.modules.impression.dto.response.ImpressionVO;
import com.chat.chat_backend.modules.impression.entity.Impression;
import com.chat.chat_backend.modules.message.entity.Message;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.impression.service.ImpressionService;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 用户评价服务实现，处理添加评价、查询评价、删除评价等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImpressionServiceImpl implements ImpressionService {

    /** 评价数据访问层 */
    private final ImpressionMapper impressionMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 消息数据访问层 */
    private final MessageMapper messageMapper;
    /** WebSocket会话管理器（用于实时推送通知） */
    private final WebSocketSessionManager sessionManager;

    /** 添加用户评价（含字数校验，自动生成系统通知消息，在线实时推送） @param userId 评价者用户ID @param request 评价请求 */
    @Override
    @Transactional
    public void addImpression(Long userId, AddImpressionRequest request) {
        if (userId.equals(request.getToUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能评价自己");
        }

        User targetUser = userMapper.selectById(request.getToUserId());
        if (targetUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (request.getContent().length() > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评价内容不能超过100字");
        }

        // 1. 保存评价
        Impression impression = new Impression();
        impression.setFromUserId(userId);
        impression.setToUserId(request.getToUserId());
        impression.setContent(request.getContent());
        impression.setIsDelete(0);
        impression.setCreatedAt(LocalDateTime.now());
        impressionMapper.insert(impression);

        // 2. 生成系统消息（类型为3表示系统消息/评价通知）
        User fromUser = userMapper.selectById(userId);
        String systemMessage = fromUser.getNickname() + " 给你添加了一条评价：" + request.getContent();

        Message msg = new Message();
        msg.setFromUserId(userId);
        msg.setToUserId(request.getToUserId());
        msg.setMessageType(3);  // 类型3：系统消息/评价通知
        msg.setContent(systemMessage);
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        messageMapper.insert(msg);

        log.info("评价通知已发送: {} -> {}", fromUser.getNickname(), targetUser.getNickname());

        // 3. 如果对方在线，实时推送通知
        if (sessionManager.isOnline(request.getToUserId())) {
            sessionManager.sendMessage(request.getToUserId(),
                    "{\"type\":\"impression\",\"fromUserId\":" + userId +
                            ",\"fromUserNickname\":\"" + fromUser.getNickname() +
                            "\",\"content\":\"" + request.getContent() + "\"}");
        }
    }

    /** 获取收到的评价列表 @param userId 用户ID @return 评价列表 */
    @Override
    public List<ImpressionVO> getImpressionsToMe(Long userId) {
        List<Impression> impressions = impressionMapper.findImpressionsToUser(userId);
        return impressions.stream().map(imp -> {
            User fromUser = userMapper.selectById(imp.getFromUserId());
            return ImpressionVO.builder()
                    .id(imp.getId())
                    .fromUserId(imp.getFromUserId())
                    .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                    .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                    .toUserId(imp.getToUserId())
                    .content(imp.getContent())
                    .createdAt(imp.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /** 获取发出的评价列表 @param userId 用户ID @return 评价列表 */
    @Override
    public List<ImpressionVO> getImpressionsByMe(Long userId) {
        List<Impression> impressions = impressionMapper.findImpressionsByUser(userId);
        return impressions.stream().map(imp -> {
            User toUser = userMapper.selectById(imp.getToUserId());
            return ImpressionVO.builder()
                    .id(imp.getId())
                    .fromUserId(imp.getFromUserId())
                    .toUserId(imp.getToUserId())
                    .toUserNickname(toUser != null ? toUser.getNickname() : "未知用户")
                    .toUserAvatar(toUser != null ? toUser.getAvatar() : null)
                    .content(imp.getContent())
                    .createdAt(imp.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /** 删除评价（软删除，仅评价作者可操作） @param userId 用户ID @param impressionId 评价ID */
    @Override
    public void deleteImpression(Long userId, Long impressionId) {
        Impression impression = impressionMapper.selectById(impressionId);
        if (impression == null) {
            throw new BusinessException(ResultCode.IMPRESSION_NOT_FOUND);
        }
        if (!impression.getFromUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        impression.setIsDelete(1);
        impressionMapper.updateById(impression);
    }
}
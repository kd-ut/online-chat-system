package com.chat.chat_backend.modules.group.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.group.mapper.GroupMessageMapper;
import com.chat.chat_backend.modules.group.service.GroupMessageService;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.group.dto.response.GroupMessageVO;
import com.chat.chat_backend.modules.group.entity.GroupMessage;
import com.chat.chat_backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 群消息控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupMessageController {

    /** 群消息 Mapper */
    private final GroupMessageMapper groupMessageMapper;
    /** 群消息 Service */
    private final GroupMessageService groupMessageService;
    /** 用户 Mapper */
    private final UserMapper userMapper;

    /**
     * 获取群聊历史消息
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param groupId 群聊 ID
     * @param page    页码
     * @param size    每页大小
     * @return 群聊历史消息分页结果
     */
    @GetMapping("/message/{groupId}")
    public Result<Page<GroupMessageVO>> getGroupHistory(
            HttpServletRequest request,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = (Long) request.getAttribute("userId");
        int offset = (page - 1) * size;

        int total = groupMessageMapper.countHistory(groupId);
        List<GroupMessage> messages = groupMessageMapper.findHistory(groupId, offset, size);

        // 批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        for (GroupMessage gm : messages) {
            userIds.add(gm.getFromUserId());
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<GroupMessageVO> voList = messages.stream()
                .map(msg -> {
                    User fromUser = userMap.get(msg.getFromUserId());

                    // 加载被引用消息信息
                    GroupMessageVO.RepliedMessageInfo repliedInfo = null;
                    if (msg.getReplyToId() != null) {
                        GroupMessage replied = groupMessageMapper.selectById(msg.getReplyToId());
                        if (replied != null) {
                            User repliedUser = userMap.get(replied.getFromUserId());
                            if (repliedUser == null) {
                                repliedUser = userMapper.selectById(replied.getFromUserId());
                                if (repliedUser != null) userMap.put(repliedUser.getId(), repliedUser);
                            }
                            String repliedContent = replied.getRecallTime() != null ? "" : replied.getContent();
                            repliedInfo = GroupMessageVO.RepliedMessageInfo.builder()
                                    .messageId(msg.getReplyToId())
                                    .content(repliedContent)
                                    .fromUserNickname(repliedUser != null ? repliedUser.getNickname() : "未知用户")
                                    .messageType(replied.getMessageType())
                                    .build();
                        }
                    }

                    return GroupMessageVO.builder()
                            .id(msg.getId())
                            .groupId(msg.getGroupId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUser != null ? fromUser.getNickname() : "未知用户")
                            .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : null)
                            .content(msg.getContent())
                            .messageType(msg.getMessageType())
                            .sendTime(msg.getSendTime())
                            .isRecalled(msg.getRecallTime() != null)
                            .replyToId(msg.getReplyToId())
                            .repliedMessage(repliedInfo)
                            .build();
                })
                .collect(Collectors.toList());

        Page<GroupMessageVO> pageResult = new Page<>(page, size);
        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return Result.success(pageResult);
    }

    /**
     * 撤回群消息
     *
     * @param request   HTTP 请求对象（包含用户信息）
     * @param messageId 消息 ID
     * @return 操作结果
     */
    @PutMapping("/message/recall/{messageId}")
    public Result<Void> recallGroupMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = (Long) request.getAttribute("userId");
        groupMessageService.recallGroupMessage(userId, messageId);
        return Result.success("撤回成功", null);
    }
}
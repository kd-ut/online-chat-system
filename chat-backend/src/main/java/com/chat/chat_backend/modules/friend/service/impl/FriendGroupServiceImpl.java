package com.chat.chat_backend.modules.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.modules.friend.entity.Friend;
import com.chat.chat_backend.modules.friend.entity.FriendGroup;
import com.chat.chat_backend.modules.friend.mapper.FriendGroupMapper;
import com.chat.chat_backend.modules.friend.mapper.FriendMapper;
import com.chat.chat_backend.modules.friend.service.FriendGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 好友分组服务实现 @author chat-backend @since 2026-06-11 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendGroupServiceImpl implements FriendGroupService {

    private final FriendGroupMapper friendGroupMapper;
    private final FriendMapper friendMapper;

    /** 默认分组标记（空字符串表示默认分组） */
    public static final String DEFAULT_GROUP = "";

    @Override
    public List<FriendGroup> listGroups(Long userId) {
        return friendGroupMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public FriendGroup createGroup(Long userId, String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组名称不能为空");
        }
        groupName = groupName.trim();

        // 检查同名分组
        Long count = friendGroupMapper.selectCount(new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getUserId, userId)
                .eq(FriendGroup::getGroupName, groupName));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组名称已存在");
        }

        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setGroupName(groupName);
        group.setSortOrder(0);
        group.setCreatedAt(LocalDateTime.now());
        friendGroupMapper.insert(group);
        return group;
    }

    @Override
    public void renameGroup(Long userId, Long groupId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组名称不能为空");
        }
        newName = newName.trim();

        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组不存在");
        }

        // 检查新名称是否冲突
        Long count = friendGroupMapper.selectCount(new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getUserId, userId)
                .eq(FriendGroup::getGroupName, newName)
                .ne(FriendGroup::getId, groupId));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组名称已存在");
        }

        String oldName = group.getGroupName();
        group.setGroupName(newName);
        friendGroupMapper.updateById(group);

        // 同步更新该分组下所有好友的 group_name
        friendMapper.update(null, new LambdaUpdateWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getGroupName, oldName)
                .set(Friend::getGroupName, newName));
    }

    @Override
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分组不存在");
        }

        String groupName = group.getGroupName();

        // 将该分组下的好友移至默认分组
        friendMapper.update(null, new LambdaUpdateWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getGroupName, groupName)
                .set(Friend::getGroupName, DEFAULT_GROUP));

        // 删除分组
        friendGroupMapper.deleteById(groupId);
    }
}
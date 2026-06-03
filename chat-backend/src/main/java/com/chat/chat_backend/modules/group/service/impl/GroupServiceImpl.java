package com.chat.chat_backend.modules.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.modules.group.mapper.GroupMapper;
import com.chat.chat_backend.modules.group.mapper.GroupMemberMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.group.dto.request.CreateGroupRequest;
import com.chat.chat_backend.modules.group.dto.request.InviteMemberRequest;
import com.chat.chat_backend.modules.group.dto.response.GroupVO;
import com.chat.chat_backend.modules.group.entity.Group;
import com.chat.chat_backend.modules.group.entity.GroupMember;
import com.chat.chat_backend.modules.group.service.GroupMuteService;
import com.chat.chat_backend.modules.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 群聊服务实现，处理群组CRUD、成员管理、角色分配、公告等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    /** 群聊数据访问层 */
    private final GroupMapper groupMapper;
    /** 群成员数据访问层 */
    private final GroupMemberMapper groupMemberMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 禁言管理服务 */
    private final GroupMuteService groupMuteService;

    /** 创建群聊，创建者默认为群主 @param userId 创建者用户ID @param request 创建群聊请求 @return 群聊信息 */
    @Override
    @Transactional
    public GroupVO createGroup(Long userId, CreateGroupRequest request) {
        // 1. 创建群聊
        Group group = new Group();
        group.setName(request.getName());
        group.setAvatar(request.getAvatar());
        group.setOwnerId(userId);
        group.setNotice(request.getNotice());
        group.setMemberCount(1 + (request.getMemberIds() != null ? request.getMemberIds().size() : 0));
        group.setCreatedAt(LocalDateTime.now());
        groupMapper.insert(group);
        
        // 2. 添加群主为成员
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole(2); // 群主
        owner.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(owner);
        
        // 3. 添加其他成员
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (memberId.equals(userId)) continue;
                GroupMember member = new GroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole(0);
                member.setJoinTime(LocalDateTime.now());
                groupMemberMapper.insert(member);
            }
        }
        
        return getGroupDetail(userId, group.getId());
    }

    /** 获取用户加入的群聊列表 @param userId 用户ID @return 群聊列表 */
    @Override
    public List<GroupVO> getUserGroups(Long userId) {
        List<Group> groups = groupMapper.findGroupsByUserId(userId);
        if (groups.isEmpty()) return new ArrayList<>();

        // 一次查询所有群成员信息
        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getUserId, userId)
                .in(GroupMember::getGroupId, groupIds);
        List<GroupMember> members = groupMemberMapper.selectList(wrapper);
        Map<Long, Integer> unreadMap = members.stream()
                .collect(Collectors.toMap(GroupMember::getGroupId, GroupMember::getUnreadCount));

        return groups.stream()
                .map(group -> GroupVO.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .avatar(group.getAvatar())
                        .notice(group.getNotice())
                        .ownerId(group.getOwnerId())
                        .memberCount(group.getMemberCount())
                        .unreadCount(unreadMap.getOrDefault(group.getId(), 0))
                        .createdAt(group.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /** 获取群聊详情（校验用户是否为群成员） @param userId 用户ID @param groupId 群聊ID @return 群聊详情 */
    @Override
    public GroupVO getGroupDetail(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是成员
        LambdaQueryWrapper<GroupMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(checkWrapper) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "你不是群成员");
        }
        
        return GroupVO.builder()
                .id(group.getId())
                .name(group.getName())
                .avatar(group.getAvatar())
                .notice(group.getNotice())
                .ownerId(group.getOwnerId())
                .memberCount(group.getMemberCount())
                .createdAt(group.getCreatedAt())
                .build();
    }

    /** 邀请用户加入群聊 @param userId 邀请者用户ID @param request 邀请请求 */
    @Override
    @Transactional
    public void inviteMember(Long userId, InviteMemberRequest request) {
        // 检查群是否存在
        Group group = groupMapper.selectById(request.getGroupId());
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是群成员（只有群成员才能邀请）
        LambdaQueryWrapper<GroupMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupMember::getGroupId, request.getGroupId())
                    .eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(checkWrapper) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "你不是群成员");
        }
        
        // 检查用户是否已在群中
        LambdaQueryWrapper<GroupMember> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(GroupMember::getGroupId, request.getGroupId())
                       .eq(GroupMember::getUserId, request.getUserId());
        if (groupMemberMapper.selectCount(existingWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户已在群中");
        }
        
        // 添加成员
        GroupMember member = new GroupMember();
        member.setGroupId(request.getGroupId());
        member.setUserId(request.getUserId());
        member.setRole(0);
        member.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() + 1);
        groupMapper.updateById(group);
    }

    /** 退出群聊（群主不能退群，需先转让或解散） @param userId 用户ID @param groupId 群聊ID */
    @Override
    @Transactional
    public void quitGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 群主不能退群，只能解散
        if (group.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群主不能退群，请先转让群主或解散群聊");
        }
        
        // 删除成员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
               .eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() - 1);
        groupMapper.updateById(group);
    }

    /** 解散群聊（仅群主可操作） @param userId 用户ID @param groupId 群聊ID */
    @Override
    @Transactional
    public void disbandGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }
        
        // 检查是否是群主
        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有群主可以解散群聊");
        }
        
        // 删除所有成员
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(memberWrapper);
        
        // 删除群聊
        groupMapper.deleteById(groupId);
    }

    /** 清除群聊未读消息数 @param userId 用户ID @param groupId 群聊ID */
    @Override
    public void clearUnreadCount(Long userId, Long groupId) {
        groupMemberMapper.clearUnreadCount(groupId, userId);
    }

    /** 更新群公告（群主或管理员可操作） @param userId 用户ID @param groupId 群聊ID @param notice 公告内容 */
    @Override
    @Transactional
    public void updateNotice(Long userId, Long groupId, String notice) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        }

        // 检查是否是群主或管理员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        GroupMember member = groupMemberMapper.selectOne(wrapper);

        if (member == null || (member.getRole() != 2 && member.getRole() != 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限修改群公告");
        }

        group.setNotice(notice);
        groupMapper.updateById(group);
    }

    /** 设置管理员（仅群主可操作） @param userId 用户ID @param groupId 群聊ID @param memberId 成员ID */
    @Override
    @Transactional
    public void setAdmin(Long userId, Long groupId, Long memberId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        if (!group.getOwnerId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有群主可以设置管理员");

        GroupMember member = findMember(groupId, memberId);
        if (member == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该用户不是群成员");
        if (member.getRole() == 2) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能设置群主为管理员");
        member.setRole(1);
        groupMemberMapper.updateById(member);
    }

    /** 取消管理员（仅群主可操作） @param userId 用户ID @param groupId 群聊ID @param memberId 管理员ID */
    @Override
    @Transactional
    public void removeAdmin(Long userId, Long groupId, Long memberId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");
        if (!group.getOwnerId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有群主可以取消管理员");

        GroupMember member = findMember(groupId, memberId);
        if (member == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该用户不是群成员");
        if (member.getRole() != 1) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该用户不是管理员");
        member.setRole(0);
        groupMemberMapper.updateById(member);
    }

    /** 移除群成员（群主可移除所有人，管理员不能移除其他管理员和群主） @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被移除成员ID */
    @Override
    @Transactional
    public void removeMember(Long userId, Long groupId, Long memberId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "群聊不存在");

        GroupMember operator = findMember(groupId, userId);
        if (operator == null) throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "你不是群成员");
        if (operator.getRole() == null || operator.getRole() == 0)
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限移除成员");
        if (userId.equals(memberId)) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能移除自己");

        GroupMember target = findMember(groupId, memberId);
        if (target == null) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该用户不是群成员");
        Integer targetRole = target.getRole();
        if (targetRole == null || targetRole == 2) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能移除群主");
        // 管理员不能移除其他管理员
        if (operator.getRole() == 1 && targetRole == 1)
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "管理员不能移除其他管理员");

        groupMemberMapper.deleteById(target.getId());
        group.setMemberCount(group.getMemberCount() - 1);
        groupMapper.updateById(group);
    }

    /** 禁言成员 @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被禁言成员ID @param minutes 禁言时长（分钟） */
    @Override
    public void muteMember(Long userId, Long groupId, Long memberId, Integer minutes) {
        groupMuteService.muteMember(userId, groupId, memberId, minutes);
    }

    /** 取消禁言 @param userId 操作者用户ID @param groupId 群聊ID @param memberId 被禁言成员ID */
    @Override
    public void unmuteMember(Long userId, Long groupId, Long memberId) {
        groupMuteService.unmuteMember(userId, groupId, memberId);
    }

    /** 批量禁言成员 @param userId 操作者用户ID @param groupId 群聊ID @param memberIds 被禁言成员ID列表 @param minutes 禁言时长（分钟） */
    @Override
    public void batchMute(Long userId, Long groupId, List<Long> memberIds, Integer minutes) {
        groupMuteService.batchMute(userId, groupId, memberIds, minutes);
    }

    /** 查询群成员信息 @param groupId 群聊ID @param userId 用户ID @return 群成员信息 */
    private GroupMember findMember(Long groupId, Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(wrapper);
    }
}
package com.chat.chat_backend.modules.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.modules.friend.mapper.FriendMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.friend.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendGroupVO;
import com.chat.chat_backend.modules.friend.dto.response.FriendVO;
import com.chat.chat_backend.modules.friend.entity.Friend;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.friend.service.FriendRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/** 好友关系服务实现，处理搜索用户、好友列表、删除好友、移动分组、修改备注等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRelationServiceImpl implements FriendRelationService {

    /** 好友数据访问层 */
    private final FriendMapper friendMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** Redis缓存工具类 */
    private final RedisUtil redisUtil;
    /** Redis模板（用于哈希结构的未读数存储） */
    private final RedisTemplate<String, Object> redisTemplate;

    /** 搜索用户（按用户名或昵称模糊匹配，排除当前用户） @param currentUserId 当前用户ID @param keyword 搜索关键词 @return 匹配的用户列表 */
    @Override
    public List<FriendVO> searchUsers(Long currentUserId, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .ne(User::getId, currentUserId)
                .eq(User::getStatus, 1);

        return userMapper.selectList(wrapper).stream()
                .map(user -> {
                    Friend existingFriend = friendMapper.findFriendRelation(currentUserId, user.getId());
                    return FriendVO.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .signature(user.getSignature())
                            .remark(existingFriend != null ? existingFriend.getRemark() : null)
                            .groupName(existingFriend != null ? existingFriend.getGroupName() : null)
                            .isOnline(redisUtil.isMember(RedisConstants.ONLINE_USERS, String.valueOf(user.getId())))
                            .unreadCount(0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 获取好友列表（按分组返回，含在线状态和未读消息数） @param currentUserId 当前用户ID @return 分组好友列表 */
    @Override
    public List<FriendGroupVO> getFriendList(Long currentUserId) {
        if (currentUserId == null) return new ArrayList<>();

        List<Friend> friends = friendMapper.findAllByUserId(currentUserId);
        if (friends.isEmpty()) return new ArrayList<>();

        List<Long> friendIds = friends.stream()
                .map(Friend::getFriendId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (friendIds.isEmpty()) return new ArrayList<>();

        Map<Long, User> userMap = userMapper.selectBatchIds(friendIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        String unreadKey = RedisConstants.UNREAD_COUNT + currentUserId;
        Map<Object, Object> unreadMap = redisTemplate.opsForHash().entries(unreadKey);
        Map<String, List<FriendVO>> groupMap = new HashMap<>();

        for (Friend friend : friends) {
            User friendUser = userMap.get(friend.getFriendId());
            if (friendUser == null) continue;

            String groupName = friend.getGroupName() != null ? friend.getGroupName() : "我的好友";
            Integer unreadCount = 0;
            Object value = unreadMap.get(String.valueOf(friend.getFriendId()));
            if (value != null) unreadCount = Integer.parseInt(value.toString());

            FriendVO vo = FriendVO.builder()
                    .id(friend.getId())
                    .userId(friend.getFriendId())
                    .nickname(friendUser.getNickname())
                    .avatar(friendUser.getAvatar())
                    .signature(friendUser.getSignature())
                    .remark(friend.getRemark())
                    .groupName(groupName)
                    .isOnline(redisUtil.isMember(RedisConstants.ONLINE_USERS, String.valueOf(friend.getFriendId())))
                    .unreadCount(unreadCount)
                    .build();

            groupMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(vo);
        }

        return groupMap.entrySet().stream()
                .map(entry -> FriendGroupVO.builder()
                        .groupName(entry.getKey())
                        .friends(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /** 删除好友（双向删除好友关系） @param currentUserId 当前用户ID @param friendId 好友ID */
    @Override
    @Transactional
    public void deleteFriend(Long currentUserId, Long friendId) {
        friendMapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId));
        friendMapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, friendId).eq(Friend::getFriendId, currentUserId));
    }

    /** 移动好友分组 @param currentUserId 当前用户ID @param friendId 好友ID @param request 新分组名称 */
    @Override
    public void moveFriendGroup(Long currentUserId, Long friendId, MoveFriendGroupRequest request) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId));
        if (friend == null) throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        friend.setGroupName(request.getGroupName());
        friendMapper.updateById(friend);
    }

    /** 修改好友备注 @param currentUserId 当前用户ID @param friendId 好友ID @param remark 新备注 */
    @Override
    public void updateFriendRemark(Long currentUserId, Long friendId, String remark) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, currentUserId).eq(Friend::getFriendId, friendId));
        if (friend == null) throw new BusinessException(ResultCode.FRIEND_NOT_FOUND);
        friend.setRemark(remark);
        friendMapper.updateById(friend);
    }
}
package com.chat.chat_backend.modules.friend.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.friend.dto.request.HandleFriendRequest;
import com.chat.chat_backend.modules.friend.dto.request.MoveFriendGroupRequest;
import com.chat.chat_backend.modules.friend.dto.request.SendFriendRequest;
import com.chat.chat_backend.modules.friend.dto.response.FriendGroupVO;
import com.chat.chat_backend.modules.friend.dto.response.FriendRequestVO;
import com.chat.chat_backend.modules.friend.dto.response.FriendVO;
import com.chat.chat_backend.modules.friend.service.FriendRelationService;
import com.chat.chat_backend.modules.friend.service.FriendRequestService;
import com.chat.chat_backend.common.annotation.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友控制器（优化版）
 * - 更规范 REST API
 * - 支持参数校验
 * - 解耦 HttpServletRequest
 * - 更清晰语义
 */
@Slf4j
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "好友管理", description = "好友关系、好友申请、分组管理")
public class FriendController {

    private final FriendRelationService friendRelationService;
    private final FriendRequestService friendRequestService;

    /**
     * 搜索用户
     */
    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<FriendVO>> searchUsers(@LoginUser Long userId,
                                              @RequestParam @NotBlank String keyword) {

        log.info("用户 [{}] 搜索用户，关键词: {}", userId, keyword);
        return Result.success(friendRelationService.searchUsers(userId, keyword));
    }

    /**
     * 发送好友申请
     */
    @Operation(summary = "发送好友申请")
    @PostMapping("/requests")
    public Result<Void> sendFriendRequest(@LoginUser Long userId,
                                          @Valid @RequestBody SendFriendRequest req) {

        log.info("用户 [{}] 向 [{}] 发送好友申请", userId, req.getTargetUserId());

        friendRequestService.sendFriendRequest(userId, req);
        return Result.success();
    }

    /**
     * 获取好友申请列表
     */
    @Operation(summary = "好友申请列表")
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> getFriendRequests(@LoginUser Long userId) {

        return Result.success(friendRequestService.getFriendRequests(userId));
    }

    /**
     * 同意好友申请
     */
    @Operation(summary = "同意好友申请")
    @PostMapping("/requests/{id}/accept")
    public Result<Void> accept(@LoginUser Long userId,
                               @PathVariable Long id) {

        log.info("用户 [{}] 同意好友申请 [{}]", userId, id);

        friendRequestService.accept(userId, id);
        return Result.success();
    }

    /**
     * 拒绝好友申请
     */
    @Operation(summary = "拒绝好友申请")
    @PostMapping("/requests/{id}/reject")
    public Result<Void> reject(@LoginUser Long userId,
                               @PathVariable Long id) {

        log.info("用户 [{}] 拒绝好友申请 [{}]", userId, id);

        friendRequestService.reject(userId, id);
        return Result.success();
    }

    /**
     * 好友列表
     */
    @Operation(summary = "好友列表")
    @GetMapping
    public Result<List<FriendGroupVO>> getFriendList(@LoginUser Long userId) {

        return Result.success(friendRelationService.getFriendList(userId));
    }

    /**
     * 删除好友（双向）
     */
    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@LoginUser Long userId,
                                     @PathVariable Long friendId) {

        log.info("用户 [{}] 删除好友 [{}]", userId, friendId);

        friendRelationService.deleteFriend(userId, friendId);
        return Result.success();
    }

    /**
     * 移动好友分组
     */
    @Operation(summary = "移动好友分组")
    @PutMapping("/{friendId}/group")
    public Result<Void> moveFriendGroup(@LoginUser Long userId,
                                        @PathVariable Long friendId,
                                        @Valid @RequestBody MoveFriendGroupRequest req) {

        friendRelationService.moveFriendGroup(userId, friendId, req);
        return Result.success();
    }

    /**
     * 修改好友备注
     */
    @Operation(summary = "修改好友备注")
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(@LoginUser Long userId,
                                           @PathVariable Long friendId,
                                           @RequestParam
                                           @Size(max = 50, message = "备注不能超过50字符")
                                           String remark) {

        friendRelationService.updateFriendRemark(userId, friendId, remark);
        return Result.success();
    }
}
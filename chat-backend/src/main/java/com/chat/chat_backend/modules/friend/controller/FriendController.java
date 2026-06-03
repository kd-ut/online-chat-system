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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 好友控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    /** 好友关系服务 */
    private final FriendRelationService friendRelationService;
    /** 好友请求服务 */
    private final FriendRequestService friendRequestService;

    /**
     * 搜索用户
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param keyword 搜索关键词
     * @return 好友列表
     */
    @GetMapping("/search")
    public Result<List<FriendVO>> searchUsers(HttpServletRequest request, @RequestParam String keyword) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRelationService.searchUsers(userId, keyword));
    }

    /**
     * 发送好友申请
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param req     好友申请请求
     * @return 操作结果
     */
    @PostMapping("/request")
    public Result<Void> sendFriendRequest(HttpServletRequest request, @RequestBody SendFriendRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRequestService.sendFriendRequest(userId, req);
        return Result.success("好友申请已发送", null);
    }

    /**
     * 获取好友申请列表
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 好友申请列表
     */
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> getFriendRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRequestService.getFriendRequests(userId));
    }

    /**
     * 处理好友申请
     *
     * @param request   HTTP 请求对象（包含用户信息）
     * @param requestId 好友申请 ID
     * @param req       处理好友申请请求
     * @return 操作结果
     */
    @PutMapping("/request/{requestId}")
    public Result<Void> handleFriendRequest(HttpServletRequest request,
                                            @PathVariable Long requestId,
                                            @RequestBody HandleFriendRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRequestService.handleFriendRequest(userId, requestId, req);
        String msg = req.getStatus() == 1 ? "已同意" : "已拒绝";
        return Result.success(msg, null);
    }

    /**
     * 获取好友列表（按分组）
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 好友分组列表
     */
    @GetMapping("/list")
    public Result<List<FriendGroupVO>> getFriendList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendRelationService.getFriendList(userId));
    }

    /**
     * 删除好友
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param friendId 好友 ID
     * @return 操作结果
     */
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.deleteFriend(userId, friendId);
        return Result.success("删除成功", null);
    }

    /**
     * 移动好友到其他分组
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param friendId 好友 ID
     * @param req      移动好友分组请求
     * @return 操作结果
     */
    @PutMapping("/{friendId}/group")
    public Result<Void> moveFriendGroup(HttpServletRequest request,
                                        @PathVariable Long friendId,
                                        @RequestBody MoveFriendGroupRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.moveFriendGroup(userId, friendId, req);
        return Result.success("移动成功", null);
    }

    /**
     * 修改好友备注
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param friendId 好友 ID
     * @param remark   新备注
     * @return 操作结果
     */
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(HttpServletRequest request,
                                           @PathVariable Long friendId,
                                           @RequestParam String remark) {
        Long userId = (Long) request.getAttribute("userId");
        friendRelationService.updateFriendRemark(userId, friendId, remark);
        return Result.success("修改成功", null);
    }
}
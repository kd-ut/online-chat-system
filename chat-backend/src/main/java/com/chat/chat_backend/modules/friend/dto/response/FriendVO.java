package com.chat.chat_backend.modules.friend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 好友视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class FriendVO {
    /** 好友关系ID */
    private Long id;
    /** 好友用户ID */
    private Long userId;
    /** 好友昵称 */
    private String nickname;
    /** 好友头像URL */
    private String avatar;
    /** 好友个性签名 */
    private String signature;
    /** 好友备注 */
    private String remark;
    /** 分组名称 */
    private String groupName;
    /** 是否在线 */
    private Boolean isOnline;
    /** 未读消息数 */
    private Integer unreadCount;
}
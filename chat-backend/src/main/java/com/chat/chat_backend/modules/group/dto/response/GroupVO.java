package com.chat.chat_backend.modules.group.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
/**
 * 群组视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class GroupVO {
    /** 群组ID */
    private Long id;
    /** 群组名称 */
    private String name;
    /** 群组头像URL */
    private String avatar;
    /** 群公告 */
    private String notice;
    /** 群主用户ID */
    private Long ownerId;
    /** 成员数量 */
    private Integer memberCount;
    /** 当前用户未读消息数 */
    private Integer unreadCount;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 成员列表 */
    private List<GroupMemberVO> members;
}
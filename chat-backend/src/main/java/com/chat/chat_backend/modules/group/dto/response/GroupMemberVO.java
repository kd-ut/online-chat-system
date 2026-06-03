package com.chat.chat_backend.modules.group.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * 群成员视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class GroupMemberVO {
    /** 用户ID */
    private Long userId;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL */
    private String avatar;
    /** 群内昵称 */
    private String groupNickname;
    /** 角色：0-普通成员，1-群管理员，2-群主 */
    private Integer role;
    /** 是否被禁言 */
    private Boolean muted;
}
package com.chat.chat_backend.modules.friend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
/**
 * 好友分组视图对象
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class FriendGroupVO {
    /** 分组ID（默认分组为null） */
    private Long groupId;
    /** 分组名称 */
    private String groupName;
    /** 该分组下的好友列表 */
    private List<FriendVO> friends;
}
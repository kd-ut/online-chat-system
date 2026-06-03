package com.chat.chat_backend.modules.group.dto.request;

import lombok.Data;
import java.util.List;

@Data
/**
 * 创建群组请求参数
 *
 * @author chat-backend
 * @since 2026-05-12
 */
public class CreateGroupRequest {
    /** 群组名称 */
    private String name;
    /** 群组头像URL */
    private String avatar;
    /** 群公告 */
    private String notice;
    /** 初始成员ID列表 */
    private List<Long> memberIds;
}
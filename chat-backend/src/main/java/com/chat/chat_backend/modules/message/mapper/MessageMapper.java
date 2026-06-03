package com.chat.chat_backend.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.dto.response.UnreadGroupVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadMessageDetailVO;
import com.chat.chat_backend.modules.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私聊消息数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 分页查询两个用户之间的聊天历史
     * @param userId 当前用户ID
     * @param friendId 对方用户ID
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 消息实体列表
     */
    List<Message> findChatHistory(@Param("userId") Long userId,
                                  @Param("friendId") Long friendId,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    /**
     * 统计两个用户之间的消息总数
     * @param userId 当前用户ID
     * @param friendId 对方用户ID
     * @return 消息总数
     */
    Long countChatHistory(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 统计用户的所有未读消息数
     * @param userId 用户ID
     * @return 未读消息总数
     */
    Integer countUnreadTotal(@Param("userId") Long userId);

    /**
     * 按好友分组统计用户的未读消息
     * @param userId 用户ID
     * @return 未读分组视图对象列表
     */
    List<UnreadGroupVO> groupUnreadByFriend(@Param("userId") Long userId);

    /**
     * 查询用户的未读消息详情
     * @param userId 用户ID
     * @return 未读消息详情视图对象列表
     */
    List<UnreadMessageDetailVO> findUnreadMessages(@Param("userId") Long userId);

    /**
     * 将指定发送者的所有消息标记为已读
     * @param userId 接收方用户ID
     * @param friendId 发送方用户ID
     * @return 影响的行数
     */
    Integer markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 撤回指定消息（校验消息归属）
     * @param messageId 待撤回的消息ID
     * @param userId 请求撤回的用户ID
     * @return 影响的行数（不归该用户所有时返回0）
     */
    Integer recallMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
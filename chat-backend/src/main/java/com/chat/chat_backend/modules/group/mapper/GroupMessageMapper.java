package com.chat.chat_backend.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 群消息数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {

    /**
     * 分页查询群组消息历史
     * @param groupId 群组ID
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 群消息实体列表
     */
    List<GroupMessage> findHistory(@Param("groupId") Long groupId,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 统计群组消息总数
     * @param groupId 群组ID
     * @return 消息总数
     */
    Integer countHistory(@Param("groupId") Long groupId);
}
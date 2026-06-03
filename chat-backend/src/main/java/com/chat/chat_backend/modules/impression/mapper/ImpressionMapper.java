package com.chat.chat_backend.modules.impression.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.impression.entity.Impression;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户印象/评价数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface ImpressionMapper extends BaseMapper<Impression> {

    /**
     * 查询用户收到的所有印象评价
     * @param userId 目标用户ID
     * @return 印象评价实体列表
     */
    List<Impression> findImpressionsToUser(@Param("userId") Long userId);

    /**
     * 查询用户发出的所有印象评价
     * @param userId 评价者用户ID
     * @return 印象评价实体列表
     */
    List<Impression> findImpressionsByUser(@Param("userId") Long userId);

    /**
     * 查询两个用户之间的互相印象评价
     * @param fromUserId 评价者用户ID
     * @param toUserId 被评价用户ID
     * @return 印象评价实体列表
     */
    List<Impression> findImpressionBetween(@Param("fromUserId") Long fromUserId,
                                           @Param("toUserId") Long toUserId);
}
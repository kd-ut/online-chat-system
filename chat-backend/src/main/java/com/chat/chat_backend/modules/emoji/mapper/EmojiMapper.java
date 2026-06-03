package com.chat.chat_backend.modules.emoji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.emoji.entity.Emoji;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 表情数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface EmojiMapper extends BaseMapper<Emoji> {

    /**
     * 查询所有系统提供的表情
     * @return 系统表情实体列表
     */
    List<Emoji> findSystemEmojis();

    /**
     * 查询用户上传的自定义表情
     * @param userId 用户ID
     * @return 用户表情实体列表
     */
    List<Emoji> findUserEmojis(@Param("userId") Long userId);
}
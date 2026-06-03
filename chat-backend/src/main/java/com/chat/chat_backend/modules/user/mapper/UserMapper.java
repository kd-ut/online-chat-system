package com.chat.chat_backend.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 匹配的User实体，不存在时返回null
     */
    User findByUsername(@Param("username") String username);

    /**
     * 统计当前在线用户数量
     * @return 在线用户数
     */
    Integer countOnlineUsers();
}
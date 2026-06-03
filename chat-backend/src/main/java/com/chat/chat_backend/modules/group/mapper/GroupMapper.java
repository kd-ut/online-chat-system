package com.chat.chat_backend.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.chat_backend.modules.group.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 群组数据访问接口，继承Mybatis-Plus BaseMapper
 * @author chat-backend
 * @since 2026-05-12
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    /**
     * 查询用户加入的所有群组
     * @param userId 用户ID
     * @return 群组实体列表
     */
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

}
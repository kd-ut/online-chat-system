package com.chat.chat_backend.modules.emoji.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表情实体，对应 emoji 数据表。
 */
@Data
@TableName("emoji")
public class Emoji {

    /** 表情 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表情名称 */
    private String name;

    /** 表情图片 URL */
    private String url;

    /** 表情分类 */
    private String category;

    /** 上传用户 ID，系统表情为空 */
    private Long userId;

    /** 是否系统表情：0=用户自定义，1=系统内置 */
    private Integer isSystem;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

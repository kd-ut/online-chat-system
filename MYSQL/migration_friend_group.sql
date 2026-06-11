-- 好友分组管理表
-- 执行日期: 2026-06-11

CREATE TABLE IF NOT EXISTS friend_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分组ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    group_name VARCHAR(50) NOT NULL COMMENT '分组名称',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_group (user_id, group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友分组表';

-- 将旧数据 "我的好友" 和 NULL 统一改为空字符串（默认分组）
UPDATE friend SET group_name = '' WHERE group_name IS NULL OR group_name = '我的好友';

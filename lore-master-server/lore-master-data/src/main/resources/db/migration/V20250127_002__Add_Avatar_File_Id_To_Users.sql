-- 为用户表添加头像文件ID字段
-- 用于关联file_storage表中的头像文件

USE lore_consumer;

-- 添加头像文件ID字段
ALTER TABLE users 
ADD COLUMN avatar_file_id VARCHAR(64) NULL COMMENT '头像文件ID（关联file_storage表）' 
AFTER avatar_url;

-- 添加索引
ALTER TABLE users 
ADD INDEX idx_avatar_file_id (avatar_file_id);

-- 添加注释说明
ALTER TABLE users 
MODIFY COLUMN avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像访问链接（用于显示）';

-- 更新表注释
ALTER TABLE users 
COMMENT = '用户主表（已添加头像文件ID字段）';

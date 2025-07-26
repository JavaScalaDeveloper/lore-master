-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) NOT NULL DEFAULT 'operator' COMMENT '角色：admin-超级管理员，manager-管理员，operator-操作员',
  `permissions` TEXT DEFAULT NULL COMMENT '权限列表（JSON格式）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` INT UNSIGNED DEFAULT 0 COMMENT '登录次数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`),
  UNIQUE KEY `uk_email` (`email`, `deleted`),
  UNIQUE KEY `uk_phone` (`phone`, `deleted`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- 插入默认管理员账户（如果不存在）
INSERT IGNORE INTO `admin_users` (
  `username`, 
  `password`, 
  `real_name`, 
  `email`, 
  `role`, 
  `permissions`, 
  `status`, 
  `remark`
) VALUES (
  'admin', 
  '$2a$12$rFJHQq7qZqKqKqKqKqKqKOeKqKqKqKqKqKqKqKqKqKqKqKqKqKqKq', -- 密码: 123456
  '超级管理员', 
  'admin@loremaster.com', 
  'admin', 
  '["*:*:*"]', 
  1, 
  '系统默认管理员账户'
);

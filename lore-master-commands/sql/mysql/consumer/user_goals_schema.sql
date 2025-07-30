
-- 创建用户学习目标表
CREATE TABLE `user_learning_goals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `skill_code` varchar(50) NOT NULL COMMENT '技能编码',
  `skill_name` varchar(100) NOT NULL COMMENT '技能名称（冗余存储）',
  `skill_path` varchar(200) NOT NULL COMMENT '技能路径（冗余存储）',
  `target_level` varchar(20) DEFAULT NULL COMMENT '目标等级',
  `current_progress` decimal(5,2) DEFAULT 0.00 COMMENT '当前进度百分比（0-100）',
  `start_date` date DEFAULT NULL COMMENT '开始学习日期',
  `target_date` date DEFAULT NULL COMMENT '目标完成日期',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-进行中，completed-已完成，paused-暂停，abandoned-放弃',
  `priority` tinyint(2) DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高',
  `notes` text COMMENT '学习笔记',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_skill` (`user_id`, `skill_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_skill_code` (`skill_code`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习目标表';

-- 创建学习进度记录表
CREATE TABLE `learning_progress_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `skill_code` varchar(50) NOT NULL COMMENT '技能编码',
  `progress_date` date NOT NULL COMMENT '学习日期',
  `study_hours` decimal(4,2) DEFAULT 0.00 COMMENT '学习时长（小时）',
  `progress_increment` decimal(5,2) DEFAULT 0.00 COMMENT '进度增量',
  `activity_type` varchar(50) DEFAULT NULL COMMENT '活动类型：video-视频学习，practice-练习，exam-考试等',
  `content_title` varchar(200) DEFAULT NULL COMMENT '学习内容标题',
  `notes` text COMMENT '学习记录',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_skill_date` (`user_id`, `skill_code`, `progress_date`),
  KEY `idx_progress_date` (`progress_date`),
  KEY `idx_activity_type` (`activity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度记录表';

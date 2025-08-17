-- 学习目标技能目录表
-- 使用路径编码方式，避免外键关联，支持灵活的层级结构

CREATE TABLE `learning_skill_catalog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `skill_code` varchar(50) NOT NULL COMMENT '技能编码，唯一标识',
  `skill_name` varchar(100) NOT NULL COMMENT '技能名称',
  `skill_path` varchar(200) NOT NULL COMMENT '技能路径，用/分隔，如：foreign_language/english/cet4',
  `level` tinyint(2) NOT NULL COMMENT '层级：1-一级分类，2-二级分类，3-三级目标',
  `parent_code` varchar(50) DEFAULT NULL COMMENT '父级技能编码',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标标识',
  `description` text COMMENT '技能描述',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序序号',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT '难度等级：beginner/intermediate/advanced',
  `estimated_hours` int(11) DEFAULT NULL COMMENT '预估学习时长（小时）',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签，JSON格式存储',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(50) DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(50) DEFAULT 'system' COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`skill_code`),
  KEY `idx_skill_path` (`skill_path`),
  KEY `idx_level` (`level`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习技能目录表';

-- 插入基础数据
INSERT INTO `learning_skill_catalog` VALUES 
-- 一级分类
(1, 'foreign_language', '外语', 'foreign_language', 1, NULL, '🌍', '外语学习相关技能', 1, 1, NULL, NULL, '["language", "communication"]', NOW(), NOW(), 'system', 'system'),
(2, 'it_internet', 'IT/互联网', 'it_internet', 1, NULL, '💻', 'IT和互联网技术相关技能', 2, 1, NULL, NULL, '["technology", "programming"]', NOW(), NOW(), 'system', 'system'),
(3, 'finance_audit_tax', '财务/审计/税务', 'finance_audit_tax', 1, NULL, '💰', '财务、审计、税务相关专业技能', 3, 1, NULL, NULL, '["finance", "professional"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - 外语
(10, 'english', '英语', 'foreign_language/english', 2, 'foreign_language', '🇺🇸', '英语语言技能', 1, 1, NULL, NULL, '["english", "language"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - IT/互联网
(20, 'backend', '后端', 'it_internet/backend', 2, 'it_internet', '⚙️', '后端开发技术', 1, 1, NULL, NULL, '["backend", "server", "development"]', NOW(), NOW(), 'system', 'system'),
(21, 'frontend', '前端', 'it_internet/frontend', 2, 'it_internet', '🎨', '前端开发技术', 2, 1, NULL, NULL, '["frontend", "ui", "development"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - 财务/审计/税务
(30, 'finance', '财务', 'finance_audit_tax/finance', 2, 'finance_audit_tax', '📊', '财务管理相关技能', 1, 1, NULL, NULL, '["finance", "accounting"]', NOW(), NOW(), 'system', 'system'),
(31, 'audit', '审计', 'finance_audit_tax/audit', 2, 'finance_audit_tax', '🔍', '审计相关技能', 2, 1, NULL, NULL, '["audit", "inspection"]', NOW(), NOW(), 'system', 'system'),
(32, 'tax', '税务', 'finance_audit_tax/tax', 2, 'finance_audit_tax', '📋', '税务相关技能', 3, 1, NULL, NULL, '["tax", "taxation"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 英语
(100, 'cet4', '大学英语CET-4', 'foreign_language/english/cet4', 3, 'english', NULL, '大学英语四级考试', 1, 1, 'intermediate', 200, '["cet", "exam", "university"]', NOW(), NOW(), 'system', 'system'),
(101, 'cet6', '大学英语CET-6', 'foreign_language/english/cet6', 3, 'english', NULL, '大学英语六级考试', 2, 1, 'intermediate', 300, '["cet", "exam", "university"]', NOW(), NOW(), 'system', 'system'),
(102, 'ielts', '雅思', 'foreign_language/english/ielts', 3, 'english', NULL, '国际英语语言测试系统', 3, 1, 'advanced', 400, '["ielts", "exam", "international"]', NOW(), NOW(), 'system', 'system'),
(103, 'toefl', '托福', 'foreign_language/english/toefl', 3, 'english', NULL, '托福英语考试', 4, 1, 'advanced', 450, '["toefl", "exam", "international"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 后端
(200, 'java', 'Java', 'it_internet/backend/java', 3, 'backend', NULL, 'Java编程语言', 1, 1, 'intermediate', 500, '["java", "programming", "oop"]', NOW(), NOW(), 'system', 'system'),
(201, 'bigdata_engineer', '大数据开发工程师', 'it_internet/backend/bigdata_engineer', 3, 'backend', NULL, '大数据开发相关技术栈', 2, 1, 'advanced', 800, '["bigdata", "hadoop", "spark"]', NOW(), NOW(), 'system', 'system'),
(202, 'python', 'Python', 'it_internet/backend/python', 3, 'backend', NULL, 'Python编程语言', 3, 1, 'beginner', 300, '["python", "programming", "scripting"]', NOW(), NOW(), 'system', 'system'),
(203, 'golang', 'Golang', 'it_internet/backend/golang', 3, 'backend', NULL, 'Go编程语言', 4, 1, 'intermediate', 400, '["golang", "programming", "concurrent"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 前端
(210, 'react_vue', 'React/Vue', 'it_internet/frontend/react_vue', 3, 'frontend', NULL, 'React和Vue前端框架', 1, 1, 'intermediate', 350, '["react", "vue", "framework"]', NOW(), NOW(), 'system', 'system'),
(211, 'mobile_dev', 'IOS/Android', 'it_internet/frontend/mobile_dev', 3, 'frontend', NULL, '移动端开发技术', 2, 1, 'advanced', 600, '["ios", "android", "mobile"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 财务
(300, 'cpa', '注册会计师CPA', 'finance_audit_tax/finance/cpa', 3, 'finance', NULL, '注册会计师资格认证', 1, 1, 'advanced', 1000, '["cpa", "accounting", "certification"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 审计
(310, 'cia', '国际注册内部审计师CIA', 'finance_audit_tax/audit/cia', 3, 'audit', NULL, '国际注册内部审计师认证', 1, 1, 'advanced', 800, '["cia", "audit", "certification"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 税务
(320, 'cta', '税务师CTA', 'finance_audit_tax/tax/cta', 3, 'tax', NULL, '税务师资格认证', 1, 1, 'advanced', 600, '["cta", "tax", "certification"]', NOW(), NOW(), 'system', 'system');



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

-- =============================================
-- 管理端数据库设计 (lore_admin)
-- 主要用于：系统配置、内容管理、数据分析、运营管理
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lore_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lore_admin;

-- 1. 职业目标表
CREATE TABLE career_targets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    name VARCHAR(100) NOT NULL COMMENT '职业目标名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '职业代码',
    description TEXT COMMENT '职业描述',
    category VARCHAR(50) COMMENT '职业分类',
    difficulty_level INT DEFAULT 1 COMMENT '整体难度等级1-5',
    estimated_months INT COMMENT '预估学习月数',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_code (code),
    INDEX idx_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职业目标表';

-- 2. 学习路径表（AI生成的思维导图结构）
CREATE TABLE learning_paths (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    career_target_id BIGINT UNSIGNED NOT NULL COMMENT '职业目标ID',
    name VARCHAR(200) NOT NULL COMMENT '路径名称',
    level_range VARCHAR(20) COMMENT '适用等级范围，如L1-L3',
    sequence_order INT COMMENT '学习顺序',
    parent_id BIGINT UNSIGNED COMMENT '父路径ID，用于构建树形结构',
    path_type ENUM('foundation', 'advanced', 'expert', 'specialization') COMMENT '路径类型',
    ai_generated_content JSON COMMENT 'AI生成的详细内容',
    prerequisites TEXT COMMENT '前置要求',
    learning_objectives TEXT COMMENT '学习目标',
    estimated_hours INT COMMENT '预估学习时长（小时）',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_career_target (career_target_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path_type (path_type),
    FOREIGN KEY (career_target_id) REFERENCES career_targets(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES learning_paths(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习路径表';

-- 3. 知识点体系表
CREATE TABLE knowledge_points (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    learning_path_id BIGINT UNSIGNED NOT NULL COMMENT '学习路径ID',
    name VARCHAR(200) NOT NULL COMMENT '知识点名称',
    code VARCHAR(100) UNIQUE COMMENT '知识点编码',
    description TEXT COMMENT '知识点描述',
    level_min INT DEFAULT 1 COMMENT '最低适用等级',
    level_max INT DEFAULT 9 COMMENT '最高适用等级',
    difficulty INT DEFAULT 1 COMMENT '难度系数1-5',
    importance INT DEFAULT 1 COMMENT '重要程度1-5',
    knowledge_type ENUM('concept', 'skill', 'practice', 'project') COMMENT '知识类型',
    prerequisites JSON COMMENT '前置知识点ID列表',
    content_outline TEXT COMMENT '内容大纲',
    learning_resources JSON COMMENT '学习资源链接',
    practice_suggestions TEXT COMMENT '练习建议',
    vector_embedding TEXT COMMENT '知识点向量表示',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_learning_path (learning_path_id),
    INDEX idx_level_range (level_min, level_max),
    INDEX idx_difficulty (difficulty),
    INDEX idx_knowledge_type (knowledge_type),
    FOREIGN KEY (learning_path_id) REFERENCES learning_paths(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点体系表';

-- 4. 题库管理表
CREATE TABLE question_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    knowledge_point_id BIGINT UNSIGNED NOT NULL COMMENT '知识点ID',
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    content TEXT NOT NULL COMMENT '题目内容',
    question_type ENUM('single', 'multiple', 'judge', 'fill', 'essay') NOT NULL COMMENT '题目类型',
    options JSON COMMENT '选项（选择题、判断题）',
    correct_answer TEXT NOT NULL COMMENT '正确答案',
    explanation TEXT COMMENT '答案解析',
    difficulty INT DEFAULT 1 COMMENT '难度等级1-5',
    tags JSON COMMENT '标签',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    correct_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '正确率',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_knowledge_point (knowledge_point_id),
    INDEX idx_question_type (question_type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_points(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库管理表';

-- 5. 学习内容管理表
CREATE TABLE learning_contents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    knowledge_point_id BIGINT UNSIGNED NOT NULL COMMENT '知识点ID',
    title VARCHAR(200) NOT NULL COMMENT '内容标题',
    content_type ENUM('text', 'video', 'audio', 'document', 'interactive') NOT NULL COMMENT '内容类型',
    content_url VARCHAR(500) COMMENT '内容链接',
    content_text LONGTEXT COMMENT '文本内容',
    duration_minutes INT COMMENT '内容时长（分钟）',
    difficulty INT DEFAULT 1 COMMENT '难度等级1-5',
    tags JSON COMMENT '标签',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_knowledge_point (knowledge_point_id),
    INDEX idx_content_type (content_type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_points(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习内容管理表';

-- 6. 系统配置表
CREATE TABLE system_configs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置描述',
    category VARCHAR(50) COMMENT '配置分类',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1是 0否',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 7. 管理员用户表
CREATE TABLE admin_users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(500) COMMENT '头像链接',
    role ENUM('super_admin', 'admin', 'operator', 'viewer') DEFAULT 'operator' COMMENT '角色',
    permissions JSON COMMENT '权限列表',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- 8. 操作日志表
CREATE TABLE operation_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    admin_user_id BIGINT UNSIGNED COMMENT '管理员ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time INT COMMENT '执行时间（毫秒）',
    status TINYINT DEFAULT 1 COMMENT '状态：1成功 0失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_admin_user (admin_user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status),
    FOREIGN KEY (admin_user_id) REFERENCES admin_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 9. 数据统计表
CREATE TABLE data_statistics (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    stat_key VARCHAR(100) NOT NULL COMMENT '统计键',
    stat_value BIGINT DEFAULT 0 COMMENT '统计值',
    metadata JSON COMMENT '元数据',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stat_date_type_key (stat_date, stat_type, stat_key),
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_type (stat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据统计表';

-- 插入初始数据
INSERT INTO career_targets (name, code, description, category, difficulty_level, estimated_months) VALUES
('Web工程师（业务系统）', 'WEB_ENGINEER', 'Web应用开发，包括前端、后端、数据库等全栈技术', 'Development', 3, 12),
('大数据平台开发工程师', 'BIGDATA_ENGINEER', '大数据处理、分布式系统、数据仓库等技术', 'BigData', 4, 18),
('中间件开发工程师', 'MIDDLEWARE_ENGINEER', '中间件原理、源码分析、系统架构设计', 'Infrastructure', 5, 24);

-- 插入默认管理员账号（密码：123456）
INSERT INTO admin_users (username, password, real_name, role) VALUES
('admin', '123456', '系统管理员', 'super_admin');

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, config_type, description, category) VALUES
('system.name', '通学万卷', 'string', '系统名称', 'basic'),
('system.version', '1.0.0', 'string', '系统版本', 'basic'),
('ai.model.default', 'gpt-3.5-turbo', 'string', '默认AI模型', 'ai'),
('assessment.default_difficulty', '3', 'number', '默认测评难度', 'assessment'),
('learning.daily_minutes', '60', 'number', '默认每日学习时长（分钟）', 'learning');

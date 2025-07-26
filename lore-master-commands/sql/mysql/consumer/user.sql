-- =============================================
-- 通学万卷 - C端用户数据库设计
-- 用户ID格式: LM-YYYY-XXXX-XXXX (防遍历设计)
-- =============================================

CREATE DATABASE IF NOT EXISTS lore_consumer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lore_consumer;

-- =============================================
-- 1. 用户主表 (users)
-- =============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键（内部使用）',
    user_id VARCHAR(20) NOT NULL UNIQUE COMMENT '用户唯一ID（格式：LM-YYYY-XXXX-XXXX，防遍历）',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    real_name VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像链接',
    gender TINYINT DEFAULT 0 COMMENT '性别：1男 2女 0未知',
    birth_date DATE DEFAULT NULL COMMENT '出生日期',
    bio VARCHAR(500) DEFAULT NULL COMMENT '个人简介',

    -- 学习相关字段
    current_level INT DEFAULT 1 COMMENT '当前等级',
    total_score INT DEFAULT 0 COMMENT '总积分',
    study_days INT DEFAULT 0 COMMENT '学习天数',
    continuous_days INT DEFAULT 0 COMMENT '连续学习天数',

    -- 登录相关字段
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    login_count INT DEFAULT 0 COMMENT '登录次数',

    -- 状态字段
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用 2待激活',
    is_verified TINYINT DEFAULT 0 COMMENT '是否已验证：1已验证 0未验证',

    -- 扩展字段
    extra_info JSON DEFAULT NULL COMMENT '扩展信息（JSON格式）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_nickname (nickname),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- =============================================
-- 2. 用户认证方式表 (user_auth_methods)
-- =============================================
CREATE TABLE user_auth_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id VARCHAR(20) NOT NULL COMMENT '用户ID（关联users.user_id）',
    auth_type VARCHAR(20) NOT NULL COMMENT '认证类型：username、email、phone、qq、wechat、weibo等',
    auth_key VARCHAR(255) NOT NULL COMMENT '认证标识（用户名、邮箱、手机号、第三方openid等）',
    auth_secret VARCHAR(255) DEFAULT NULL COMMENT '认证密钥（密码hash、token等，第三方登录可为空）',

    -- 第三方登录相关字段
    third_party_id VARCHAR(255) DEFAULT NULL COMMENT '第三方平台用户ID',
    union_id VARCHAR(255) DEFAULT NULL COMMENT '第三方平台UnionID（微信等）',
    third_party_nickname VARCHAR(100) DEFAULT NULL COMMENT '第三方平台昵称',
    third_party_avatar VARCHAR(500) DEFAULT NULL COMMENT '第三方平台头像',

    -- 验证相关字段
    is_verified TINYINT DEFAULT 0 COMMENT '是否已验证：1已验证 0未验证',
    verified_time DATETIME DEFAULT NULL COMMENT '验证时间',

    -- 状态字段
    is_primary TINYINT DEFAULT 0 COMMENT '是否为主要登录方式：1是 0否',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    last_used_time DATETIME DEFAULT NULL COMMENT '最后使用时间',

    -- 扩展字段
    extra_info JSON DEFAULT NULL COMMENT '扩展信息（存储第三方平台返回的其他信息）',

   
    -- 索引
    UNIQUE KEY uk_auth_type_key (auth_type, auth_key) COMMENT '认证类型和标识唯一',
    INDEX idx_user_id (user_id),
    INDEX idx_auth_type (auth_type),
    INDEX idx_third_party_id (third_party_id),
    INDEX idx_union_id (union_id),
    INDEX idx_status (status),

    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证方式表';

-- =============================================
-- 3. 用户ID生成序列表 (user_id_sequence)
-- =============================================
CREATE TABLE user_id_sequence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    sequence_name VARCHAR(50) NOT NULL UNIQUE COMMENT '序列名称',
    current_value BIGINT NOT NULL DEFAULT 0 COMMENT '当前值',
    increment_step INT NOT NULL DEFAULT 1 COMMENT '递增步长',
    max_value BIGINT DEFAULT NULL COMMENT '最大值',
    cycle_flag TINYINT DEFAULT 0 COMMENT '是否循环：1是 0否'

    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户ID生成序列表';

-- 初始化用户ID序列
INSERT INTO user_id_sequence (sequence_name, current_value, increment_step, max_value)
VALUES ('user_id_seq', 100000000, 1, 999999999);

-- =============================================
-- 4. 用户登录日志表 (user_login_logs)
-- =============================================
CREATE TABLE user_login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
    auth_type VARCHAR(20) NOT NULL COMMENT '登录方式',
    login_ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    login_location VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    device_type VARCHAR(20) DEFAULT NULL COMMENT '设备类型：web、mobile、tablet',
    device_id VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    login_status TINYINT DEFAULT 1 COMMENT '登录状态：1成功 0失败',
    failure_reason VARCHAR(200) DEFAULT NULL COMMENT '失败原因',

    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_login_ip (login_ip),
    INDEX idx_login_status (login_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- =============================================
-- 5. 用户绑定关系表 (user_bindings)
-- =============================================
CREATE TABLE user_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    main_user_id VARCHAR(20) NOT NULL COMMENT '主用户ID',
    bind_user_id VARCHAR(20) NOT NULL COMMENT '绑定的用户ID',
    bind_type VARCHAR(20) NOT NULL COMMENT '绑定类型：merge（合并）、link（关联）',
    bind_reason VARCHAR(100) DEFAULT NULL COMMENT '绑定原因',


    -- 索引
    UNIQUE KEY uk_main_bind (main_user_id, bind_user_id),
    INDEX idx_bind_user_id (bind_user_id),
    INDEX idx_bind_type (bind_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户绑定关系表（处理重复注册问题）';

-- =============================================
-- 6. 验证码表 (verification_codes)
-- =============================================
CREATE TABLE verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    code_key VARCHAR(255) NOT NULL COMMENT '验证码标识（邮箱、手机号等）',
    code_type VARCHAR(20) NOT NULL COMMENT '验证码类型：register、login、reset_password、bind',
    code_value VARCHAR(10) NOT NULL COMMENT '验证码值',

    -- 验证相关
    attempt_count INT DEFAULT 0 COMMENT '尝试次数',
    max_attempts INT DEFAULT 5 COMMENT '最大尝试次数',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用：1已使用 0未使用',

    -- 时间相关
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    used_time DATETIME DEFAULT NULL COMMENT '使用时间',

    -- 索引
    UNIQUE KEY uk_key_type (code_key, code_type),
    INDEX idx_expire_time (expire_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- =============================================
-- 7. 用户会话表 (user_sessions)
-- =============================================
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',

    -- 时间字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    session_id VARCHAR(128) NOT NULL UNIQUE COMMENT '会话ID',
    user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
    access_token VARCHAR(500) NOT NULL COMMENT '访问令牌',
    refresh_token VARCHAR(500) DEFAULT NULL COMMENT '刷新令牌',

    -- 设备信息
    device_type VARCHAR(20) DEFAULT NULL COMMENT '设备类型',
    device_id VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
    client_ip VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '用户代理',

    -- 时间相关
    access_expire_time DATETIME NOT NULL COMMENT '访问令牌过期时间',
    refresh_expire_time DATETIME DEFAULT NULL COMMENT '刷新令牌过期时间',
    last_active_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_access_expire (access_expire_time),
    INDEX idx_refresh_expire (refresh_expire_time),
    INDEX idx_last_active (last_active_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- =============================================
-- 8. 示例数据插入
-- =============================================

-- 示例用户1：多种登录方式
INSERT INTO users (user_id, nickname, avatar_url, gender, current_level, total_score, study_days, status, is_verified)
VALUES ('LM-2025-A8B9-C3D7', '学霸小明', 'https://example.com/avatar1.jpg', 1, 5, 1250, 30, 1, 1);

-- 用户1的认证方式
INSERT INTO user_auth_methods (user_id, auth_type, auth_key, auth_secret, is_verified, is_primary, status) VALUES
('LM-2025-A8B9-C3D7', 'username', 'xiaoming', '$2a$10$encrypted_password_hash', 1, 1, 1),
('LM-2025-A8B9-C3D7', 'email', 'xiaoming@example.com', NULL, 1, 0, 1),
('LM-2025-A8B9-C3D7', 'qq', 'qq_openid_123456', NULL, 1, 0, 1);

-- 示例用户2：邮箱注册
INSERT INTO users (user_id, nickname, avatar_url, gender, current_level, total_score, study_days, status, is_verified)
VALUES ('LM-2025-F1G2-H5J8', '学神小红', 'https://example.com/avatar2.jpg', 2, 3, 800, 20, 1, 1);

INSERT INTO user_auth_methods (user_id, auth_type, auth_key, auth_secret, is_verified, is_primary, status) VALUES
('LM-2025-F1G2-H5J8', 'email', 'xiaohong@example.com', NULL, 1, 1, 1),
('LM-2025-F1G2-H5J8', 'wechat', 'wechat_openid_789012', NULL, 1, 0, 1);

-- =============================================
-- 9. 常用查询示例
-- =============================================

-- 查询用户的所有登录方式
-- SELECT * FROM user_auth_methods WHERE user_id = 'LM-2025-A8B9-C3D7' AND status = 1;

-- 检查某个认证标识是否已存在
-- SELECT COUNT(*) FROM user_auth_methods WHERE auth_type = 'email' AND auth_key = 'test@example.com' AND status = 1;

-- 查询用户完整信息（通过JOIN查询）
-- SELECT u.*, GROUP_CONCAT(CONCAT(uam.auth_type, ':', uam.auth_key) SEPARATOR ';') as auth_methods
-- FROM users u
-- LEFT JOIN user_auth_methods uam ON u.user_id = uam.user_id AND uam.status = 1
-- WHERE u.user_id = 'LM-2025-A8B9-C3D7' AND u.status = 1
-- GROUP BY u.user_id;

-- 获取下一个用户ID
-- UPDATE user_id_sequence SET current_value = current_value + increment_step WHERE sequence_name = 'user_id_seq';
-- SELECT current_value FROM user_id_sequence WHERE sequence_name = 'user_id_seq';

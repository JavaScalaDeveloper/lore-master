-- =====================================================
-- 通学万卷 - 用户系统数据库表结构
-- 创建时间: 2025-07-26
-- 描述: 支持多种登录方式的可扩展用户系统
-- =====================================================

-- 1. 用户基础信息表（核心用户表）
DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键（内部使用）',
    user_id         VARCHAR(32)     NOT NULL COMMENT '用户唯一ID（对外展示，类似QQ号）',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    
    -- 基础信息
    nickname        VARCHAR(100) COMMENT '昵称',
    real_name       VARCHAR(100) COMMENT '真实姓名',
    avatar_url      VARCHAR(500) COMMENT '头像链接',
    gender          TINYINT COMMENT '性别：1男 2女 0未知',
    birth_date      DATE COMMENT '出生日期',
    bio             VARCHAR(500) COMMENT '个人简介',
    
    -- 学习信息
    current_level   INT DEFAULT 1 COMMENT '当前等级',
    total_score     INT DEFAULT 0 COMMENT '总积分',
    study_days      INT DEFAULT 0 COMMENT '学习天数',
    
    -- 系统信息
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) COMMENT '最后登录IP',
    login_count     INT DEFAULT 0 COMMENT '登录次数',
    status          TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用 2待激活',
    is_verified     TINYINT DEFAULT 0 COMMENT '是否已验证：1已验证 0未验证',
    
    -- 扩展字段
    extra_info      JSON COMMENT '扩展信息（JSON格式）',
    remark          VARCHAR(500) COMMENT '备注',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_current_level (current_level)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户基础信息表';

-- 2. 用户登录方式表（支持多种登录方式）
DROP TABLE IF EXISTS user_auth_methods;
CREATE TABLE user_auth_methods
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id         VARCHAR(32)     NOT NULL COMMENT '用户ID',
    
    -- 登录方式信息
    auth_type       VARCHAR(20)     NOT NULL COMMENT '认证类型：username、email、phone、wechat、qq、github等',
    auth_key        VARCHAR(255)    NOT NULL COMMENT '认证标识（用户名、邮箱、手机号、第三方openid等）',
    auth_secret     VARCHAR(255) COMMENT '认证密钥（密码hash、token等，第三方登录可为空）',
    
    -- 第三方登录信息
    third_party_id  VARCHAR(255) COMMENT '第三方平台用户ID',
    union_id        VARCHAR(255) COMMENT '第三方平台UnionID（微信等）',
    
    -- 验证信息
    is_verified     TINYINT DEFAULT 0 COMMENT '是否已验证：1已验证 0未验证',
    verified_time   DATETIME COMMENT '验证时间',
    
    -- 状态信息
    is_primary      TINYINT DEFAULT 0 COMMENT '是否为主要登录方式：1是 0否',
    status          TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    last_used_time  DATETIME COMMENT '最后使用时间',
    
    -- 扩展字段
    extra_info      JSON COMMENT '扩展信息（存储第三方平台返回的其他信息）',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_type_key (auth_type, auth_key),
    INDEX idx_user_id (user_id),
    INDEX idx_auth_type (auth_type),
    INDEX idx_third_party_id (third_party_id),
    INDEX idx_union_id (union_id),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户登录方式表';

-- 3. 用户会话表（登录会话管理）
DROP TABLE IF EXISTS user_sessions;
CREATE TABLE user_sessions
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id         VARCHAR(32)     NOT NULL COMMENT '用户ID',
    
    -- 会话信息
    session_id      VARCHAR(64)     NOT NULL COMMENT '会话ID',
    auth_type       VARCHAR(20)     NOT NULL COMMENT '登录方式',
    device_type     VARCHAR(20) COMMENT '设备类型：web、ios、android、wechat等',
    device_id       VARCHAR(255) COMMENT '设备标识',
    
    -- 网络信息
    login_ip        VARCHAR(50) COMMENT '登录IP',
    user_agent      TEXT COMMENT '用户代理',
    location        VARCHAR(255) COMMENT '登录地点',
    
    -- 时间信息
    login_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    last_active_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    expire_time     DATETIME COMMENT '过期时间',
    
    -- 状态信息
    status          TINYINT DEFAULT 1 COMMENT '状态：1活跃 0已过期 -1已注销',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户会话表';

-- 4. 用户操作日志表
DROP TABLE IF EXISTS user_operation_logs;
CREATE TABLE user_operation_logs
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    user_id         VARCHAR(32) COMMENT '用户ID（可为空，如注册失败）',
    
    -- 操作信息
    operation_type  VARCHAR(50)     NOT NULL COMMENT '操作类型：login、logout、register、bind_auth、unbind_auth等',
    operation_desc  VARCHAR(255) COMMENT '操作描述',
    
    -- 请求信息
    request_ip      VARCHAR(50) COMMENT '请求IP',
    user_agent      TEXT COMMENT '用户代理',
    device_type     VARCHAR(20) COMMENT '设备类型',
    
    -- 结果信息
    result_status   TINYINT DEFAULT 1 COMMENT '操作结果：1成功 0失败',
    error_message   VARCHAR(500) COMMENT '错误信息',
    
    -- 扩展字段
    extra_data      JSON COMMENT '扩展数据',
    
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time),
    INDEX idx_request_ip (request_ip)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户操作日志表';

-- =====================================================
-- 用户ID生成函数
-- =====================================================
DELIMITER //
DROP FUNCTION IF EXISTS generate_user_id//
CREATE FUNCTION generate_user_id() RETURNS VARCHAR(32)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE new_user_id VARCHAR(32);
    DECLARE id_exists INT DEFAULT 1;
    
    WHILE id_exists > 0 DO
        -- 生成策略：时间戳 + 随机数 + 校验位
        SET new_user_id = CONCAT(
            -- 年份后两位 + 月日时分秒 (10位)
            DATE_FORMAT(NOW(), '%y%m%d%H%i%s'),
            -- 3位随机数
            LPAD(FLOOR(RAND() * 1000), 3, '0'),
            -- 2位校验位
            LPAD(MOD(CAST(SUBSTRING(DATE_FORMAT(NOW(), '%y%m%d%H%i%s'), -6) AS UNSIGNED) + FLOOR(RAND() * 1000), 100), 2, '0')
        );
        
        -- 检查是否已存在
        SELECT COUNT(*) INTO id_exists FROM users WHERE user_id = new_user_id;
    END WHILE;
    
    RETURN new_user_id;
END //
DELIMITER ;

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入测试用户
INSERT INTO users (user_id, nickname, real_name, gender, current_level, total_score, status, is_verified) 
VALUES 
(generate_user_id(), '测试用户1', '张三', 1, 1, 100, 1, 1),
(generate_user_id(), '测试用户2', '李四', 2, 2, 250, 1, 1),
(generate_user_id(), '测试用户3', '王五', 1, 1, 50, 1, 0);

-- 为测试用户添加登录方式
INSERT INTO user_auth_methods (user_id, auth_type, auth_key, auth_secret, is_primary, is_verified)
SELECT user_id, 'username', CONCAT('user', SUBSTRING(user_id, -3)), '$2a$10$example_hash', 1, 1
FROM users WHERE nickname LIKE '测试用户%';

-- 插入操作日志示例
INSERT INTO user_operation_logs (user_id, operation_type, operation_desc, request_ip, result_status)
SELECT user_id, 'register', '用户注册', '127.0.0.1', 1
FROM users WHERE nickname LIKE '测试用户%';

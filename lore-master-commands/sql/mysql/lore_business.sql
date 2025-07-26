-- =============================================
-- C端业务数据库设计 (lore_business)
-- 主要用于：用户数据、学习记录、测评结果、个性化推荐
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lore_business DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lore_business;

-- 1. 用户基础信息表
CREATE TABLE users
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    username        VARCHAR(50)     NOT NULL COMMENT '用户名',
    password        VARCHAR(255)    NOT NULL COMMENT '密码',
    nickname        VARCHAR(100) COMMENT '昵称',
    real_name       VARCHAR(100) COMMENT '真实姓名',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    avatar_url      VARCHAR(500) COMMENT '头像链接',
    gender          TINYINT COMMENT '性别：1男 2女 0未知',
    birth_date      DATE COMMENT '出生日期',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) COMMENT '最后登录IP',
    login_count     INT                      DEFAULT 0 COMMENT '登录次数',
    status          TINYINT                  DEFAULT 1 COMMENT '状态：1正常 0禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户基础信息表';


-- 2. 用户学习档案表
CREATE TABLE user_learning_profiles
(
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time            DATETIME        NOT NULL        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time            DATETIME        NOT NULL        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id                BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    career_target_id       BIGINT UNSIGNED NOT NULL COMMENT '职业目标ID',
    current_level          INT                             DEFAULT 1 COMMENT '当前等级L1-L9',
    overall_progress       DECIMAL(5, 2)                   DEFAULT 0.00 COMMENT '整体进度百分比',
    learning_style         ENUM ('visual', 'auditory', 'kinesthetic', 'mixed') COMMENT '学习风格',
    learning_pace          ENUM ('slow', 'normal', 'fast') DEFAULT 'normal' COMMENT '学习节奏',
    strengths              JSON COMMENT '优势领域',
    weaknesses             JSON COMMENT '薄弱环节',
    learning_goals         TEXT COMMENT '学习目标',
    target_completion_date DATE COMMENT '目标完成日期',
    total_study_minutes    INT                             DEFAULT 0 COMMENT '总学习时长（分钟）',
    continuous_days        INT                             DEFAULT 0 COMMENT '连续学习天数',
    max_continuous_days    INT                             DEFAULT 0 COMMENT '最大连续学习天数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_career (user_id, career_target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_career_target_id (career_target_id),
    INDEX idx_current_level (current_level),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户学习档案表';

-- 3. 用户知识点掌握情况表
CREATE TABLE user_knowledge_mastery
(
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    knowledge_point_id    BIGINT UNSIGNED NOT NULL COMMENT '知识点ID',
    mastery_level         DECIMAL(3, 2)            DEFAULT 0.00 COMMENT '掌握程度0-1',
    confidence_score      DECIMAL(3, 2)            DEFAULT 0.00 COMMENT '置信度0-1',
    last_assessment_score INT COMMENT '最近测评分数',
    assessment_count      INT                      DEFAULT 0 COMMENT '测评次数',
    study_time_minutes    INT                      DEFAULT 0 COMMENT '学习时长（分钟）',
    first_learned_at      DATETIME COMMENT '首次学习时间',
    last_reviewed_at      DATETIME COMMENT '最近复习时间',
    next_review_at        DATETIME COMMENT '下次复习时间',
    is_weak_point         TINYINT                  DEFAULT 0 COMMENT '是否为薄弱点：1是 0否',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_knowledge (user_id, knowledge_point_id),
    INDEX idx_user_id (user_id),
    INDEX idx_knowledge_point_id (knowledge_point_id),
    INDEX idx_mastery_level (mastery_level),
    INDEX idx_is_weak_point (user_id, is_weak_point),
    INDEX idx_next_review (next_review_at),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户知识点掌握情况表';

-- 4. AI测评记录表
CREATE TABLE ai_assessments
(
    id                          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id                     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    assessment_type             ENUM ('level_test', 'knowledge_scan', 'weakness_check', 'comprehensive') COMMENT '测评类型',
    target_level                INT COMMENT '目标等级',
    knowledge_point_ids         JSON COMMENT '涉及的知识点ID列表',
    questions_data              JSON COMMENT '题目数据',
    user_answers                JSON COMMENT '用户答案',
    ai_analysis                 JSON COMMENT 'AI分析结果',
    overall_score               DECIMAL(5, 2) COMMENT '总体得分',
    detailed_scores             JSON COMMENT '详细得分情况',
    identified_gaps             JSON COMMENT '识别出的知识盲点',
    recommendations             JSON COMMENT 'AI推荐建议',
    assessment_duration_seconds INT COMMENT '测评耗时（秒）',
    ai_model_version            VARCHAR(50) COMMENT 'AI模型版本',

    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_assessment_type (assessment_type),
    INDEX idx_target_level (target_level),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI测评记录表';

-- 5. 个性化学习计划表
CREATE TABLE personalized_learning_plans
(
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time          DATETIME        NOT NULL                            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time          DATETIME        NOT NULL                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id              BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    career_target_id     BIGINT UNSIGNED NOT NULL COMMENT '职业目标ID',
    plan_name            VARCHAR(200) COMMENT '计划名称',
    current_phase        ENUM ('foundation', 'advanced', 'expert', 'specialization') COMMENT '当前阶段',
    target_level         INT COMMENT '目标等级',
    plan_data            JSON COMMENT '详细计划数据',
    daily_study_minutes  INT                                                 DEFAULT 60 COMMENT '每日学习时长（分钟）',
    weekly_goals         JSON COMMENT '周目标',
    monthly_milestones   JSON COMMENT '月度里程碑',
    adaptive_adjustments JSON COMMENT '自适应调整记录',
    progress_tracking    JSON COMMENT '进度跟踪数据',
    ai_optimization_log  JSON COMMENT 'AI优化日志',
    plan_status          ENUM ('active', 'paused', 'completed', 'abandoned') DEFAULT 'active',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_career_target_id (career_target_id),
    INDEX idx_plan_status (plan_status),
    INDEX idx_current_phase (current_phase),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='个性化学习计划表';

-- 6. 学习行为分析表
CREATE TABLE learning_behavior_analytics
(
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id            BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    session_id         VARCHAR(100) COMMENT '学习会话ID',
    behavior_type      ENUM ('study', 'practice', 'assessment', 'review', 'search') COMMENT '行为类型',
    knowledge_point_id BIGINT UNSIGNED COMMENT '相关知识点ID',
    content_type       ENUM ('text', 'video', 'audio', 'interactive', 'quiz') COMMENT '内容类型',
    engagement_score   DECIMAL(3, 2) COMMENT '参与度得分0-1',
    time_spent_seconds INT COMMENT '花费时间（秒）',
    interaction_data   JSON COMMENT '交互数据',
    learning_outcome   ENUM ('mastered', 'improved', 'struggled', 'abandoned') COMMENT '学习结果',
    device_type        VARCHAR(50) COMMENT '设备类型',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_behavior_type (behavior_type),
    INDEX idx_knowledge_point_id (knowledge_point_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学习行为分析表';

-- 7. AI推荐记录表
CREATE TABLE ai_recommendations
(
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    recommendation_type ENUM ('next_topic', 'review_content', 'practice_exercise', 'learning_resource') COMMENT '推荐类型',
    recommended_content JSON COMMENT '推荐内容',
    reasoning           TEXT COMMENT '推荐理由',
    confidence_score    DECIMAL(3, 2) COMMENT '推荐置信度',
    user_feedback       ENUM ('accepted', 'rejected', 'ignored') COMMENT '用户反馈',
    effectiveness_score DECIMAL(3, 2) COMMENT '效果评分',
    ai_model_version    VARCHAR(50) COMMENT 'AI模型版本',
    feedback_at         DATETIME COMMENT '反馈时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_recommendation_type (recommendation_type),
    INDEX idx_user_feedback (user_feedback),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI推荐记录表';

-- 8. 向量数据库映射表
CREATE TABLE vector_embeddings
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    entity_type      ENUM ('user_profile', 'knowledge_point', 'learning_content', 'assessment_result') COMMENT '实体类型',
    entity_id        BIGINT UNSIGNED NOT NULL COMMENT '实体ID',
    vector_id        VARCHAR(100) COMMENT '向量数据库中的ID',
    embedding_model  VARCHAR(50) COMMENT '嵌入模型名称',
    vector_dimension INT COMMENT '向量维度',
    metadata         JSON COMMENT '元数据',
    PRIMARY KEY (id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_vector_id (vector_id),
    INDEX idx_embedding_model (embedding_model)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='向量数据库映射表';

-- 9. 用户学习记录表
CREATE TABLE user_learning_records
(
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    knowledge_point_id  BIGINT UNSIGNED NOT NULL COMMENT '知识点ID',
    learning_content_id BIGINT UNSIGNED COMMENT '学习内容ID',
    learning_type       ENUM ('study', 'practice', 'review', 'assessment') COMMENT '学习类型',
    start_time          DATETIME COMMENT '开始时间',
    end_time            DATETIME COMMENT '结束时间',
    duration_seconds    INT COMMENT '学习时长（秒）',
    progress_before     DECIMAL(3, 2) COMMENT '学习前进度',
    progress_after      DECIMAL(3, 2) COMMENT '学习后进度',
    score               INT COMMENT '得分',
    is_completed        TINYINT                  DEFAULT 0 COMMENT '是否完成：1是 0否',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_knowledge_point_id (knowledge_point_id),
    INDEX idx_learning_type (learning_type),
    INDEX idx_start_time (start_time),
    INDEX idx_is_completed (is_completed),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户学习记录表';

-- 10. 用户成就表
CREATE TABLE user_achievements
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    user_id          BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    achievement_type ENUM ('level_up', 'streak', 'mastery', 'speed', 'comprehensive') COMMENT '成就类型',
    achievement_name VARCHAR(100)    NOT NULL COMMENT '成就名称',
    achievement_desc VARCHAR(500) COMMENT '成就描述',
    achievement_icon VARCHAR(200) COMMENT '成就图标',
    achievement_data JSON COMMENT '成就数据',
    earned_at        DATETIME COMMENT '获得时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_achievement_type (achievement_type),
    INDEX idx_earned_at (earned_at),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户成就表';

-- 插入测试用户数据
INSERT INTO users (username, password, nickname, email, status)
VALUES ('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgBZdO5jO8S5V8jDgmKYeQ3Eq', '测试用户1', 'test1@example.com',
        1),
       ('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgBZdO5jO8S5V8jDgmKYeQ3Eq', '测试用户2', 'test2@example.com',
        1);

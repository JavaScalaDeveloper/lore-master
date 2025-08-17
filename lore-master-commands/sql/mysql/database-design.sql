-- =============================================
-- 个性化学习路径系统数据库设计
-- =============================================

-- 1. 职业目标表
CREATE TABLE career_targets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '职业目标名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '职业代码',
    description TEXT COMMENT '职业描述',
    category VARCHAR(50) COMMENT '职业分类',
    difficulty_level INT DEFAULT 1 COMMENT '整体难度等级1-5',
    estimated_months INT COMMENT '预估学习月数',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 学习路径表（AI生成的思维导图结构）
CREATE TABLE learning_paths (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    career_target_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT '路径名称',
    level_range VARCHAR(20) COMMENT '适用等级范围，如L1-L3',
    sequence_order INT COMMENT '学习顺序',
    parent_id BIGINT COMMENT '父路径ID，用于构建树形结构',
    path_type ENUM('foundation', 'advanced', 'expert', 'specialization') COMMENT '路径类型',
    ai_generated_content JSON COMMENT 'AI生成的详细内容',
    prerequisites TEXT COMMENT '前置要求',
    learning_objectives TEXT COMMENT '学习目标',
    estimated_hours INT COMMENT '预估学习时长（小时）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (career_target_id) REFERENCES career_targets(id),
    FOREIGN KEY (parent_id) REFERENCES learning_paths(id)
);

-- 3. 知识点体系表
CREATE TABLE knowledge_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learning_path_id BIGINT NOT NULL,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (learning_path_id) REFERENCES learning_paths(id)
);

-- 4. 用户学习档案表
CREATE TABLE user_learning_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    career_target_id BIGINT NOT NULL,
    current_level INT DEFAULT 1 COMMENT '当前等级L1-L9',
    overall_progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '整体进度百分比',
    learning_style ENUM('visual', 'auditory', 'kinesthetic', 'mixed') COMMENT '学习风格',
    learning_pace ENUM('slow', 'normal', 'fast') DEFAULT 'normal' COMMENT '学习节奏',
    strengths JSON COMMENT '优势领域',
    weaknesses JSON COMMENT '薄弱环节',
    learning_goals TEXT COMMENT '学习目标',
    target_completion_date DATE COMMENT '目标完成日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (career_target_id) REFERENCES career_targets(id)
);

-- 5. 用户知识点掌握情况表
CREATE TABLE user_knowledge_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    mastery_level DECIMAL(3,2) DEFAULT 0.00 COMMENT '掌握程度0-1',
    confidence_score DECIMAL(3,2) DEFAULT 0.00 COMMENT '置信度0-1',
    last_assessment_score INT COMMENT '最近测评分数',
    assessment_count INT DEFAULT 0 COMMENT '测评次数',
    study_time_minutes INT DEFAULT 0 COMMENT '学习时长（分钟）',
    first_learned_at TIMESTAMP NULL COMMENT '首次学习时间',
    last_reviewed_at TIMESTAMP NULL COMMENT '最近复习时间',
    next_review_at TIMESTAMP NULL COMMENT '下次复习时间',
    is_weak_point BOOLEAN DEFAULT FALSE COMMENT '是否为薄弱点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_knowledge (user_id, knowledge_point_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_points(id)
);

-- 6. AI测评记录表
CREATE TABLE ai_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    assessment_type ENUM('level_test', 'knowledge_scan', 'weakness_check', 'comprehensive') COMMENT '测评类型',
    target_level INT COMMENT '目标等级',
    knowledge_point_ids JSON COMMENT '涉及的知识点ID列表',
    questions_data JSON COMMENT '题目数据',
    user_answers JSON COMMENT '用户答案',
    ai_analysis JSON COMMENT 'AI分析结果',
    overall_score DECIMAL(5,2) COMMENT '总体得分',
    detailed_scores JSON COMMENT '详细得分情况',
    identified_gaps JSON COMMENT '识别出的知识盲点',
    recommendations JSON COMMENT 'AI推荐建议',
    assessment_duration_seconds INT COMMENT '测评耗时（秒）',
    ai_model_version VARCHAR(50) COMMENT 'AI模型版本',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. 个性化学习计划表
CREATE TABLE personalized_learning_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    career_target_id BIGINT NOT NULL,
    plan_name VARCHAR(200) COMMENT '计划名称',
    current_phase ENUM('foundation', 'advanced', 'expert', 'specialization') COMMENT '当前阶段',
    target_level INT COMMENT '目标等级',
    plan_data JSON COMMENT '详细计划数据',
    daily_study_minutes INT DEFAULT 60 COMMENT '每日学习时长（分钟）',
    weekly_goals JSON COMMENT '周目标',
    monthly_milestones JSON COMMENT '月度里程碑',
    adaptive_adjustments JSON COMMENT '自适应调整记录',
    progress_tracking JSON COMMENT '进度跟踪数据',
    ai_optimization_log JSON COMMENT 'AI优化日志',
    plan_status ENUM('active', 'paused', 'completed', 'abandoned') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (career_target_id) REFERENCES career_targets(id)
);

-- 8. 学习行为分析表
CREATE TABLE learning_behavior_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100) COMMENT '学习会话ID',
    behavior_type ENUM('study', 'practice', 'assessment', 'review', 'search') COMMENT '行为类型',
    knowledge_point_id BIGINT COMMENT '相关知识点',
    content_type ENUM('text', 'video', 'audio', 'interactive', 'quiz') COMMENT '内容类型',
    engagement_score DECIMAL(3,2) COMMENT '参与度得分0-1',
    time_spent_seconds INT COMMENT '花费时间（秒）',
    interaction_data JSON COMMENT '交互数据',
    learning_outcome ENUM('mastered', 'improved', 'struggled', 'abandoned') COMMENT '学习结果',
    device_type VARCHAR(50) COMMENT '设备类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_points(id)
);

-- 9. AI推荐记录表
CREATE TABLE ai_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recommendation_type ENUM('next_topic', 'review_content', 'practice_exercise', 'learning_resource') COMMENT '推荐类型',
    recommended_content JSON COMMENT '推荐内容',
    reasoning TEXT COMMENT '推荐理由',
    confidence_score DECIMAL(3,2) COMMENT '推荐置信度',
    user_feedback ENUM('accepted', 'rejected', 'ignored') COMMENT '用户反馈',
    effectiveness_score DECIMAL(3,2) COMMENT '效果评分',
    ai_model_version VARCHAR(50) COMMENT 'AI模型版本',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    feedback_at TIMESTAMP NULL COMMENT '反馈时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 10. 向量数据库映射表（用于与向量数据库的关联）
CREATE TABLE vector_embeddings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('user_profile', 'knowledge_point', 'learning_content', 'assessment_result') COMMENT '实体类型',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    vector_id VARCHAR(100) COMMENT '向量数据库中的ID',
    embedding_model VARCHAR(50) COMMENT '嵌入模型名称',
    vector_dimension INT COMMENT '向量维度',
    metadata JSON COMMENT '元数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_vector_id (vector_id)
);

-- 创建索引优化查询性能
CREATE INDEX idx_user_learning_profiles_user_career ON user_learning_profiles(user_id, career_target_id);
CREATE INDEX idx_user_knowledge_mastery_user ON user_knowledge_mastery(user_id);
CREATE INDEX idx_user_knowledge_mastery_weak ON user_knowledge_mastery(user_id, is_weak_point);
CREATE INDEX idx_ai_assessments_user_type ON ai_assessments(user_id, assessment_type);
CREATE INDEX idx_learning_behavior_user_time ON learning_behavior_analytics(user_id, created_at);
CREATE INDEX idx_knowledge_points_path_level ON knowledge_points(learning_path_id, level_min, level_max);

-- 插入示例数据
INSERT INTO career_targets (name, code, description, category, difficulty_level, estimated_months) VALUES
('Web工程师（业务系统）', 'WEB_ENGINEER', 'Web应用开发，包括前端、后端、数据库等全栈技术', 'Development', 3, 12),
('大数据平台开发工程师', 'BIGDATA_ENGINEER', '大数据处理、分布式系统、数据仓库等技术', 'BigData', 4, 18),
('中间件开发工程师', 'MIDDLEWARE_ENGINEER', '中间件原理、源码分析、系统架构设计', 'Infrastructure', 5, 24);

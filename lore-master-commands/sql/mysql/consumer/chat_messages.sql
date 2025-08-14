CREATE TABLE consumer_chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL UNIQUE COMMENT '消息唯一标识',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID（用户可以有多个对话）',
    role VARCHAR(32) NOT NULL COMMENT '消息角色',
    content TEXT NOT NULL COMMENT '消息内容',
    model_name VARCHAR(50) COMMENT 'AI模型名称',
    token_count INT COMMENT 'Token使用量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_session_time (session_id, create_time),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '聊天消息表';
-- 用户课程学习记录表
-- 用于记录用户的课程学习行为和进度
-- 创建时间: 2025-01-21

DROP TABLE IF EXISTS `consumer_user_course_learning_records`;

CREATE TABLE `consumer_user_course_learning_records`
(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`           VARCHAR(50) NOT NULL COMMENT '用户ID',
    `course_code`       VARCHAR(64) NOT NULL COMMENT '课程编码',
    `learning_duration` INT      DEFAULT 0 COMMENT '学习时长（秒）',
    `progress_percent`  INT      DEFAULT 0 COMMENT '学习进度百分比（0-100）',
    `is_completed`      TINYINT  DEFAULT 0 COMMENT '是否完成：1-是，0-否',
    `learning_date`     DATE        NOT NULL COMMENT '学习日期',
    `created_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course_date` (`user_id`, `course_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_course_code` (`course_code`),
    KEY `idx_learning_date` (`learning_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户课程学习记录表';

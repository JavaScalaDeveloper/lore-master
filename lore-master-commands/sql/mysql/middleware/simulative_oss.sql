CREATE DATABASE IF NOT EXISTS lore_middleware DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use lore_middleware;

-- 文件存储表
CREATE TABLE `file_storage`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id`          VARCHAR(64)  NOT NULL COMMENT '文件唯一标识',
    `original_name`    VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name`        VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path`        VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size`        BIGINT       NOT NULL COMMENT '文件大小(字节)',
    `file_type`        VARCHAR(100) NOT NULL COMMENT '文件类型(MIME类型)',
    `file_extension`   VARCHAR(20)  NOT NULL COMMENT '文件扩展名',
    `file_category`    VARCHAR(50)  NOT NULL COMMENT '文件分类(image/video/document/audio)',
    `file_data`        LONGBLOB     NOT NULL COMMENT '文件二进制数据',
    `md5_hash`         VARCHAR(32)  NOT NULL COMMENT '文件MD5哈希值',
    `sha256_hash`      VARCHAR(64) COMMENT '文件SHA256哈希值',
    `upload_user_id`   VARCHAR(64) COMMENT '上传用户ID',
    `upload_user_type` VARCHAR(20) COMMENT '上传用户类型(admin/business/consumer)',
    `bucket_name`      VARCHAR(100) NOT NULL DEFAULT 'default' COMMENT '存储桶名称',
    `is_public`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否公开访问(0-私有 1-公开)',
    `access_count`     BIGINT       NOT NULL DEFAULT 0 COMMENT '访问次数',
    `last_access_time` DATETIME COMMENT '最后访问时间',
    `status`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态(0-删除 1-正常 2-禁用)',
    `remark`           VARCHAR(500) COMMENT '备注',
    `created_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_id` (`file_id`),
    UNIQUE KEY `uk_md5_hash` (`md5_hash`),
    KEY `idx_file_category` (`file_category`),
    KEY `idx_upload_user` (`upload_user_id`, `upload_user_type`),
    KEY `idx_bucket_name` (`bucket_name`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件存储表';

-- 文件访问日志表
CREATE TABLE `file_access_log`
(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id`           VARCHAR(64) NOT NULL COMMENT '文件ID',
    `access_user_id`    VARCHAR(64) COMMENT '访问用户ID',
    `access_user_type`  VARCHAR(20) COMMENT '访问用户类型',
    `access_ip`         VARCHAR(45) COMMENT '访问IP地址',
    `access_user_agent` VARCHAR(500) COMMENT '用户代理',
    `access_referer`    VARCHAR(500) COMMENT '来源页面',
    `access_type`       VARCHAR(20) NOT NULL COMMENT '访问类型(view/download)',
    `access_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_access_user` (`access_user_id`, `access_user_type`),
    KEY `idx_access_time` (`access_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件访问日志表';

-- 文件分享表
CREATE TABLE `file_share`
(
    `id`                   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `share_id`             VARCHAR(64) NOT NULL COMMENT '分享唯一标识',
    `file_id`              VARCHAR(64) NOT NULL COMMENT '文件ID',
    `share_user_id`        VARCHAR(64) NOT NULL COMMENT '分享用户ID',
    `share_user_type`      VARCHAR(20) NOT NULL COMMENT '分享用户类型',
    `share_type`           VARCHAR(20) NOT NULL DEFAULT 'link' COMMENT '分享类型(link-链接分享 code-提取码分享)',
    `share_code`           VARCHAR(10) COMMENT '提取码',
    `share_password`       VARCHAR(20) COMMENT '访问密码',
    `expire_time`          DATETIME COMMENT '过期时间(NULL表示永不过期)',
    `max_access_count`     INT COMMENT '最大访问次数(NULL表示无限制)',
    `current_access_count` INT         NOT NULL DEFAULT 0 COMMENT '当前访问次数',
    `is_active`            TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否激活(0-禁用 1-激活)',
    `created_time`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_share_id` (`share_id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_share_user` (`share_user_id`, `share_user_type`),
    KEY `idx_created_time` (`created_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件分享表';

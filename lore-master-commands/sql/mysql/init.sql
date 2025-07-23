-- 创建后台管理数据库和C端数据库
CREATE DATABASE IF NOT EXISTS lore_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lore_business DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用后台管理数据库
USE lore_admin;

-- 管理员信息表
CREATE TABLE IF NOT EXISTS admin_user
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    username   VARCHAR(64)     NOT NULL COMMENT '用户名',
    password   VARCHAR(128)    NOT NULL COMMENT '密码',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员信息表';

-- 插入初始管理员账号
INSERT INTO admin_user (username, password)
VALUES ('admin', '123456');

select * from admin_user;
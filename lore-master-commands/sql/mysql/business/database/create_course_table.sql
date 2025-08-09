-- 课程管理表
-- 用于管理学习课程，支持普通课程和合集两种类型
-- 创建时间: 2025-01-08

DROP TABLE IF EXISTS `business_course`;

CREATE TABLE `business_course` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_code` varchar(64) NOT NULL COMMENT '课程编码，唯一标识',
  `title` varchar(255) NOT NULL COMMENT '课程标题',
  `description` text COMMENT '课程描述',
  `author` varchar(100) NOT NULL COMMENT '课程作者',
  `course_type` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '课程类型：NORMAL-普通课程，COLLECTION-合集',
  `content_type` varchar(20) DEFAULT NULL COMMENT '内容类型：ARTICLE-图文，VIDEO-视频（仅普通课程有效）',
  `difficulty_level` varchar(10) DEFAULT NULL COMMENT '课程难度等级（普通课程必填，合集自动计算）',
  `difficulty_levels` varchar(50) DEFAULT NULL COMMENT '包含的难度等级（合集专用，如：L1,L2,L3）',
  `parent_course_id` bigint(20) DEFAULT NULL COMMENT '父课程ID（普通课程属于某个合集时填写）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '课程状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档',
  `knowledge_node_code` varchar(64) DEFAULT NULL COMMENT '关联的知识点节点编码',
  `knowledge_node_path` varchar(500) DEFAULT NULL COMMENT '知识点节点全路径（用/分隔，如：java/basic/variable）',
  `knowledge_node_name_path` varchar(1000) DEFAULT NULL COMMENT '知识点节点名称全路径（用/分隔，便于搜索）',
  `tags` varchar(500) DEFAULT NULL COMMENT '课程标签（用逗号分隔）',
  `duration_minutes` int(11) DEFAULT NULL COMMENT '课程时长（分钟，视频课程专用）',
  `view_count` bigint(20) DEFAULT 0 COMMENT '观看次数',
  `like_count` bigint(20) DEFAULT 0 COMMENT '点赞数',
  `collect_count` bigint(20) DEFAULT 0 COMMENT '收藏数',
  `content_url` varchar(500) DEFAULT NULL COMMENT '内容链接（图文链接或视频链接）',
  `cover_image_url` varchar(500) DEFAULT NULL COMMENT '封面图片链接',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图链接',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_code` (`course_code`),
  KEY `idx_course_type` (`course_type`),
  KEY `idx_difficulty_level` (`difficulty_level`),
  KEY `idx_parent_course_id` (`parent_course_id`),
  KEY `idx_knowledge_node_code` (`knowledge_node_code`),
  KEY `idx_knowledge_node_path` (`knowledge_node_path`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程管理表';

-- 插入示例数据
INSERT INTO `business_course` (`course_code`, `title`, `description`, `author`, `course_type`, `content_type`, `difficulty_level`, `difficulty_levels`, `parent_course_id`, `sort_order`, `status`, `knowledge_node_code`, `knowledge_node_path`, `knowledge_node_name_path`, `tags`, `duration_minutes`, `view_count`, `like_count`, `collect_count`, `content_url`, `cover_image_url`, `thumbnail_url`, `publish_time`, `created_by`) VALUES

-- ========== Java课程系列 ==========
-- Java基础合集
('java_basic_collection', 'Java基础教程合集', 'Java编程语言基础知识合集，包含变量、数据类型、控制结构、面向对象等核心概念，适合零基础学习者', '张明老师', 'COLLECTION', NULL, NULL, 'L1,L2,L3', NULL, 1, 'PUBLISHED', 'java_basic', 'java/basic', 'Java/基础语法', 'Java,基础,编程,入门', NULL, 1250, 89, 156, '/course/java-basic', '/images/java-basic-cover.jpg', '/images/java-basic-thumb.jpg', '2024-01-01 10:00:00', 'admin'),

-- Java基础合集下的普通课程
('java_variable_l1', 'Java变量入门', '学习Java中的变量概念、声明方式和基本使用方法，掌握变量的命名规范', '张明老师', 'NORMAL', 'ARTICLE', 'L1', NULL, 1, 1, 'PUBLISHED', 'java_variable', 'java/basic/variable', 'Java/基础语法/变量', 'Java,变量,基础,入门', NULL, 320, 25, 45, '/course/java-variable-intro', '/images/java-variable-l1.jpg', '/images/java-variable-l1-thumb.jpg', '2024-01-02 10:00:00', 'admin'),

('java_variable_l2', 'Java变量进阶', '深入理解Java变量的作用域、生命周期和内存分配机制', '张明老师', 'NORMAL', 'VIDEO', 'L2', NULL, 1, 2, 'PUBLISHED', 'java_variable', 'java/basic/variable', 'Java/基础语法/变量', 'Java,变量,进阶,作用域', 25, 280, 18, 32, '/course/java-variable-advanced', '/images/java-variable-l2.jpg', '/images/java-variable-l2-thumb.jpg', '2024-01-03 10:00:00', 'admin'),

('java_datatype_l1', 'Java数据类型基础', '学习Java的基本数据类型和引用数据类型，理解类型转换规则', '李华老师', 'NORMAL', 'ARTICLE', 'L1', NULL, 1, 3, 'PUBLISHED', 'java_datatype', 'java/basic/datatype', 'Java/基础语法/数据类型', 'Java,数据类型,基础,类型转换', NULL, 410, 31, 58, '/course/java-datatype-basic', '/images/java-datatype-l1.jpg', '/images/java-datatype-l1-thumb.jpg', '2024-01-04 10:00:00', 'admin'),

('java_datatype_l3', 'Java数据类型高级应用', '掌握Java数据类型的高级特性、包装类和最佳实践', '李华老师', 'NORMAL', 'VIDEO', 'L3', NULL, 1, 4, 'PUBLISHED', 'java_datatype', 'java/basic/datatype', 'Java/基础语法/数据类型', 'Java,数据类型,高级,包装类', 35, 240, 15, 28, '/course/java-datatype-advanced', '/images/java-datatype-l3.jpg', '/images/java-datatype-l3-thumb.jpg', '2024-01-05 10:00:00', 'admin'),

-- Java面向对象合集
('java_oop_collection', 'Java面向对象编程', 'Java面向对象编程核心概念，包括类、对象、继承、多态、封装等重要特性', '王强老师', 'COLLECTION', NULL, NULL, 'L2,L3,L4', NULL, 2, 'PUBLISHED', 'java_oop', 'java/oop', 'Java/面向对象', 'Java,面向对象,OOP,类,对象', NULL, 890, 67, 123, '/course/java-oop', '/images/java-oop-cover.jpg', '/images/java-oop-thumb.jpg', '2024-01-10 10:00:00', 'admin'),

('java_class_l2', 'Java类和对象', '学习Java中类的定义、对象的创建和使用，理解面向对象的基本概念', '王强老师', 'NORMAL', 'VIDEO', 'L2', NULL, 2, 1, 'PUBLISHED', 'java_class', 'java/oop/class', 'Java/面向对象/类和对象', 'Java,类,对象,面向对象', 30, 450, 35, 67, '/course/java-class-object', '/images/java-class-l2.jpg', '/images/java-class-l2-thumb.jpg', '2024-01-11 10:00:00', 'admin'),

('java_inheritance_l3', 'Java继承机制', '深入理解Java继承的概念、语法和应用场景，掌握super关键字的使用', '王强老师', 'NORMAL', 'ARTICLE', 'L3', NULL, 2, 2, 'PUBLISHED', 'java_inheritance', 'java/oop/inheritance', 'Java/面向对象/继承', 'Java,继承,super,重写', NULL, 340, 28, 45, '/course/java-inheritance', '/images/java-inheritance-l3.jpg', '/images/java-inheritance-l3-thumb.jpg', '2024-01-12 10:00:00', 'admin'),

('java_polymorphism_l4', 'Java多态性', '掌握Java多态的实现原理、方法重载和重写的区别，理解动态绑定机制', '王强老师', 'NORMAL', 'VIDEO', 'L4', NULL, 2, 3, 'PUBLISHED', 'java_polymorphism', 'java/oop/polymorphism', 'Java/面向对象/多态', 'Java,多态,重载,重写,动态绑定', 40, 280, 22, 38, '/course/java-polymorphism', '/images/java-polymorphism-l4.jpg', '/images/java-polymorphism-l4-thumb.jpg', '2024-01-13 10:00:00', 'admin'),

-- ========== Python课程系列 ==========
-- Python基础合集
('python_basic_collection', 'Python基础教程', 'Python编程语言基础教程，从零开始学习Python语法、数据结构和基本编程概念', '刘芳老师', 'COLLECTION', NULL, NULL, 'L1,L2', NULL, 3, 'PUBLISHED', 'python_basic', 'python/basic', 'Python/基础语法', 'Python,基础,编程,入门', NULL, 680, 45, 89, '/course/python-basic', '/images/python-basic-cover.jpg', '/images/python-basic-thumb.jpg', '2024-01-15 10:00:00', 'admin'),

('python_intro_l1', 'Python入门教程', 'Python编程语言入门，了解Python的特点、安装配置和第一个程序', '刘芳老师', 'NORMAL', 'VIDEO', 'L1', NULL, 3, 1, 'PUBLISHED', 'python_intro', 'python/basic/intro', 'Python/基础语法/入门', 'Python,入门,基础,安装', 20, 520, 38, 72, '/course/python-intro', '/images/python-intro-l1.jpg', '/images/python-intro-l1-thumb.jpg', '2024-01-16 10:00:00', 'admin'),

('python_syntax_l1', 'Python基础语法', '学习Python的基础语法，包括变量、数据类型、运算符和基本输入输出', '刘芳老师', 'NORMAL', 'ARTICLE', 'L1', NULL, 3, 2, 'PUBLISHED', 'python_syntax', 'python/basic/syntax', 'Python/基础语法/语法基础', 'Python,语法,变量,数据类型', NULL, 380, 29, 51, '/course/python-syntax', '/images/python-syntax-l1.jpg', '/images/python-syntax-l1-thumb.jpg', '2024-01-17 10:00:00', 'admin'),

('python_control_l2', 'Python控制结构', '掌握Python的条件语句、循环语句和异常处理机制', '刘芳老师', 'NORMAL', 'VIDEO', 'L2', NULL, 3, 3, 'PUBLISHED', 'python_control', 'python/basic/control', 'Python/基础语法/控制结构', 'Python,条件语句,循环,异常处理', 35, 290, 21, 34, '/course/python-control', '/images/python-control-l2.jpg', '/images/python-control-l2-thumb.jpg', '2024-01-18 10:00:00', 'admin'),

-- ========== 前端课程系列 ==========
-- HTML基础课程
('html_basic_l1', 'HTML基础入门', '学习HTML的基本概念、标签结构和常用元素，构建第一个网页', '陈伟老师', 'NORMAL', 'ARTICLE', 'L1', NULL, NULL, 1, 'PUBLISHED', 'html_basic', 'frontend/html/basic', '前端开发/HTML/基础', 'HTML,网页,标签,前端', NULL, 450, 56, 98, '/course/html-basic', '/images/html-basic-l1.jpg', '/images/html-basic-l1-thumb.jpg', '2024-01-20 10:00:00', 'admin'),

('html_forms_l2', 'HTML表单详解', '深入学习HTML表单元素、表单验证和用户交互设计', '陈伟老师', 'NORMAL', 'VIDEO', 'L2', NULL, NULL, 2, 'PUBLISHED', 'html_forms', 'frontend/html/forms', '前端开发/HTML/表单', 'HTML,表单,验证,交互', 28, 320, 24, 42, '/course/html-forms', '/images/html-forms-l2.jpg', '/images/html-forms-l2-thumb.jpg', '2024-01-21 10:00:00', 'admin'),

-- CSS基础课程
('css_basic_l1', 'CSS基础样式', '学习CSS的基本语法、选择器和常用样式属性，美化网页外观', '赵丽老师', 'NORMAL', 'ARTICLE', 'L1', NULL, NULL, 3, 'PUBLISHED', 'css_basic', 'frontend/css/basic', '前端开发/CSS/基础', 'CSS,样式,选择器,前端', NULL, 380, 42, 76, '/course/css-basic', '/images/css-basic-l1.jpg', '/images/css-basic-l1-thumb.jpg', '2024-01-22 10:00:00', 'admin'),

('css_layout_l3', 'CSS布局技术', '掌握CSS的布局技术，包括Flexbox、Grid和响应式设计', '赵丽老师', 'NORMAL', 'VIDEO', 'L3', NULL, NULL, 4, 'PUBLISHED', 'css_layout', 'frontend/css/layout', '前端开发/CSS/布局', 'CSS,布局,Flexbox,Grid,响应式', 45, 260, 19, 31, '/course/css-layout', '/images/css-layout-l3.jpg', '/images/css-layout-l3-thumb.jpg', '2024-01-23 10:00:00', 'admin'),

-- JavaScript基础课程
('js_basic_l1', 'JavaScript基础', '学习JavaScript的基本语法、变量、函数和DOM操作', '孙杰老师', 'NORMAL', 'VIDEO', 'L1', NULL, NULL, 5, 'PUBLISHED', 'js_basic', 'frontend/javascript/basic', '前端开发/JavaScript/基础', 'JavaScript,JS,DOM,函数', 32, 420, 33, 59, '/course/js-basic', '/images/js-basic-l1.jpg', '/images/js-basic-l1-thumb.jpg', '2024-01-24 10:00:00', 'admin'),

('js_advanced_l4', 'JavaScript高级特性', '深入学习JavaScript的闭包、原型链、异步编程和ES6新特性', '孙杰老师', 'NORMAL', 'ARTICLE', 'L4', NULL, NULL, 6, 'PUBLISHED', 'js_advanced', 'frontend/javascript/advanced', '前端开发/JavaScript/高级', 'JavaScript,闭包,原型链,ES6,异步', NULL, 180, 12, 23, '/course/js-advanced', '/images/js-advanced-l4.jpg', '/images/js-advanced-l4-thumb.jpg', '2024-01-25 10:00:00', 'admin'),

-- ========== 数据库课程系列 ==========
-- MySQL基础课程
('mysql_basic_l1', 'MySQL数据库入门', '学习MySQL数据库的基本概念、安装配置和基础SQL语句', '周敏老师', 'NORMAL', 'VIDEO', 'L1', NULL, NULL, 7, 'PUBLISHED', 'mysql_basic', 'database/mysql/basic', '数据库/MySQL/基础', 'MySQL,数据库,SQL,入门', 38, 350, 27, 48, '/course/mysql-basic', '/images/mysql-basic-l1.jpg', '/images/mysql-basic-l1-thumb.jpg', '2024-01-26 10:00:00', 'admin'),

('mysql_advanced_l3', 'MySQL高级查询', '掌握MySQL的高级查询技术、索引优化和性能调优', '周敏老师', 'NORMAL', 'ARTICLE', 'L3', NULL, NULL, 8, 'PUBLISHED', 'mysql_advanced', 'database/mysql/advanced', '数据库/MySQL/高级', 'MySQL,高级查询,索引,性能优化', NULL, 220, 16, 29, '/course/mysql-advanced', '/images/mysql-advanced-l3.jpg', '/images/mysql-advanced-l3-thumb.jpg', '2024-01-27 10:00:00', 'admin'),

-- ========== 算法课程系列 ==========
-- 算法基础合集
('algorithm_basic_collection', '算法基础教程', '计算机算法基础知识，包括排序、搜索、动态规划等经典算法', '吴涛老师', 'COLLECTION', NULL, NULL, 'L2,L3,L4,L5', NULL, 4, 'PUBLISHED', 'algorithm_basic', 'algorithm/basic', '算法/基础算法', '算法,排序,搜索,动态规划', NULL, 560, 34, 67, '/course/algorithm-basic', '/images/algorithm-basic-cover.jpg', '/images/algorithm-basic-thumb.jpg', '2024-01-28 10:00:00', 'admin'),

('sort_algorithm_l2', '排序算法详解', '学习常见的排序算法，包括冒泡排序、选择排序、快速排序等', '吴涛老师', 'NORMAL', 'VIDEO', 'L2', NULL, 4, 1, 'PUBLISHED', 'sort_algorithm', 'algorithm/basic/sort', '算法/基础算法/排序', '排序算法,冒泡排序,快速排序', 42, 310, 23, 41, '/course/sort-algorithm', '/images/sort-algorithm-l2.jpg', '/images/sort-algorithm-l2-thumb.jpg', '2024-01-29 10:00:00', 'admin'),

('search_algorithm_l3', '搜索算法原理', '掌握线性搜索、二分搜索和哈希搜索的原理和实现', '吴涛老师', 'NORMAL', 'ARTICLE', 'L3', NULL, 4, 2, 'PUBLISHED', 'search_algorithm', 'algorithm/basic/search', '算法/基础算法/搜索', '搜索算法,二分搜索,哈希搜索', NULL, 250, 18, 32, '/course/search-algorithm', '/images/search-algorithm-l3.jpg', '/images/search-algorithm-l3-thumb.jpg', '2024-01-30 10:00:00', 'admin');

-- 更新普通课程的父课程关联
-- Java基础合集的子课程
UPDATE `business_course` SET `parent_course_id` = (
    SELECT id FROM (SELECT id FROM `business_course` WHERE `course_code` = 'java_basic_collection') AS temp
) WHERE `course_code` IN ('java_variable_l1', 'java_variable_l2', 'java_datatype_l1', 'java_datatype_l3');

-- Java面向对象合集的子课程
UPDATE `business_course` SET `parent_course_id` = (
    SELECT id FROM (SELECT id FROM `business_course` WHERE `course_code` = 'java_oop_collection') AS temp
) WHERE `course_code` IN ('java_class_l2', 'java_inheritance_l3', 'java_polymorphism_l4');

-- Python基础合集的子课程
UPDATE `business_course` SET `parent_course_id` = (
    SELECT id FROM (SELECT id FROM `business_course` WHERE `course_code` = 'python_basic_collection') AS temp
) WHERE `course_code` IN ('python_intro_l1', 'python_syntax_l1', 'python_control_l2');

-- 算法基础合集的子课程
UPDATE `business_course` SET `parent_course_id` = (
    SELECT id FROM (SELECT id FROM `business_course` WHERE `course_code` = 'algorithm_basic_collection') AS temp
) WHERE `course_code` IN ('sort_algorithm_l2', 'search_algorithm_l3');

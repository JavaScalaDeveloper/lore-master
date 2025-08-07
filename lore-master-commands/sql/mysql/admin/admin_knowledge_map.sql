-- 技能路线知识图谱表（简化版）
-- 单表存储多层级技能树，支持最多10层深度
CREATE TABLE IF NOT EXISTS `admin_knowledge_map` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 核心节点字段
  `node_code` varchar(100) NOT NULL COMMENT '节点编码，全局唯一',
  `node_name` varchar(200) NOT NULL COMMENT '节点名称',
  `node_type` varchar(20) NOT NULL COMMENT '节点类型：ROOT-根节点，LEVEL-层级节点，BRANCH-分支节点，LEAF-叶子节点',

  -- 树形结构字段
  `parent_code` varchar(100) DEFAULT NULL COMMENT '父节点编码，根节点为NULL',
  `root_code` varchar(100) NOT NULL COMMENT '根节点编码，用于快速定位技能树',
  `node_path` varchar(500) NOT NULL COMMENT '节点路径，如：java/l1/basic/variables',
  `level_depth` tinyint NOT NULL DEFAULT '1' COMMENT '层级深度，1-10层',

  -- 排序字段
  `level_type` varchar(20) DEFAULT NULL COMMENT '层级类型：L1,L2,L3,L4,L5等',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '同级排序序号',

  -- 关联字段
  `skill_catalog_code` varchar(100) DEFAULT NULL COMMENT '关联learning_skill_catalog表的skill_code',

  -- 基础属性
  `description` text COMMENT '节点描述',
  `difficulty_level` varchar(20) DEFAULT 'BEGINNER' COMMENT '难度等级：BEGINNER,INTERMEDIATE,ADVANCED,EXPERT',
  `estimated_hours` int DEFAULT '0' COMMENT '预估学习时长（小时）',

  -- 系统字段
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-停用',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 主键和索引
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_code` (`node_code`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_root_code` (`root_code`),
  KEY `idx_level_depth` (`level_depth`),
  KEY `idx_level_type` (`level_type`),
  KEY `idx_node_type` (`node_type`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_skill_catalog_code` (`skill_catalog_code`),
  KEY `idx_status` (`status`),
  KEY `idx_composite_tree` (`root_code`, `level_depth`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能路线知识图谱表';

-- ========================================
-- 示例数据插入（Java专家技能路线）
-- ========================================

-- 第1层：根节点 - Java专家
INSERT INTO `admin_knowledge_map` (
  `node_code`, `node_name`, `node_type`, `parent_code`, `root_code`,
  `node_path`, `level_depth`, `sort_order`, `skill_catalog_code`,
  `difficulty_level`, `estimated_hours`, `description`, `status`, `created_by`
) VALUES (
  'java_expert', 'Java专家', 'ROOT', NULL, 'java_expert',
  'java_expert', 1, 1, 'java_fullstack',
  'EXPERT', 2000, 'Java全栈开发专家技能路线，从初级到专家的完整学习路径',
  'ACTIVE', 'system'
);

-- 第2层：层级节点 - L1到L5
INSERT INTO `admin_knowledge_map` (
  `node_code`, `node_name`, `node_type`, `parent_code`, `root_code`,
  `node_path`, `level_depth`, `level_type`, `sort_order`,
  `difficulty_level`, `estimated_hours`, `description`, `status`, `created_by`
) VALUES
('java_l1', 'L1初级', 'LEVEL', 'java_expert', 'java_expert',
 'java_expert/l1', 2, 'L1', 1, 'BEGINNER', 200,
 'Java初级开发工程师技能要求，掌握Java基础语法和核心概念',
 'ACTIVE', 'system'),

('java_l2', 'L2中级', 'LEVEL', 'java_expert', 'java_expert',
 'java_expert/l2', 2, 'L2', 2, 'INTERMEDIATE', 300,
 'Java中级开发工程师技能要求，掌握框架使用和数据库操作',
 'ACTIVE', 'system'),

('java_l3', 'L3高级', 'LEVEL', 'java_expert', 'java_expert',
 'java_expert/l3', 2, 'L3', 3, 'ADVANCED', 400,
 'Java高级开发工程师技能要求，掌握微服务和分布式系统',
 'ACTIVE', 'system'),

('java_l4', 'L4资深', 'LEVEL', 'java_expert', 'java_expert',
 'java_expert/l4', 2, 'L4', 4, 'ADVANCED', 500,
 'Java资深开发工程师技能要求，具备架构设计和团队领导能力',
 'ACTIVE', 'system'),

('java_l5', 'L5专家', 'LEVEL', 'java_expert', 'java_expert',
 'java_expert/l5', 2, 'L5', 5, 'EXPERT', 600,
 'Java技术专家技能要求，具备技术创新和行业影响力',
 'ACTIVE', 'system');

-- 第3层：技术领域节点（以L1为例）
INSERT INTO `admin_knowledge_map` (
  `node_code`, `node_name`, `node_type`, `parent_code`, `root_code`,
  `node_path`, `level_depth`, `sort_order`,
  `difficulty_level`, `estimated_hours`, `description`, `status`, `created_by`
) VALUES
('java_l1_basic', 'Java基础语法', 'BRANCH', 'java_l1', 'java_expert',
 'java_expert/l1/basic', 3, 1, 'BEGINNER', 80,
 'Java基础语法，包括变量、数据类型、控制结构等',
 'ACTIVE', 'system'),

('java_l1_oop', '面向对象编程', 'BRANCH', 'java_l1', 'java_expert',
 'java_expert/l1/oop', 3, 2, 'BEGINNER', 60,
 'Java面向对象编程，包括类、对象、继承、多态等',
 'ACTIVE', 'system'),

('java_l1_api', '常用API', 'BRANCH', 'java_l1', 'java_expert',
 'java_expert/l1/api', 3, 3, 'BEGINNER', 60,
 'Java常用API的使用，包括String、集合、IO等',
 'ACTIVE', 'system');

-- 第4层：具体知识点（以Java基础语法为例）
INSERT INTO `admin_knowledge_map` (
  `node_code`, `node_name`, `node_type`, `parent_code`, `root_code`,
  `node_path`, `level_depth`, `sort_order`,
  `difficulty_level`, `estimated_hours`, `description`, `status`, `created_by`
) VALUES
('java_l1_basic_variables', '变量与数据类型', 'LEAF', 'java_l1_basic', 'java_expert',
 'java_expert/l1/basic/variables', 4, 1, 'BEGINNER', 20,
 '学习Java中的变量声明、数据类型和类型转换',
 'ACTIVE', 'system'),

('java_l1_basic_operators', '运算符', 'LEAF', 'java_l1_basic', 'java_expert',
 'java_expert/l1/basic/operators', 4, 2, 'BEGINNER', 15,
 '学习Java中的各种运算符及其优先级',
 'ACTIVE', 'system'),

('java_l1_basic_control', '控制结构', 'LEAF', 'java_l1_basic', 'java_expert',
 'java_expert/l1/basic/control', 4, 3, 'BEGINNER', 25,
 '学习Java中的条件语句和循环语句',
 'ACTIVE', 'system');

-- ========================================
-- 常用查询示例
-- ========================================

-- 1. 查询整个技能树结构（按层级和排序）
-- SELECT node_code, node_name, node_type, level_depth, level_type, sort_order, node_path
-- FROM admin_knowledge_map
-- WHERE root_code = 'java_expert' AND status = 'ACTIVE'
-- ORDER BY level_depth, sort_order;

-- 2. 查询指定节点的直接子节点
-- SELECT node_code, node_name, node_type, level_depth, sort_order
-- FROM admin_knowledge_map
-- WHERE parent_code = 'java_l1' AND status = 'ACTIVE'
-- ORDER BY sort_order;

-- 3. 查询指定层级的所有节点
-- SELECT node_code, node_name, level_type, sort_order
-- FROM admin_knowledge_map
-- WHERE root_code = 'java_expert' AND level_depth = 2 AND status = 'ACTIVE'
-- ORDER BY sort_order;

-- 4. 查询所有叶子节点（最终知识点）
-- SELECT node_code, node_name, node_path, difficulty_level, estimated_hours
-- FROM admin_knowledge_map
-- WHERE root_code = 'java_expert' AND is_leaf = 1 AND status = 'ACTIVE'
-- ORDER BY node_path;

-- 5. 查询指定路径下的所有节点
-- SELECT node_code, node_name, level_depth, node_path
-- FROM admin_knowledge_map
-- WHERE root_code = 'java_expert' AND node_path LIKE 'java_expert/l1/%' AND status = 'ACTIVE'
-- ORDER BY level_depth, sort_order;

-- 6. 统计各层级节点数量
-- SELECT level_depth, level_type, COUNT(*) as node_count
-- FROM admin_knowledge_map
-- WHERE root_code = 'java_expert' AND status = 'ACTIVE'
-- GROUP BY level_depth, level_type
-- ORDER BY level_depth;

-- 7. 查询与learning_skill_catalog关联的根节点
-- SELECT node_code, node_name, skill_catalog_code, skill_catalog_name
-- FROM admin_knowledge_map
-- WHERE node_type = 'ROOT' AND skill_catalog_code IS NOT NULL AND status = 'ACTIVE';

-- ========================================
-- 表结构设计说明
-- ========================================

/*
表结构特点：
1. 单表设计：避免多表关联的复杂性，所有节点信息存储在一张表中
2. 树形结构：通过parent_code和node_path支持完整的树形关系
3. 层级深度：支持最多10层的层级深度，满足复杂技能树需求
4. 高效查询：通过多个索引支持各种查询场景
5. 扩展性强：JSON字段支持灵活的扩展属性

核心字段说明：
- node_code: 节点唯一标识，全局唯一
- parent_code: 父节点编码，构建树形关系
- root_code: 根节点编码，快速定位技能树
- node_path: 完整路径，支持路径查询
- level_depth: 层级深度，1-10层
- level_type: 层级类型，如L1-L5
- skill_catalog_code: 关联learning_skill_catalog表的skill_code

使用场景：
1. 技能路线规划：定义从初级到专家的完整学习路径
2. 知识图谱构建：建立知识点之间的层级关系
3. 学习路径推荐：根据用户水平推荐合适的学习内容
4. 进度跟踪：跟踪用户在技能树中的学习进度
5. 内容管理：管理和维护技能相关的学习内容

索引优化：
- 主要查询场景都有对应的索引支持
- 复合索引优化常用查询组合
- 路径索引支持模糊路径查询
*/
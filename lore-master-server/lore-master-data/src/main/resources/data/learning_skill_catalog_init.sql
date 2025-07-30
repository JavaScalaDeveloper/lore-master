-- 学习技能目录初始化数据
-- 清空现有数据
DELETE FROM learning_skill_catalog;

-- 重置自增ID
ALTER TABLE learning_skill_catalog AUTO_INCREMENT = 1;

-- 插入基础数据
INSERT INTO learning_skill_catalog (id, skill_code, skill_name, skill_path, level, parent_code, icon, description, sort_order, is_active, difficulty_level, estimated_hours, tags, created_time, updated_time, created_by, updated_by) VALUES 

-- 一级分类
(1, 'foreign_language', '外语', 'foreign_language', 1, NULL, '🌍', '外语学习相关技能', 1, 1, NULL, NULL, '["language", "communication"]', NOW(), NOW(), 'system', 'system'),
(2, 'it_internet', 'IT/互联网', 'it_internet', 1, NULL, '💻', 'IT和互联网技术相关技能', 2, 1, NULL, NULL, '["technology", "programming"]', NOW(), NOW(), 'system', 'system'),
(3, 'finance_audit_tax', '财务/审计/税务', 'finance_audit_tax', 1, NULL, '💰', '财务、审计、税务相关专业技能', 3, 1, NULL, NULL, '["finance", "professional"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - 外语
(10, 'english', '英语', 'foreign_language/english', 2, 'foreign_language', '🇺🇸', '英语语言技能', 1, 1, NULL, NULL, '["english", "language"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - IT/互联网
(20, 'backend', '后端', 'it_internet/backend', 2, 'it_internet', '⚙️', '后端开发技术', 1, 1, NULL, NULL, '["backend", "server", "development"]', NOW(), NOW(), 'system', 'system'),
(21, 'frontend', '前端', 'it_internet/frontend', 2, 'it_internet', '🎨', '前端开发技术', 2, 1, NULL, NULL, '["frontend", "ui", "development"]', NOW(), NOW(), 'system', 'system'),

-- 二级分类 - 财务/审计/税务
(30, 'finance', '财务', 'finance_audit_tax/finance', 2, 'finance_audit_tax', '📊', '财务管理相关技能', 1, 1, NULL, NULL, '["finance", "accounting"]', NOW(), NOW(), 'system', 'system'),
(31, 'audit', '审计', 'finance_audit_tax/audit', 2, 'finance_audit_tax', '🔍', '审计相关技能', 2, 1, NULL, NULL, '["audit", "inspection"]', NOW(), NOW(), 'system', 'system'),
(32, 'tax', '税务', 'finance_audit_tax/tax', 2, 'finance_audit_tax', '📋', '税务相关技能', 3, 1, NULL, NULL, '["tax", "taxation"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 英语
(100, 'cet4', '大学英语CET-4', 'foreign_language/english/cet4', 3, 'english', NULL, '大学英语四级考试', 1, 1, 'intermediate', 200, '["cet", "exam", "university"]', NOW(), NOW(), 'system', 'system'),
(101, 'cet6', '大学英语CET-6', 'foreign_language/english/cet6', 3, 'english', NULL, '大学英语六级考试', 2, 1, 'intermediate', 300, '["cet", "exam", "university"]', NOW(), NOW(), 'system', 'system'),
(102, 'ielts', '雅思', 'foreign_language/english/ielts', 3, 'english', NULL, '国际英语语言测试系统', 3, 1, 'advanced', 400, '["ielts", "exam", "international"]', NOW(), NOW(), 'system', 'system'),
(103, 'toefl', '托福', 'foreign_language/english/toefl', 3, 'english', NULL, '托福英语考试', 4, 1, 'advanced', 450, '["toefl", "exam", "international"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 后端
(200, 'java', 'Java', 'it_internet/backend/java', 3, 'backend', NULL, 'Java编程语言', 1, 1, 'intermediate', 500, '["java", "programming", "oop"]', NOW(), NOW(), 'system', 'system'),
(201, 'bigdata_engineer', '大数据开发工程师', 'it_internet/backend/bigdata_engineer', 3, 'backend', NULL, '大数据开发相关技术栈', 2, 1, 'advanced', 800, '["bigdata", "hadoop", "spark"]', NOW(), NOW(), 'system', 'system'),
(202, 'python', 'Python', 'it_internet/backend/python', 3, 'backend', NULL, 'Python编程语言', 3, 1, 'beginner', 300, '["python", "programming", "scripting"]', NOW(), NOW(), 'system', 'system'),
(203, 'golang', 'Golang', 'it_internet/backend/golang', 3, 'backend', NULL, 'Go编程语言', 4, 1, 'intermediate', 400, '["golang", "programming", "concurrent"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 前端
(210, 'react_vue', 'React/Vue', 'it_internet/frontend/react_vue', 3, 'frontend', NULL, 'React和Vue前端框架', 1, 1, 'intermediate', 350, '["react", "vue", "framework"]', NOW(), NOW(), 'system', 'system'),
(211, 'mobile_dev', 'IOS/Android', 'it_internet/frontend/mobile_dev', 3, 'frontend', NULL, '移动端开发技术', 2, 1, 'advanced', 600, '["ios", "android", "mobile"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 财务
(300, 'cpa', '注册会计师CPA', 'finance_audit_tax/finance/cpa', 3, 'finance', NULL, '注册会计师资格认证', 1, 1, 'advanced', 1000, '["cpa", "accounting", "certification"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 审计
(310, 'cia', '国际注册内部审计师CIA', 'finance_audit_tax/audit/cia', 3, 'audit', NULL, '国际注册内部审计师认证', 1, 1, 'advanced', 800, '["cia", "audit", "certification"]', NOW(), NOW(), 'system', 'system'),

-- 三级目标 - 税务
(320, 'cta', '税务师CTA', 'finance_audit_tax/tax/cta', 3, 'tax', NULL, '税务师资格认证', 1, 1, 'advanced', 600, '["cta", "tax", "certification"]', NOW(), NOW(), 'system', 'system');

-- 查询验证数据
SELECT 
    level,
    COUNT(*) as count,
    GROUP_CONCAT(skill_name ORDER BY sort_order SEPARATOR ', ') as skills
FROM learning_skill_catalog 
WHERE is_active = 1 
GROUP BY level 
ORDER BY level;

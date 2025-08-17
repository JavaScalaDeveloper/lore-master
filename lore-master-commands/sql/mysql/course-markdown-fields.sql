-- 为business_course表添加Markdown内容支持字段
ALTER TABLE `lore_business`.`business_course` 
ADD COLUMN `content_markdown` LONGTEXT COMMENT '课程Markdown格式内容详情',
ADD COLUMN `content_html` LONGTEXT COMMENT '课程HTML格式内容（由Markdown转换而来）',
ADD COLUMN `content_updated_time` DATETIME DEFAULT NULL COMMENT '内容最后更新时间',
ADD COLUMN `content_file_ids` TEXT COMMENT '内容中引用的文件ID列表（JSON格式）';

-- 添加索引以提高查询性能
CREATE INDEX `idx_business_course_content_updated` ON `lore_business`.`business_course` (`content_updated_time`);

-- 注释说明：
-- content_markdown: 存储原始的Markdown格式内容
-- content_html: 存储转换后的HTML内容，用于前端直接显示
-- content_updated_time: 记录内容最后更新时间
-- content_file_ids: 存储内容中引用的文件ID列表，JSON格式如：["file1", "file2"]

-- 轮播图表结构设计
-- 数据库：lore_consumer（消费端轮播图，简化版本）

USE `lore_consumer`;

-- 创建消费端轮播图表（简化版本）
CREATE TABLE `consumer_carousel_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `banner_id` varchar(32) NOT NULL COMMENT '轮播图唯一标识',
  `title` varchar(200) NOT NULL COMMENT '轮播图标题',
  `subtitle` varchar(500) DEFAULT NULL COMMENT '副标题/简介',
  `cover_image_url` varchar(1000) NOT NULL COMMENT '封面图片URL',
  `content_markdown` longtext COMMENT '详情内容(Markdown格式)',
  `content_html` longtext COMMENT '详情内容(HTML格式，由Markdown转换)',
  `jump_url` varchar(1000) DEFAULT NULL COMMENT '跳转链接(可选)',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序，数字越小越靠前',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用',
  `view_count` bigint DEFAULT 0 COMMENT '查看次数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_banner_id` (`banner_id`),
  KEY `idx_status_sort` (`status`, `sort_order`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费端轮播图表';


-- 插入示例数据
INSERT INTO `consumer_carousel_banner` (
  `banner_id`,
  `title`,
  `subtitle`,
  `cover_image_url`,
  `content_markdown`,
  `sort_order`,
  `status`,
  `created_by`
) VALUES
(
  'banner_001',
  '精选技术文章推荐',
  '最新前沿技术分享，助力开发者成长',
  'https://via.placeholder.com/800x400/4A90E2/FFFFFF?text=Tech+Articles',
  '# 精选技术文章推荐

## 本期亮点

### 🚀 前端技术
- React 18 新特性详解
- Vue 3 Composition API 最佳实践
- TypeScript 高级类型应用

### 🔧 后端技术
- Spring Boot 3.0 升级指南
- 微服务架构设计模式
- 数据库性能优化技巧

### 📱 移动开发
- Flutter 跨平台开发实战
- React Native 性能优化
- 小程序开发最佳实践

## 学习路径

1. **基础知识巩固**
2. **项目实战练习**
3. **源码深度解析**
4. **性能优化实践**

> 持续学习，持续进步！',
  1,
  'ACTIVE',
  'admin'
),
(
  'banner_002',
  '开源项目精选',
  '优质开源项目推荐，拓展技术视野',
  'https://via.placeholder.com/800x400/50C878/FFFFFF?text=Open+Source',
  '# 开源项目精选

## 本月推荐

### 🌟 热门项目
- **项目A**: 高性能Web框架
- **项目B**: 智能代码生成工具
- **项目C**: 分布式存储系统

### 📊 数据统计
- Star数量: 10K+
- Fork数量: 2K+
- 贡献者: 100+

## 如何参与

1. Fork项目到个人仓库
2. 创建功能分支
3. 提交Pull Request
4. 代码Review

**让我们一起为开源社区贡献力量！**',
  2,
  'ACTIVE',
  'admin'
),
(
  'banner_003',
  '技术大会回顾',
  '行业技术大会精彩内容回顾',
  'https://via.placeholder.com/800x400/FF6B6B/FFFFFF?text=Tech+Conference',
  '# 技术大会回顾

## 大会亮点

### 🎯 主题演讲
- AI与未来技术发展
- 云原生架构实践
- 数字化转型趋势

### 💡 技术分享
- 大规模系统设计
- 性能优化实战
- 安全防护策略

## 核心观点

> "技术的发展永远超出我们的想象"

### 未来趋势
1. 人工智能普及化
2. 边缘计算兴起
3. 量子计算突破
4. 区块链应用扩展

**把握技术趋势，引领未来发展！**',
  3,
  'ACTIVE',
  'admin'
);

-- 创建索引以提高查询性能
CREATE INDEX `idx_view_count` ON `consumer_carousel_banner` (`view_count` DESC);
CREATE INDEX `idx_title` ON `consumer_carousel_banner` (`title`);

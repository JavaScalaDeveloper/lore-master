# 学习中心页面

## 功能概述

学习中心页面是一个用于搜索和浏览学习内容的交互页面，提供了丰富的筛选功能和美观的卡片展示。

## 主要功能

### 1. 搜索功能
- 顶部搜索框支持关键词搜索
- 支持搜索课程标题、描述、标签等内容
- 实时搜索，输入后点击搜索按钮或回车即可

### 2. 筛选功能
提供三个维度的筛选条件：
- **等级筛选**：L1-L5 难度等级
- **类型筛选**：普通课程 / 合集
- **内容筛选**：图文 / 视频

### 3. 课程展示
- 卡片式布局，美观直观
- 显示课程封面图片（支持占位符）
- 课程基本信息：标题、描述、作者、发布时间
- 统计数据：观看数、点赞数、收藏数
- 类型标签：区分普通课程和合集
- 难度等级标签
- 内容类型标识：图文/视频

### 4. 分页加载
- 支持下拉加载更多
- 智能分页，每页10条数据
- 加载状态提示

## 技术实现

### API接口
调用后端 `BusinessCourseController` 的 `queryCourseList` 方法：
- 接口地址：`/business/course/queryCourseList`
- 请求方式：POST
- 参数：CourseQueryDTO

### 数据结构
```typescript
interface CourseVO {
  id: number
  courseCode: string
  title: string
  description: string
  author: string
  courseType: 'NORMAL' | 'COLLECTION'
  contentType: 'ARTICLE' | 'VIDEO'
  difficultyLevel: string
  coverImageUrl?: string
  thumbnailUrl?: string
  viewCount: number
  likeCount: number
  collectCount: number
  publishTime: string
  subCourseCount?: number
}
```

### 样式特点
- 响应式设计，适配不同屏幕尺寸
- 现代化UI设计，使用渐变色和阴影效果
- 流畅的动画过渡
- 清晰的视觉层次

## 使用说明

1. **搜索课程**：在顶部搜索框输入关键词，点击搜索按钮
2. **筛选内容**：使用三个筛选条件缩小搜索范围
3. **浏览课程**：滚动查看课程列表，下拉加载更多
4. **查看详情**：点击课程卡片查看详细信息（待实现）

## 文件结构

```
src/pages/study/
├── study.tsx          # 主组件文件
├── study.css          # 样式文件
└── README.md          # 说明文档
```

## 依赖项

- React Hooks (useState)
- Taro API (request, showToast, showLoading等)
- Taro Components (View, Text, Input, Button等)

## 注意事项

1. 确保后端API服务正常运行
2. 检查API配置文件中的baseUrl设置
3. 图片资源需要正确的URL路径
4. 网络请求需要适当的错误处理

## 后续优化

1. 添加课程详情页面跳转
2. 实现收藏和点赞功能
3. 添加搜索历史记录
4. 优化图片加载性能
5. 添加骨架屏加载效果

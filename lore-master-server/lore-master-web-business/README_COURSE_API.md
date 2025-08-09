# 课程管理API文档

## 概述
课程管理API提供了完整的课程查询功能，支持分页查询、搜索、筛选等多种操作。

## 基础信息
- **基础路径**: `/course`
- **请求方式**: POST（所有接口）
- **请求格式**: JSON
- **响应格式**: JSON
- **编码**: UTF-8

## 通用响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 具体数据
  },
  "errorCode": null,
  "timestamp": 1704672000000
}
```

## API接口列表

### 1. 分页查询课程
**接口**: `POST /course/queryCourseList`

**请求体**:
```json
{
  "courseType": "NORMAL",
  "status": "PUBLISHED",
  "difficultyLevel": "L1",
  "author": "张明老师",
  "knowledgeNodePath": "java/basic",
  "keyword": "Java变量",
  "parentCourseId": 1,
  "sortBy": "publish_time_desc",
  "page": 0,
  "size": 20,
  "publishedOnly": true,
  "includeSubCourseCount": false
}
```

### 2. 根据课程编码获取详情
**接口**: `POST /course/getCourseByCode`

**请求体**:
```json
{
  "courseCode": "java_variable_l1",
  "userId": "user123",
  "includeSubCourses": true
}
```

### 3. 根据课程ID获取详情
**接口**: `POST /course/getCourseById`

**请求体**:
```json
{
  "courseId": 1,
  "userId": "user123",
  "includeSubCourses": true
}
```

### 4. 获取合集下的子课程
**接口**: `POST /course/getSubCourses`

**请求体**:
```json
{
  "parentCourseId": 1,
  "userId": "user123"
}
```

### 5. 搜索课程
**接口**: `POST /course/searchCourses`

**请求体**:
```json
{
  "keyword": "Java变量",
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 6. 获取热门课程
**接口**: `POST /course/getPopularCourses`

**请求体**:
```json
{
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 7. 获取最新课程
**接口**: `POST /course/getLatestCourses`

**请求体**:
```json
{
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 8. 根据知识点路径获取课程
**接口**: `POST /course/getCoursesByKnowledgePath`

**请求体**:
```json
{
  "knowledgeNodePath": "java/basic",
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 9. 根据难度等级获取课程
**接口**: `POST /course/getCoursesByDifficulty`

**请求体**:
```json
{
  "difficultyLevel": "L1",
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 10. 根据作者获取课程
**接口**: `POST /course/getCoursesByAuthor`

**请求体**:
```json
{
  "author": "张明老师",
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

### 11. 获取课程统计信息
**接口**: `POST /course/getCourseStatistics`

**请求体**: 无需请求体
```json
{}
```

### 12. 获取推荐课程
**接口**: `POST /course/getRecommendedCourses`

**请求体**:
```json
{
  "page": 0,
  "size": 20,
  "userId": "user123"
}
```

## 响应数据结构

### 课程对象 (CourseVO)
```json
{
  "id": 1,
  "courseCode": "java_variable_l1",
  "title": "Java变量入门",
  "description": "学习Java中的变量概念、声明方式和基本使用方法",
  "author": "张明老师",
  "courseType": "NORMAL",
  "contentType": "ARTICLE",
  "difficultyLevel": "L1",
  "difficultyLevels": null,
  "difficultyLevelList": null,
  "parentCourseId": 1,
  "parentCourseTitle": "Java基础教程合集",
  "sortOrder": 1,
  "status": "PUBLISHED",
  "knowledgeNodeCode": "java_variable",
  "knowledgeNodePath": "java/basic/variable",
  "knowledgeNodeNamePath": "Java/基础语法/变量",
  "tags": "Java,变量,基础,入门",
  "tagList": ["Java", "变量", "基础", "入门"],
  "durationMinutes": null,
  "formattedDuration": null,
  "viewCount": 320,
  "likeCount": 25,
  "collectCount": 45,
  "contentUrl": "/course/java-variable-intro",
  "coverImageUrl": "/images/java-variable-l1.jpg",
  "thumbnailUrl": "/images/java-variable-l1-thumb.jpg",
  "publishTime": "2024-01-02T10:00:00",
  "createdTime": "2024-01-02T10:00:00",
  "subCourseCount": null,
  "subCourses": null,
  "isCollected": false,
  "isLiked": false,
  "progressPercent": 0
}
```

### 分页响应 (CoursePageVO)
```json
{
  "courses": [
    // CourseVO数组
  ],
  "currentPage": 0,
  "pageSize": 20,
  "totalPages": 5,
  "totalElements": 100,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false,
  "statistics": {
    "totalCourses": 100,
    "normalCourses": 80,
    "collections": 20,
    "difficultyStats": {
      "l1Count": 30,
      "l2Count": 25,
      "l3Count": 20,
      "l4Count": 15,
      "l5Count": 10
    }
  }
}
```

## 错误码说明
- `success: false`: 请求失败
- `message`: 错误描述信息

## 接口特点
1. **非RESTful风格**: 所有接口都使用POST方法，通过不同的路径区分功能
2. **实体对象参数**: 使用DTO对象作为请求参数，而不是URL参数
3. **统一响应格式**: 使用ApiResponse包装所有响应数据
4. **类型安全**: 强类型的请求和响应对象

## 注意事项
1. 所有分页接口的页码从0开始
2. 课程详情接口会自动增加观看次数
3. 用户相关状态（收藏、点赞、进度）需要登录后才能获取
4. 搜索支持标题、描述、标签、知识点名称路径的模糊匹配
5. 所有接口都需要发送JSON格式的请求体
6. userId参数为可选，未登录用户可以传null或不传

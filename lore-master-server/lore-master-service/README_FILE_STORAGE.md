# 文件存储服务 - 模拟OSS存储

## 📋 概述

本模块实现了一个完整的文件存储服务，模拟OSS（对象存储服务）的功能，将文件以二进制流的形式存储在MySQL数据库中。支持图片、视频、音频、文档等多种文件类型的上传、下载、管理功能。

## 🎯 主要功能

### 核心功能
- **文件上传**: 支持多种文件类型上传，自动生成唯一标识
- **文件下载**: 支持在线预览和文件下载
- **文件管理**: 文件信息查询、删除、批量操作
- **访问控制**: 支持公开/私有文件访问控制
- **去重机制**: 基于MD5哈希值的文件去重
- **访问统计**: 记录文件访问日志和统计信息

### 高级功能
- **分类管理**: 自动识别文件类型并分类
- **存储桶**: 支持多存储桶管理
- **权限控制**: 基于用户类型的权限管理
- **文件分享**: 支持文件分享链接生成
- **清理机制**: 自动清理过期临时文件

## 🗄️ 数据库设计

### 主要表结构

#### 1. file_storage (文件存储表)
```sql
CREATE TABLE `file_storage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` VARCHAR(64) NOT NULL COMMENT '文件唯一标识',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(100) NOT NULL COMMENT '文件类型(MIME类型)',
    `file_extension` VARCHAR(20) NOT NULL COMMENT '文件扩展名',
    `file_category` VARCHAR(50) NOT NULL COMMENT '文件分类',
    `file_data` LONGBLOB NOT NULL COMMENT '文件二进制数据',
    `md5_hash` VARCHAR(32) NOT NULL COMMENT '文件MD5哈希值',
    `sha256_hash` VARCHAR(64) COMMENT '文件SHA256哈希值',
    `upload_user_id` VARCHAR(64) COMMENT '上传用户ID',
    `upload_user_type` VARCHAR(20) COMMENT '上传用户类型',
    `bucket_name` VARCHAR(100) NOT NULL DEFAULT 'default' COMMENT '存储桶名称',
    `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开访问',
    `access_count` BIGINT NOT NULL DEFAULT 0 COMMENT '访问次数',
    `last_access_time` DATETIME COMMENT '最后访问时间',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_id` (`file_id`),
    UNIQUE KEY `uk_md5_hash` (`md5_hash`)
);
```

#### 2. file_access_log (文件访问日志表)
```sql
CREATE TABLE `file_access_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` VARCHAR(64) NOT NULL COMMENT '文件ID',
    `access_user_id` VARCHAR(64) COMMENT '访问用户ID',
    `access_user_type` VARCHAR(20) COMMENT '访问用户类型',
    `access_ip` VARCHAR(45) COMMENT '访问IP地址',
    `access_user_agent` VARCHAR(500) COMMENT '用户代理',
    `access_referer` VARCHAR(500) COMMENT '来源页面',
    `access_type` VARCHAR(20) NOT NULL COMMENT '访问类型(view/download)',
    `access_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_file_id` (`file_id`)
);
```

## 🔧 API接口

### 文件上传
```http
POST /api/file/upload
Content-Type: multipart/form-data

Parameters:
- file: 上传的文件 (required)
- bucketName: 存储桶名称 (default: "default")
- filePath: 文件路径 (optional)
- isPublic: 是否公开 (default: false)
- uploadUserId: 上传用户ID (optional)
- uploadUserType: 上传用户类型 (default: "consumer")
- remark: 备注 (optional)
- overwrite: 是否覆盖 (default: false)
- customFileName: 自定义文件名 (optional)
```

### 简单上传
```http
POST /api/file/upload/simple
Content-Type: multipart/form-data

Parameters:
- file: 上传的文件 (required)
- uploadUserId: 上传用户ID (optional)
- uploadUserType: 上传用户类型 (default: "consumer")
```

### 获取文件信息
```http
GET /api/file/info/{fileId}
```

### 在线预览文件
```http
GET /api/file/view/{fileId}?accessUserId={userId}&accessUserType={userType}
```

### 下载文件
```http
GET /api/file/download/{fileId}?accessUserId={userId}&accessUserType={userType}
```

### 删除文件
```http
DELETE /api/file/{fileId}?operatorUserId={userId}&operatorUserType={userType}
```

### 批量删除文件
```http
DELETE /api/file/batch?operatorUserId={userId}&operatorUserType={userType}
Content-Type: application/json

Body: ["fileId1", "fileId2", "fileId3"]
```

### 分页查询文件
```http
GET /api/file/list?fileCategory={category}&uploadUserId={userId}&page=0&size=20
```

### 获取用户文件统计
```http
GET /api/file/statistics?userId={userId}&userType={userType}
```

### 检查文件是否存在
```http
GET /api/file/exists/{fileId}
```

### 根据MD5获取文件
```http
GET /api/file/md5/{md5Hash}
```

## 💻 使用示例

### Java代码示例

#### 1. 上传文件
```java
@Autowired
private FileStorageService fileStorageService;

// 简单上传
public FileInfoVO uploadFile(MultipartFile file, String userId) {
    return fileStorageService.uploadFile(file, userId, "consumer");
}

// 完整上传
public FileInfoVO uploadFileWithOptions(MultipartFile file, String userId) {
    FileUploadRequest request = new FileUploadRequest();
    request.setFile(file);
    request.setUploadUserId(userId);
    request.setUploadUserType("consumer");
    request.setBucketName("images");
    request.setIsPublic(true);
    request.setRemark("用户头像");
    
    return fileStorageService.uploadFile(request);
}
```

#### 2. 下载文件
```java
public byte[] downloadFile(String fileId, String userId) {
    return fileStorageService.downloadFile(fileId, userId, "consumer", "127.0.0.1");
}
```

#### 3. 查询文件
```java
public Page<FileInfoVO> queryUserImages(String userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return fileStorageService.queryFiles("image", userId, "consumer", 
                                        null, null, null, pageable);
}
```

### 前端JavaScript示例

#### 1. 上传文件
```javascript
// 上传文件
async function uploadFile(file, userId) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('uploadUserId', userId);
    formData.append('uploadUserType', 'consumer');
    formData.append('isPublic', 'true');
    
    const response = await fetch('/api/file/upload', {
        method: 'POST',
        body: formData
    });
    
    return await response.json();
}

// 简单上传
async function uploadFileSimple(file, userId) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('uploadUserId', userId);
    
    const response = await fetch('/api/file/upload/simple', {
        method: 'POST',
        body: formData
    });
    
    return await response.json();
}
```

#### 2. 显示图片
```javascript
// 显示图片
function displayImage(fileId) {
    const imageUrl = `/api/file/view/${fileId}?accessUserId=${userId}&accessUserType=consumer`;
    const img = document.createElement('img');
    img.src = imageUrl;
    img.style.maxWidth = '100%';
    document.body.appendChild(img);
}
```

#### 3. 下载文件
```javascript
// 下载文件
function downloadFile(fileId, fileName) {
    const downloadUrl = `/api/file/download/${fileId}?accessUserId=${userId}&accessUserType=consumer`;
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    link.click();
}
```

## 🔒 安全特性

### 1. 文件验证
- 文件大小限制（默认100MB）
- 文件类型验证
- 恶意文件检测

### 2. 权限控制
- 基于用户类型的访问控制
- 文件所有者权限验证
- 管理员特殊权限

### 3. 数据安全
- MD5和SHA256双重哈希验证
- 文件去重机制
- 软删除保护

## 📊 性能优化

### 1. 数据库优化
- 合理的索引设计
- 分页查询优化
- 批量操作支持

### 2. 存储优化
- 文件去重减少存储空间
- 分桶存储管理
- 过期文件自动清理

### 3. 访问优化
- 访问日志异步记录
- 缓存机制（可扩展）
- CDN支持（可扩展）

## 🚀 扩展功能

### 1. 图片处理
- 缩略图生成
- 图片压缩
- 格式转换

### 2. 视频处理
- 视频截图
- 格式转换
- 压缩优化

### 3. 文件分享
- 临时访问链接
- 密码保护
- 过期时间控制

## 📝 注意事项

1. **存储限制**: 使用MySQL存储大文件可能影响数据库性能，建议单文件不超过100MB
2. **备份策略**: 重要文件建议定期备份到外部存储
3. **清理机制**: 定期清理删除的文件和过期日志
4. **监控告警**: 监控存储空间使用情况，及时扩容

## 🔧 配置说明

### 应用配置
```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
  
file-storage:
  max-file-size: 104857600  # 100MB
  allowed-types: 
    - image/*
    - video/*
    - audio/*
    - application/pdf
  temp-file-expire-hours: 24
```

这个文件存储服务提供了完整的OSS模拟功能，支持各种文件操作需求，可以根据实际业务场景进行扩展和优化。

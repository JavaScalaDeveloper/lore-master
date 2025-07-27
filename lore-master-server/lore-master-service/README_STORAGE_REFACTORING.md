# 文件存储服务重构 - 策略模式实现

## 📋 重构概述

本次重构将原本与MySQL存储紧耦合的文件存储服务，重构为基于策略模式的可扩展架构，支持多种存储后端的无缝切换。

## 🎯 重构目标

### 解决的问题
- ❌ **紧耦合**: 原代码直接操作MySQL，难以扩展其他存储方式
- ❌ **不可配置**: 存储方式硬编码，无法动态切换
- ❌ **扩展困难**: 添加新存储方式需要修改核心业务逻辑
- ❌ **测试困难**: 存储逻辑与业务逻辑混合，难以单元测试

### 实现的目标
- ✅ **松耦合**: 业务逻辑与存储实现分离
- ✅ **可配置**: 通过配置文件切换存储策略
- ✅ **易扩展**: 新增存储方式只需实现接口
- ✅ **易测试**: 可以轻松mock存储策略进行测试

## 🏗️ 架构设计

### 设计模式
- **策略模式**: 定义存储算法族，使它们可以互相替换
- **工厂模式**: 根据配置创建相应的存储策略实例
- **依赖注入**: 通过Spring容器管理策略实例

### 核心组件

```
FileStorageService (业务接口)
    ↓
FileStorageServiceImpl (业务实现)
    ↓
StorageStrategyFactory (策略工厂)
    ↓
StorageStrategy (策略接口)
    ↓
├── MySQLStorageStrategy (MySQL实现)
├── AliyunOSSStorageStrategy (阿里云OSS实现)
└── LocalFileStorageStrategy (本地文件实现)
```

## 📁 文件结构

```
lore-master-service/
├── src/main/java/com/lore/master/service/storage/
│   ├── FileStorageService.java                    # 业务服务接口
│   ├── impl/
│   │   └── FileStorageServiceImpl.java            # 业务服务实现
│   ├── strategy/
│   │   ├── StorageStrategy.java                   # 存储策略接口
│   │   └── impl/
│   │       ├── MySQLStorageStrategy.java          # MySQL存储实现
│   │       ├── AliyunOSSStorageStrategy.java      # 阿里云OSS存储实现
│   │       └── LocalFileStorageStrategy.java     # 本地文件存储实现
│   ├── factory/
│   │   └── StorageStrategyFactory.java            # 策略工厂
│   └── config/
│       ├── FileStorageProperties.java             # 配置属性类
│       └── AliyunOSSConfig.java                   # 阿里云OSS配置
└── README_STORAGE_REFACTORING.md                  # 本文档
```

## 🔧 核心接口

### StorageStrategy 接口
```java
public interface StorageStrategy {
    String getStorageType();
    String storeFile(FileStorage fileStorage, byte[] fileData);
    byte[] retrieveFile(FileStorage fileStorage);
    boolean deleteFile(FileStorage fileStorage);
    boolean fileExists(FileStorage fileStorage);
    String generateAccessUrl(FileStorage fileStorage, int expireMinutes);
    String generateDownloadUrl(FileStorage fileStorage, int expireMinutes);
    boolean validateConfiguration();
    StorageStatistics getStorageStatistics();
}
```

### 策略实现类
1. **MySQLStorageStrategy**: 将文件存储在MySQL数据库的LONGBLOB字段中
2. **AliyunOSSStorageStrategy**: 将文件存储到阿里云对象存储服务
3. **LocalFileStorageStrategy**: 将文件存储到本地文件系统（待实现）

## ⚙️ 配置管理

### 配置文件 (application.yml)
```yaml
file-storage:
  # 存储策略类型
  strategy: mysql  # mysql, aliyun-oss, local-file
  
  # 通用配置
  max-file-size: 104857600  # 100MB
  enable-deduplication: true
  enable-access-log: true
  
  # MySQL存储配置
  mysql:
    enable-compression: false
    compression-threshold: 1048576
  
  # 阿里云OSS配置
  aliyun-oss:
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket-name: lore-master-files
```

### 环境切换
```bash
# 使用MySQL存储
java -jar app.jar --spring.profiles.active=mysql-storage

# 使用阿里云OSS存储
java -jar app.jar --spring.profiles.active=aliyun-oss-storage

# 使用本地文件存储
java -jar app.jar --spring.profiles.active=local-file-storage
```

## 🚀 使用示例

### 业务代码无需修改
```java
@Service
public class UserAvatarService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public String uploadAvatar(MultipartFile file, String userId) {
        // 业务代码完全不变，存储策略由配置决定
        FileInfoVO fileInfo = fileStorageService.uploadFile(file, userId, "consumer");
        return fileInfo.getFileId();
    }
}
```

### 动态切换存储策略
```java
@RestController
public class StorageController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/switch-storage")
    public Result<Boolean> switchStorage(@RequestParam String strategyType) {
        boolean success = fileStorageService.switchStorageStrategy(strategyType);
        return Result.success("切换成功", success);
    }
}
```

## 🔄 迁移指南

### 从旧版本迁移

1. **无需修改业务代码**: 所有业务接口保持不变
2. **添加配置**: 在application.yml中添加存储策略配置
3. **数据迁移**: 如需切换存储方式，可使用迁移工具

### 数据迁移示例
```java
@Service
public class StorageMigrationService {
    
    public void migrateFromMySQLToOSS() {
        // 1. 获取所有MySQL存储的文件
        // 2. 逐个迁移到OSS
        // 3. 更新数据库中的文件路径
        // 4. 验证迁移结果
    }
}
```

## 🧪 测试策略

### 单元测试
```java
@Test
public void testFileUpload() {
    // Mock存储策略
    StorageStrategy mockStrategy = Mockito.mock(StorageStrategy.class);
    when(mockStrategy.storeFile(any(), any())).thenReturn("mock-path");
    
    // 测试业务逻辑
    FileInfoVO result = fileStorageService.uploadFile(mockFile, "user123", "consumer");
    assertNotNull(result);
}
```

### 集成测试
```java
@SpringBootTest
@TestPropertySource(properties = "file-storage.strategy=mysql")
public class MySQLStorageIntegrationTest {
    
    @Test
    public void testMySQLStorage() {
        // 测试MySQL存储策略
    }
}
```

## 📊 性能对比

| 存储方式 | 上传性能 | 下载性能 | 存储成本 | 扩展性 |
|---------|---------|---------|---------|--------|
| MySQL | 中等 | 中等 | 高 | 低 |
| 阿里云OSS | 高 | 高 | 低 | 高 |
| 本地文件 | 高 | 高 | 中等 | 中等 |

## 🔮 扩展计划

### 即将支持的存储方式
- **MinIO**: 私有云对象存储
- **腾讯云COS**: 腾讯云对象存储
- **AWS S3**: 亚马逊对象存储
- **七牛云**: 七牛云存储

### 高级功能
- **多存储同步**: 同时存储到多个后端
- **智能分层**: 根据访问频率自动选择存储方式
- **缓存策略**: 热点文件本地缓存
- **CDN集成**: 自动配置CDN加速

## 🎯 最佳实践

### 选择存储策略
- **开发环境**: 使用MySQL存储，简单快速
- **测试环境**: 使用本地文件存储，成本低
- **生产环境**: 使用云存储，性能好、可靠性高

### 配置建议
- **启用去重**: 节省存储空间
- **启用访问日志**: 便于分析和监控
- **合理设置文件大小限制**: 防止滥用
- **定期清理**: 删除过期的临时文件

## 🔧 故障排除

### 常见问题

1. **存储策略切换失败**
   - 检查配置文件是否正确
   - 验证存储后端连接性
   - 查看应用日志

2. **文件上传失败**
   - 检查文件大小限制
   - 验证文件类型是否允许
   - 确认存储空间是否充足

3. **文件下载失败**
   - 检查文件是否存在
   - 验证访问权限
   - 确认存储后端可用性

### 监控指标
- 文件上传成功率
- 文件下载响应时间
- 存储空间使用率
- 错误日志统计

这次重构大大提高了系统的可扩展性和可维护性，为未来的功能扩展奠定了坚实的基础！

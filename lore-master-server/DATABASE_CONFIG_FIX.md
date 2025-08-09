# 数据库配置修复说明

## 问题描述
后端报错：`Table 'lore_consumer.business_course' doesn't exist`

系统在 `lore_consumer` 数据库中查找 `business_course` 表，但该表应该在 `lore_business` 数据库中。

## 问题原因
Repository配置冲突：
- `ConsumerRepositoryConfig` 和 `BusinessRepositoryConfig` 都在管理business repository
- `ConsumerRepositoryConfig` 被标记为 `@Primary`，优先生效
- 导致business实体使用了consumer数据源

## 修复方案

### 1. 修改 BusinessRepositoryConfig.java ✅
```java
@Configuration
@Order(1) // 设置更高的优先级
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.business",
    entityManagerFactoryRef = "businessEntityManagerFactory",
    transactionManagerRef = "businessTransactionManager"
)
@EntityScan(basePackages = "com.lore.master.data.entity.business")
public class BusinessRepositoryConfig {
}
```

### 2. 修改 ConsumerRepositoryConfig.java ✅
```java
@Configuration
@Order(2) // 设置较低的优先级
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.consumer",
    entityManagerFactoryRef = "consumerEntityManagerFactory",
    transactionManagerRef = "consumerTransactionManager"
)
@EntityScan(basePackages = "com.lore.master.data.entity.consumer")
public class ConsumerRepositoryConfig {
}
```

### 3. 修改 DataSourceConfig.java ✅
```java
// consumerEntityManagerFactory 只管理consumer实体
.packages("com.lore.master.data.entity.consumer")
```

### 4. 修改 WebConsumerApplication.java ✅
```java
@Import({
    DataSourceConfig.class, 
    ConsumerRepositoryConfig.class, 
    BusinessRepositoryConfig.class,  // 添加business配置
    StorageRepositoryConfig.class, 
    AdminRepositoryConfig.class
})
```

## 配置说明

### 数据源映射
- **lore_consumer** ← consumer实体 ← ConsumerRepositoryConfig
- **lore_business** ← business实体 ← BusinessRepositoryConfig  
- **lore_admin** ← admin实体 ← AdminRepositoryConfig
- **lore_storage** ← storage实体 ← StorageRepositoryConfig

### 优先级设置
- `@Order(1)` - BusinessRepositoryConfig (高优先级)
- `@Order(2)` - ConsumerRepositoryConfig (低优先级)

这确保business repository使用正确的数据源。

## 验证步骤

### 1. 重启应用
```bash
cd lore-master-server/lore-master-web-consumer
mvn spring-boot:run
```

### 2. 检查启动日志
应该看到：
- 两个数据源都正确初始化
- business repository连接到business数据源
- consumer repository连接到consumer数据源

### 3. 测试API
```bash
curl -X POST http://localhost:8082/api/consumer/course/queryCourseList \
  -H "Content-Type: application/json" \
  -d '{
    "page": 0,
    "size": 10,
    "publishedOnly": true
  }'
```

### 4. 检查数据库查询
应该在 `lore_business.business_course` 表中查询，而不是 `lore_consumer.business_course`。

## 预期结果

修复后：
- ✅ business实体使用business数据源
- ✅ consumer实体使用consumer数据源  
- ✅ API正常返回课程数据
- ✅ 小程序学习页面正常显示

## 注意事项

1. **不要移动数据**：business_course表应该保留在lore_business数据库中
2. **配置优先级**：使用@Order确保正确的配置生效
3. **实体分离**：每个配置类只管理对应的实体包
4. **导入配置**：确保所有需要的配置类都被导入

## 故障排除

如果仍有问题：
1. 检查数据库连接配置
2. 确认lore_business数据库存在business_course表
3. 查看详细的启动日志
4. 验证Repository bean的创建情况

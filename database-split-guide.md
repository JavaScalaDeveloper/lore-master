# 数据库拆分设计说明

## 🎯 **拆分原则**

基于业务特性和性能需求，将原有数据库拆分为两个独立的数据库：

### 1. **管理端数据库 (lore_admin)**
- **用途**: 系统配置、内容管理、数据分析、运营管理
- **特点**: 写入频率低，读取频率中等，数据一致性要求高
- **用户**: 管理员、运营人员、内容编辑

### 2. **C端业务数据库 (lore_business)**
- **用途**: 用户数据、学习记录、测评结果、个性化推荐
- **特点**: 高并发读写，实时性要求高，数据量大
- **用户**: 普通学习用户

## 📊 **表结构分布**

### 管理端数据库 (lore_admin)
| 表名 | 用途 | 特点 |
|------|------|------|
| career_targets | 职业目标管理 | 配置数据，变更频率低 |
| learning_paths | 学习路径管理 | AI生成内容，结构化数据 |
| knowledge_points | 知识点体系 | 内容管理，关联关系复杂 |
| question_bank | 题库管理 | 内容数据，需要版本控制 |
| learning_contents | 学习内容管理 | 多媒体内容，存储量大 |
| system_configs | 系统配置 | 全局配置，高一致性要求 |
| admin_users | 管理员用户 | 权限管理，安全性要求高 |
| operation_logs | 操作日志 | 审计追踪，只增不改 |
| data_statistics | 数据统计 | 汇总数据，定时更新 |

### C端业务数据库 (lore_business)
| 表名 | 用途 | 特点 |
|------|------|------|
| users | 用户基础信息 | 高并发读写，用户量大 |
| user_learning_profiles | 用户学习档案 | 个性化数据，频繁更新 |
| user_knowledge_mastery | 知识点掌握情况 | 学习进度，实时更新 |
| ai_assessments | AI测评记录 | 测评数据，数据量大 |
| personalized_learning_plans | 个性化学习计划 | AI生成，动态调整 |
| learning_behavior_analytics | 学习行为分析 | 行为数据，高频写入 |
| ai_recommendations | AI推荐记录 | 推荐算法，实时计算 |
| vector_embeddings | 向量数据库映射 | 向量数据，高维计算 |
| user_learning_records | 用户学习记录 | 学习轨迹，海量数据 |
| user_achievements | 用户成就 | 激励系统，游戏化元素 |

## 🔄 **跨库关联处理**

### 1. **外键关系处理**
由于数据库拆分，原有的外键约束需要在应用层处理：

```java
// 示例：获取用户学习档案时需要关联职业目标信息
@Service
public class UserLearningService {
    
    @Autowired
    private UserLearningProfileMapper userLearningProfileMapper; // business库
    
    @Autowired
    private CareerTargetMapper careerTargetMapper; // admin库
    
    public UserLearningProfileVO getUserLearningProfile(Long userId) {
        // 1. 从business库获取用户学习档案
        UserLearningProfile profile = userLearningProfileMapper.selectByUserId(userId);
        
        // 2. 从admin库获取职业目标信息
        CareerTarget careerTarget = careerTargetMapper.selectById(profile.getCareerTargetId());
        
        // 3. 组装返回数据
        return UserLearningProfileVO.builder()
            .profile(profile)
            .careerTarget(careerTarget)
            .build();
    }
}
```

### 2. **数据同步策略**
- **配置数据同步**: 管理端的配置变更需要同步到C端缓存
- **统计数据汇总**: C端的用户行为数据定期汇总到管理端
- **内容发布流程**: 管理端内容审核通过后发布到C端

## ⚡ **性能优化策略**

### 1. **管理端优化 (lore_admin)**
- **读写分离**: 配置读多写少的特点，使用读写分离
- **缓存策略**: 配置数据使用Redis缓存，减少数据库压力
- **索引优化**: 针对查询场景建立合适的索引

### 2. **C端优化 (lore_business)**
- **分库分表**: 用户相关表按用户ID分表，学习记录按时间分表
- **读写分离**: 主从复制，读写分离
- **缓存策略**: 用户画像、推荐结果等热点数据缓存
- **异步处理**: 学习行为分析、推荐计算等异步处理

## 🔧 **数据源配置**

### Spring Boot多数据源配置示例：

```yaml
spring:
  datasource:
    admin:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/lore_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      username: root
      password: password
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
    business:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/lore_business?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      username: root
      password: password
      hikari:
        maximum-pool-size: 50
        minimum-idle: 10
```

### MyBatis配置：

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.business")
    public DataSource businessDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @Primary
    public SqlSessionFactory businessSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(businessDataSource());
        return factoryBean.getObject();
    }
    
    @Bean
    public SqlSessionFactory adminSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(adminDataSource());
        return factoryBean.getObject();
    }
}
```

## 📈 **监控和运维**

### 1. **性能监控**
- **QPS监控**: 分别监控两个数据库的QPS
- **慢查询监控**: 定期分析慢查询日志
- **连接池监控**: 监控连接池使用情况

### 2. **容量规划**
- **管理端**: 预估数据增长缓慢，重点关注内容存储
- **C端**: 预估用户增长和学习数据增长，制定分表策略

### 3. **备份策略**
- **管理端**: 全量备份 + 增量备份，重点保护配置数据
- **C端**: 分表备份，用户数据和学习记录分别备份

## 🚀 **部署建议**

### 1. **硬件配置**
- **管理端**: 中等配置，SSD存储，重点保证稳定性
- **C端**: 高配置，SSD存储，重点保证性能

### 2. **网络架构**
- **内网隔离**: 管理端和C端数据库网络隔离
- **访问控制**: 严格的IP白名单和权限控制

### 3. **扩展性**
- **垂直扩展**: 优先考虑硬件升级
- **水平扩展**: C端数据库支持分库分表扩展

这种拆分方案能够有效应对不同业务场景的性能需求，同时保证数据的安全性和一致性。

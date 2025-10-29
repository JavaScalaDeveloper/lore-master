# 通学万卷 - 后端服务启动说明

## 启动脚本说明

本项目为每个Spring Boot后端服务提供了启动脚本，方便快速启动各个服务：

1. **管理端后端服务** (端口: 8080)
   - Linux/Mac: `./start-admin.sh`
   - Windows: `start-admin.bat`

2. **B端后端服务** (端口: 8081)
   - Linux/Mac: `./start-business.sh`
   - Windows: `start-business.bat`

3. **C端后端服务** (端口: 8082)
   - Linux/Mac: `./start-consumer.sh`
   - Windows: `start-consumer.bat`

## 手动启动方式

你也可以使用Maven命令手动启动各个服务：

```bash
# 启动管理端后端服务
mvn -pl lore-master-web-admin spring-boot:run

# 启动B端后端服务
mvn -pl lore-master-web-business spring-boot:run

# 启动C端后端服务
mvn -pl lore-master-web-consumer spring-boot:run
```

## 环境要求

- Java 17+
- Maven 3.6+

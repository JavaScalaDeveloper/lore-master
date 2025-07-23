# Docker 相关命令与配置

本目录用于存放与 Docker 相关的常用命令、配置文件和说明。

## MySQL8 一键启动（推荐 docker-compose）

已提供 `docker-compose.yml`，可一键启动 MySQL8 容器，初始化 root 密码为 123456，并将数据持久化到本地 `mysql-data` 目录。

### 启动命令

```shell
docker-compose up -d
```

### 关闭并移除容器

```shell
docker-compose down
```

### 目录说明
- `docker-compose.yml`：MySQL8 服务编排文件
- `mysql-data/`：MySQL 持久化数据目录（自动生成）

### 连接信息
- 主机端口：3306
- 用户名：root
- 密码：123456

如需自定义配置，请修改 `docker-compose.yml` 文件。 
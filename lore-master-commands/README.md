# commands 命令脚本目录

本目录用于存放本项目相关的常用命令脚本及说明，方便开发、部署、运维等场景复用。

## 目录用途
- 统一管理 docker、shell、java -jar 等常用命令
- 提供标准化的启动、构建、部署、运维脚本
- 便于团队成员查阅和复用

## 示例内容

- `docker/` 目录：存放 Dockerfile、docker-compose.yml 及常用 docker 命令脚本
- `shell/`  目录：存放常用 shell 脚本（如一键启动、备份、日志清理等）
- `java/`   目录：存放 java -jar 启动命令、JVM 参数说明等

## 约定
- 每个子目录建议包含对应的 README.md，说明脚本用途和用法
- 脚本需注明适用环境（如 Linux/Windows）及注意事项

---

如有新命令或脚本，请补充到本目录并完善说明。 
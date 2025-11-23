#!/bin/bash
# 启动MySQL8容器，初始化root密码为123456
# 需要先拉取mysql:8镜像
# 使用方法：bash mysql8-docker-run.sh

docker run -d \
  --name mysql8-demo \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -p 3306:3306 \
  -v $(pwd)/mysql-data:/var/lib/mysql \
  mysql:8

echo "MySQL8容器已启动，root密码为123456，端口映射为3306。" 
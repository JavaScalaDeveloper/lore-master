#!/bin/bash
# set_static_ip_simple.sh - 简化版静态IP设置脚本
# 示例：sh set_static_ip_simple.sh 192.168.1.161/24

if [ $# -lt 1 ]; then
    echo "用法: $0 <IP地址/CIDR>"
    echo "示例: $0 192.168.1.100/24"
    exit 1
fi

IP_CIDR="$1"
GATEWAY="${2:-192.168.1.1}"
DNS="${3:-8.8.8.8}"

# 获取活动连接名称
CONN_NAME=$(nmcli connection show --active | grep -v "lo\|--" | grep -E "wifi|ethernet" | head -1 | awk '{print $1}')

if [ -z "$CONN_NAME" ]; then
    echo "错误: 未找到活动的网络连接!"
    exit 1
fi

echo "配置连接: $CONN_NAME"
echo "设置静态IP: $IP_CIDR"

# 执行配置
sudo nmcli connection modify "$CONN_NAME" ipv4.addresses "$IP_CIDR" ipv4.gateway "$GATEWAY" ipv4.method manual
sudo nmcli connection modify "$CONN_NAME" ipv4.dns "$DNS"
sudo nmcli connection down "$CONN_NAME"
sudo nmcli connection up "$CONN_NAME"

echo "配置完成!"

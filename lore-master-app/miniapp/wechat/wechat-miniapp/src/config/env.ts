/**
 * 环境配置文件
 * 用于手动切换开发环境
 */

import type { Environment } from './api';

/**
 * 手动设置当前环境
 * 在开发过程中可以通过修改这个值来切换环境
 * 
 * 可选值：
 * - 'development': 开发环境 (localhost:8082)
 * - 'test': 测试环境 (test-api.loremaster.com)
 * - 'production': 生产环境 (api.loremaster.com)
 */
export const MANUAL_ENV: Environment = 'development';

/**
 * 是否启用手动环境设置
 * true: 使用 MANUAL_ENV 设置的环境
 * false: 根据 process.env.NODE_ENV 自动判断
 */
export const USE_MANUAL_ENV = true;

/**
 * 开发环境配置
 * 可以根据实际情况修改这些配置
 */
export const DEV_CONFIG = {
  // 本地开发服务器地址
  LOCAL_API_URL: process.env.TARO_APP_API_BASE_URL || 'http://localhost:8082',
  
  // 远程开发环境服务器地址
  REMOTE_API_URL: process.env.TARO_APP_DEV_API_URL || 'https://ly112978940c.vicp.fun',
  
  // 局域网开发服务器地址（用于真机调试）
  // 请将 192.168.1.100 替换为你的电脑IP地址
  NETWORK_API_URL: 'http://192.168.1.100:8082',
  
  // 一键切换配置：
  // - false: 使用本地地址 (localhost:8082)
  // - true: 使用远程地址 (ly112978940c.vicp.fun)
  USE_REMOTE_URL: true,
  
  // 是否使用局域网地址（用于真机调试）
  USE_NETWORK_URL: false,
};

/**
 * 获取开发环境的API地址
 */
export function getDevApiUrl(): string {
  // 如果启用局域网调试，优先使用局域网地址
  if (DEV_CONFIG.USE_NETWORK_URL) {
    console.log('使用局域网地址：', DEV_CONFIG.NETWORK_API_URL);
    return DEV_CONFIG.NETWORK_API_URL;
  }
  
  // 一键切换：本地 vs 远程
  if (DEV_CONFIG.USE_REMOTE_URL) {
    console.log('使用远程地址：', DEV_CONFIG.REMOTE_API_URL);
    return DEV_CONFIG.REMOTE_API_URL;
  } else {
    console.log('使用本地地址：', DEV_CONFIG.LOCAL_API_URL);
    return DEV_CONFIG.LOCAL_API_URL;
  }
}

/**
 * 环境说明
 */
export const ENV_DESCRIPTIONS = {
  development: '开发环境 - 本地服务器',
  test: '测试环境 - 测试服务器',
  production: '生产环境 - 正式服务器',
};

/**
 * 打印当前环境信息
 */
export function printEnvInfo(currentEnv: Environment, baseUrl: string): void {
  console.log('=== 环境配置信息 ===');
  console.log(`当前环境: ${currentEnv}`);
  console.log(`环境描述: ${ENV_DESCRIPTIONS[currentEnv]}`);
  console.log(`当前API地址: ${baseUrl}`);
  console.log(`手动环境: ${USE_MANUAL_ENV ? '启用' : '禁用'}`);
  if (USE_MANUAL_ENV) {
    console.log(`手动设置: ${MANUAL_ENV}`);
  }
  if (currentEnv === 'development') {
    console.log('--- 开发环境配置 ---');
    console.log(`本地地址: ${DEV_CONFIG.LOCAL_API_URL}`);
    console.log(`远程地址: ${DEV_CONFIG.REMOTE_API_URL}`);
    console.log(`局域网地址: ${DEV_CONFIG.NETWORK_API_URL}`);
    console.log(`使用远程: ${DEV_CONFIG.USE_REMOTE_URL ? '是' : '否'} (一键切换)`);
    console.log(`使用局域网: ${DEV_CONFIG.USE_NETWORK_URL ? '是' : '否'}`);
  }
  console.log('==================');
}

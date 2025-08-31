/**
 * API配置文件
 * 统一管理后端API域名和环境配置
 */

import { MANUAL_ENV, USE_MANUAL_ENV, getDevApiUrl, printEnvInfo } from './env';

// 环境类型定义
export type Environment = 'development' | 'production' | 'test';

// API配置接口
interface ApiConfig {
  baseUrl: string;
  timeout: number;
  enableLog: boolean;
}

// 不同环境的配置
const configs: Record<Environment, ApiConfig> = {
  // 开发环境
  development: {
    baseUrl: getDevApiUrl(), // 从环境配置获取开发环境地址
    timeout: 10000,
    enableLog: true,
  },

  // 生产环境
  production: {
    baseUrl: process.env.TARO_APP_API_BASE_URL || 'https://api.loremaster.com', // 从环境变量或默认值获取生产环境地址
    timeout: 15000,
    enableLog: false,
  },

  // 测试环境
  test: {
    baseUrl: process.env.TARO_APP_API_BASE_URL || 'https://test-api.loremaster.com', // 从环境变量或默认值获取测试环境地址
    timeout: 12000,
    enableLog: true,
  },
};

/**
 * 获取当前环境
 * 可以通过环境变量或其他方式判断
 */
function getCurrentEnvironment(): Environment {
  // 如果启用了手动环境设置，直接返回手动设置的环境
  if (USE_MANUAL_ENV) {
    return MANUAL_ENV;
  }

  // 在小程序中，可以通过以下方式判断环境：
  // 1. 通过编译时的环境变量
  // 2. 通过小程序的版本类型
  // 3. 通过手动配置

  // 这里使用简单的判断逻辑，实际项目中可以根据需要调整
  if (process.env.NODE_ENV === 'production') {
    return 'production';
  } else if (process.env.NODE_ENV === 'test') {
    return 'test';
  } else {
    return 'development';
  }
}

// 当前环境
export const CURRENT_ENV: Environment = getCurrentEnvironment();

// 当前环境的配置
export const API_CONFIG: ApiConfig = configs[CURRENT_ENV];

// 打印环境信息（仅在开发环境）
if (API_CONFIG.enableLog) {
  printEnvInfo(CURRENT_ENV, API_CONFIG.baseUrl);
}

// 导出常用的API地址
export const API_ENDPOINTS = {
  // 用户相关
  USER_LOGIN: `${API_CONFIG.baseUrl}/api/user/login`,
  USER_INFO: `${API_CONFIG.baseUrl}/api/user/info`,
  USER_AVATAR_UPLOAD: `${API_CONFIG.baseUrl}/api/user/avatar/upload`,

  // 课程相关
  COURSES_LIST: `${API_CONFIG.baseUrl}/api/courses/list`,
  COURSE_DETAIL: `${API_CONFIG.baseUrl}/api/courses/detail`,
  COURSE_QUERY_LIST: `${API_CONFIG.baseUrl}/business/course/queryCourseList`,
  COURSE_SEARCH: `${API_CONFIG.baseUrl}/business/course/searchCourses`,
  COURSE_BY_CODE: `${API_CONFIG.baseUrl}/business/course/getCourseByCode`,
  COURSE_BY_ID: `${API_CONFIG.baseUrl}/business/course/getCourseById`,

  // 学习相关
  LEARNING_PROGRESS: `${API_CONFIG.baseUrl}/api/learning/progress`,
  LEARNING_STATS: `${API_CONFIG.baseUrl}/api/learning/stats`,

  // 聊天相关
  CHAT_SEND_MESSAGE: `${API_CONFIG.baseUrl}/api/chat/sendMessageStream`,
  CHAT_UPLOAD_FILE: `${API_CONFIG.baseUrl}/api/chat/upload`,

  // 学习目标相关
  SKILL_CATALOG_TREE: `${API_CONFIG.baseUrl}/api/consumer/skill-catalog/tree`,
  USER_LEARNING_GOAL: `${API_CONFIG.baseUrl}/api/user/learning-goal`,
  USER_LEARNING_GOAL_CURRENT: `${API_CONFIG.baseUrl}/api/user/learning-goal/current`,
};

// 导出基础URL，用于拼接相对路径
export const BASE_URL = API_CONFIG.baseUrl;

/**
 * 构建完整的API URL
 * @param path API路径
 * @returns 完整的URL
 */
export function buildApiUrl(path: string): string {
  // 如果path已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // 确保path以/开头
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  
  return `${API_CONFIG.baseUrl}${normalizedPath}`;
}

/**
 * 日志输出函数
 * @param message 日志信息
 * @param data 附加数据
 */
export function apiLog(message: string, data?: any): void {
  if (API_CONFIG.enableLog) {
    console.log(`[API] ${message}`, data || '');
  }
}

/**
 * 获取请求头
 * @param token 用户token
 * @returns 请求头对象
 */
export function getApiHeaders(token?: string): Record<string, string> {
  const headers: Record<string, string> = {
    'content-type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
}

// 导出配置信息（用于调试）
export function getConfigInfo() {
  return {
    environment: CURRENT_ENV,
    baseUrl: API_CONFIG.baseUrl,
    timeout: API_CONFIG.timeout,
    enableLog: API_CONFIG.enableLog,
  };
}

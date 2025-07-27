/**
 * API配置文件
 * 统一管理所有API域名和路径
 */

// 环境信息
export const ENV_INFO = {
  NODE_ENV: process.env.NODE_ENV || 'development',
  IS_DEVELOPMENT: process.env.NODE_ENV === 'development',
  IS_PRODUCTION: process.env.NODE_ENV === 'production',
};

// API域名配置
export const DOMAINS = {
  ADMIN_API: process.env.REACT_APP_ADMIN_API || 'http://localhost:8080',
  BUSINESS_API: process.env.REACT_APP_BUSINESS_API || 'http://localhost:8081', 
  CONSUMER_API: process.env.REACT_APP_CONSUMER_API || 'http://localhost:8082',
  WEB_FRONTEND: process.env.REACT_APP_WEB_FRONTEND || 'http://localhost:3001',
};

// API路径配置
export const API_PATHS = {
  ADMIN: {
    AUTH: {
      LOGIN: '/api/admin/auth/login',
    },
    USERS: {
      PAGE: '/api/admin/users/page',
    },
    CAREER_TARGETS: {
      PAGE: '/api/admin/career-targets/page',
    },
    KNOWLEDGE: {
      PAGE: '/api/admin/knowledge-points/page',
    },
  },
  CONSUMER: {
    AUTH: {
      LOGIN: '/api/user/login',
      LOGOUT: '/api/user/logout',
      REFRESH_TOKEN: '/api/user/refresh-token',
    },
    REGISTER: {
      REGISTER: '/api/user/register',
      SEND_CODE: '/api/user/register/send-code',
      CHECK: '/api/user/register/check',
    },
    PROFILE: {
      GET: '/api/user/profile',
    },
  },
  BUSINESS: {
    AUTH: {
      LOGIN: '/api/user/login',
    },
  },
};

// CORS配置
export const CORS_CONFIG = {
  ALLOWED_ORIGINS: [
    'http://localhost:3000',
    'http://localhost:3001',
    'http://localhost:3002',
    'https://www.loremaster.com',
    'https://test.loremaster.com'
  ],
  ALLOWED_METHODS: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  ALLOWED_HEADERS: ['Content-Type', 'Authorization', 'X-Requested-With'],
  ALLOW_CREDENTIALS: true,
  MAX_AGE: 3600,
};

// 请求配置
export const REQUEST_CONFIG = {
  TIMEOUT: 10000, // 10秒超时
  RETRY_COUNT: 2, // 重试2次
  RETRY_DELAY: 1000, // 重试延迟1秒
};

// 构建完整的API地址
export const buildApiUrl = (domain: string, path: string): string => {
  return `${domain}${path}`;
};

// 获取API域名
export const getApiDomain = (service: 'admin' | 'business' | 'consumer'): string => {
  switch (service) {
    case 'admin':
      return DOMAINS.ADMIN_API;
    case 'business':
      return DOMAINS.BUSINESS_API;
    case 'consumer':
      return DOMAINS.CONSUMER_API;
    default:
      throw new Error(`未知的服务类型: ${service}`);
  }
};

// 开发环境调试信息
if (ENV_INFO.IS_DEVELOPMENT) {
  console.log('🌐 小程序API配置信息:', {
    环境: ENV_INFO.NODE_ENV,
    管理端API: DOMAINS.ADMIN_API,
    业务端API: DOMAINS.BUSINESS_API,
    C端API: DOMAINS.CONSUMER_API,
    前端地址: DOMAINS.WEB_FRONTEND,
    '环境变量CONSUMER_API': process.env.REACT_APP_CONSUMER_API,
  });
}

/**
 * API配置文件
 * 统一管理所有API地址和配置
 */

// 环境变量配置
const isDevelopment = process.env.NODE_ENV === 'development';
const isProduction = process.env.NODE_ENV === 'production';

// 基础域名配置
const BASE_DOMAINS = {
  // 开发环境
  development: {
    ADMIN_API: 'http://localhost:8080',      // 管理端API
    BUSINESS_API: 'http://localhost:8081',   // 业务端API
    CONSUMER_API: 'http://localhost:8082',   // C端用户API
    WEB_ADMIN: 'http://localhost:3001',      // 管理端前端
    WEB_BUSINESS: 'http://localhost:3002',   // B端前端
    WEB_CONSUMER: 'http://localhost:3003',   // C端前端
    WEB_MINIAPP: 'http://localhost:3004',    // 小程序前端
  },
  // 生产环境
  production: {
    ADMIN_API: process.env.REACT_APP_ADMIN_API || 'https://admin-api.loremaster.com',
    BUSINESS_API: process.env.REACT_APP_BUSINESS_API || 'https://business-api.loremaster.com',
    CONSUMER_API: process.env.REACT_APP_CONSUMER_API || 'https://consumer-api.loremaster.com',
    WEB_ADMIN: process.env.REACT_APP_WEB_ADMIN || 'https://admin.loremaster.com',
    WEB_BUSINESS: process.env.REACT_APP_WEB_BUSINESS || 'https://business.loremaster.com',
    WEB_CONSUMER: process.env.REACT_APP_WEB_CONSUMER || 'https://www.loremaster.com',
    WEB_MINIAPP: process.env.REACT_APP_WEB_MINIAPP || 'https://miniapp.loremaster.com',
  },
  // 测试环境
  test: {
    ADMIN_API: process.env.REACT_APP_ADMIN_API || 'http://test-admin-api.loremaster.com',
    BUSINESS_API: process.env.REACT_APP_BUSINESS_API || 'http://test-business-api.loremaster.com',
    CONSUMER_API: process.env.REACT_APP_CONSUMER_API || 'http://test-consumer-api.loremaster.com',
    WEB_ADMIN: process.env.REACT_APP_WEB_ADMIN || 'http://test-admin.loremaster.com',
    WEB_BUSINESS: process.env.REACT_APP_WEB_BUSINESS || 'http://test-business.loremaster.com',
    WEB_CONSUMER: process.env.REACT_APP_WEB_CONSUMER || 'http://test.loremaster.com',
    WEB_MINIAPP: process.env.REACT_APP_WEB_MINIAPP || 'http://test-miniapp.loremaster.com',
  }
};

// 获取当前环境的域名配置
const getCurrentDomains = () => {
  if (isProduction) {
    return BASE_DOMAINS.production;
  } else if (process.env.NODE_ENV === 'test') {
    return BASE_DOMAINS.test;
  } else {
    return BASE_DOMAINS.development;
  }
};

// 当前环境的域名配置
export const DOMAINS = getCurrentDomains();

// API路径配置
export const API_PATHS = {
  // 管理端API路径
  ADMIN: {
    AUTH: {
      LOGIN: '/api/admin/auth/login',
      LOGOUT: '/api/admin/auth/logout',
      REFRESH: '/api/admin/auth/refresh',
    },
    USERS: {
      PAGE: '/api/admin/users/page',
      CREATE: '/api/admin/users/create',
      UPDATE: '/api/admin/users/update',
      DELETE: '/api/admin/users/delete',
    },
    CAREER_TARGETS: {
      PAGE: '/api/admin/career-targets/page',
      CREATE: '/api/admin/career-targets/create',
      UPDATE: '/api/admin/career-targets/update',
      DELETE: '/api/admin/career-targets/delete',
    },
    KNOWLEDGE: {
      PAGE: '/api/admin/knowledge-points/page',
      CREATE: '/api/admin/knowledge-points/create',
      UPDATE: '/api/admin/knowledge-points/update',
      DELETE: '/api/admin/knowledge-points/delete',
    }
  },
  
  // C端用户API路径
  CONSUMER: {
    AUTH: {
      LOGIN: '/api/user/login',
      LOGOUT: '/api/user/logout',
      REFRESH: '/api/user/refresh',
    },
    REGISTER: {
      REGISTER: '/api/user/register',
      CHECK: '/api/user/register/check',
      SEND_CODE: '/api/user/register/send-code',
    },
    PROFILE: {
      GET: '/api/user/profile',
      UPDATE: '/api/user/profile/update',
    },
    STUDY: {
      PROGRESS: '/api/user/study/progress',
      RECORD: '/api/user/study/record',
    }
  },
  
  // 业务端API路径
  BUSINESS: {
    STATS: {
      OVERVIEW: '/api/business/stats/overview',
      USERS: '/api/business/stats/users',
    },
    CONTENT: {
      LIST: '/api/business/content/list',
      CREATE: '/api/business/content/create',
      UPDATE: '/api/business/content/update',
    }
  }
};

// 完整API地址构建函数
export const buildApiUrl = (domain: keyof typeof DOMAINS, path: string): string => {
  return `${DOMAINS[domain]}${path}`;
};

// 常用API地址快捷方式
export const API_URLS = {
  // 管理端API
  ADMIN_LOGIN: buildApiUrl('ADMIN_API', API_PATHS.ADMIN.AUTH.LOGIN),
  ADMIN_USERS_PAGE: buildApiUrl('ADMIN_API', API_PATHS.ADMIN.USERS.PAGE),
  ADMIN_CAREER_TARGETS_PAGE: buildApiUrl('ADMIN_API', API_PATHS.ADMIN.CAREER_TARGETS.PAGE),
  ADMIN_KNOWLEDGE_PAGE: buildApiUrl('ADMIN_API', API_PATHS.ADMIN.KNOWLEDGE.PAGE),
  
  // C端用户API
  CONSUMER_LOGIN: buildApiUrl('CONSUMER_API', API_PATHS.CONSUMER.AUTH.LOGIN),
  CONSUMER_REGISTER: buildApiUrl('CONSUMER_API', API_PATHS.CONSUMER.REGISTER.REGISTER),
  CONSUMER_REGISTER_CHECK: buildApiUrl('CONSUMER_API', API_PATHS.CONSUMER.REGISTER.CHECK),
  CONSUMER_REGISTER_SEND_CODE: buildApiUrl('CONSUMER_API', API_PATHS.CONSUMER.REGISTER.SEND_CODE),
};

// CORS配置
export const CORS_CONFIG = {
  // 允许的前端域名
  ALLOWED_ORIGINS: [
    DOMAINS.WEB_ADMIN,
    DOMAINS.WEB_BUSINESS,
    DOMAINS.WEB_CONSUMER,
    DOMAINS.WEB_MINIAPP,
    'http://localhost:3000',  // 开发环境备用端口
  ],
  // 允许的请求方法
  ALLOWED_METHODS: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  // 允许的请求头
  ALLOWED_HEADERS: ['Content-Type', 'Authorization', 'X-Requested-With'],
  // 是否允许携带凭证
  ALLOW_CREDENTIALS: true,
  // 预检请求缓存时间（秒）
  MAX_AGE: 3600,
};

// 请求配置
export const REQUEST_CONFIG = {
  // 请求超时时间（毫秒）
  TIMEOUT: 10000,
  // 重试次数
  RETRY_COUNT: 3,
  // 重试间隔（毫秒）
  RETRY_DELAY: 1000,
};

// 导出环境信息
export const ENV_INFO = {
  NODE_ENV: process.env.NODE_ENV,
  IS_DEVELOPMENT: isDevelopment,
  IS_PRODUCTION: isProduction,
  IS_TEST: process.env.NODE_ENV === 'test',
};

// 调试信息（仅开发环境）
if (isDevelopment) {
  console.log('🔧 API配置信息:', {
    环境: process.env.NODE_ENV,
    域名配置: DOMAINS,
    CORS配置: CORS_CONFIG,
  });
}

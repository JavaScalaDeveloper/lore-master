/**
 * HTTP请求工具类
 * 统一管理API请求，支持拦截器、错误处理、重试等功能
 */

import { DOMAINS, REQUEST_CONFIG, ENV_INFO } from '../config/api';

// 请求选项接口
interface RequestOptions extends RequestInit {
  timeout?: number;
  retry?: number;
  retryDelay?: number;
  baseURL?: string;
}

// 响应数据接口
interface ApiResponse<T = any> {
  code?: number;
  success?: boolean;
  message?: string;
  data?: T;
}

// 请求拦截器类型
type RequestInterceptor = (config: RequestOptions) => RequestOptions | Promise<RequestOptions>;
type ResponseInterceptor = (response: Response) => Response | Promise<Response>;
type ErrorInterceptor = (error: Error) => Error | Promise<Error>;

class HttpClient {
  private requestInterceptors: RequestInterceptor[] = [];
  private responseInterceptors: ResponseInterceptor[] = [];
  private errorInterceptors: ErrorInterceptor[] = [];

  constructor() {
    // 添加默认请求拦截器
    this.addRequestInterceptor((config) => {
      // 添加默认headers
      config.headers = {
        'Content-Type': 'application/json',
        ...config.headers,
      };

      // 添加认证token
      const token = localStorage.getItem('adminToken') || localStorage.getItem('userToken');
      const tokenType = localStorage.getItem('tokenType') || 'Bearer';
      if (token) {
        config.headers = {
          ...config.headers,
          'Authorization': `${tokenType} ${token}`,
        };
      }

      return config;
    });

    // 添加默认响应拦截器
    this.addResponseInterceptor(async (response) => {
      // 处理401未授权
      if (response.status === 401) {
        // 清除token
        localStorage.removeItem('adminToken');
        localStorage.removeItem('userToken');
        localStorage.removeItem('tokenType');
        
        // 跳转到登录页
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }

      return response;
    });
  }

  // 添加请求拦截器
  addRequestInterceptor(interceptor: RequestInterceptor) {
    this.requestInterceptors.push(interceptor);
  }

  // 添加响应拦截器
  addResponseInterceptor(interceptor: ResponseInterceptor) {
    this.responseInterceptors.push(interceptor);
  }

  // 添加错误拦截器
  addErrorInterceptor(interceptor: ErrorInterceptor) {
    this.errorInterceptors.push(interceptor);
  }

  // 执行请求拦截器
  private async executeRequestInterceptors(config: RequestOptions): Promise<RequestOptions> {
    let finalConfig = config;
    for (const interceptor of this.requestInterceptors) {
      finalConfig = await interceptor(finalConfig);
    }
    return finalConfig;
  }

  // 执行响应拦截器
  private async executeResponseInterceptors(response: Response): Promise<Response> {
    let finalResponse = response;
    for (const interceptor of this.responseInterceptors) {
      finalResponse = await interceptor(finalResponse);
    }
    return finalResponse;
  }

  // 执行错误拦截器
  private async executeErrorInterceptors(error: Error): Promise<Error> {
    let finalError = error;
    for (const interceptor of this.errorInterceptors) {
      finalError = await interceptor(finalError);
    }
    return finalError;
  }

  // 带超时的fetch
  private async fetchWithTimeout(url: string, options: RequestOptions): Promise<Response> {
    const timeout = options.timeout || REQUEST_CONFIG.TIMEOUT;
    
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);

    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  }

  // 带重试的请求
  private async requestWithRetry(url: string, options: RequestOptions): Promise<Response> {
    const maxRetries = options.retry || REQUEST_CONFIG.RETRY_COUNT;
    const retryDelay = options.retryDelay || REQUEST_CONFIG.RETRY_DELAY;

    let lastError: Error;

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        const response = await this.fetchWithTimeout(url, options);
        
        // 如果是网络错误或5xx错误，进行重试
        if (!response.ok && response.status >= 500 && attempt < maxRetries) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return response;
      } catch (error) {
        lastError = error as Error;
        
        // 如果是最后一次尝试，直接抛出错误
        if (attempt === maxRetries) {
          break;
        }

        // 等待后重试
        if (retryDelay > 0) {
          await new Promise(resolve => setTimeout(resolve, retryDelay));
        }

        if (ENV_INFO.IS_DEVELOPMENT) {
          console.warn(`请求重试 ${attempt + 1}/${maxRetries}:`, url, error);
        }
      }
    }

    throw lastError!;
  }

  // 核心请求方法
  async request<T = any>(url: string, options: RequestOptions = {}): Promise<ApiResponse<T>> {
    try {
      // 处理baseURL
      const fullUrl = options.baseURL ? `${options.baseURL}${url}` : url;
      
      // 执行请求拦截器
      const finalOptions = await this.executeRequestInterceptors(options);

      // 发送请求
      let response = await this.requestWithRetry(fullUrl, finalOptions);

      // 执行响应拦截器
      response = await this.executeResponseInterceptors(response);

      // 解析响应
      const data = await response.json();

      if (ENV_INFO.IS_DEVELOPMENT) {
        console.log(`🌐 API请求:`, {
          url: fullUrl,
          method: options.method || 'GET',
          status: response.status,
          data,
        });
      }

      return data;
    } catch (error) {
      // 执行错误拦截器
      const finalError = await this.executeErrorInterceptors(error as Error);
      
      if (ENV_INFO.IS_DEVELOPMENT) {
        console.error(`❌ API请求失败:`, {
          url,
          error: finalError,
        });
      }

      throw finalError;
    }
  }

  // GET请求
  async get<T = any>(url: string, options: RequestOptions = {}): Promise<ApiResponse<T>> {
    return this.request<T>(url, { ...options, method: 'GET' });
  }

  // POST请求
  async post<T = any>(url: string, data?: any, options: RequestOptions = {}): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...options,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  // PUT请求
  async put<T = any>(url: string, data?: any, options: RequestOptions = {}): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...options,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  // DELETE请求
  async delete<T = any>(url: string, options: RequestOptions = {}): Promise<ApiResponse<T>> {
    return this.request<T>(url, { ...options, method: 'DELETE' });
  }
}

// 创建默认实例
export const http = new HttpClient();

// 创建不同服务的HTTP客户端
export const adminApi = new HttpClient();
adminApi.addRequestInterceptor((config) => ({
  ...config,
  baseURL: DOMAINS.ADMIN_API,
}));

export const consumerApi = new HttpClient();
consumerApi.addRequestInterceptor((config) => ({
  ...config,
  baseURL: DOMAINS.CONSUMER_API,
}));

export const businessApi = new HttpClient();
businessApi.addRequestInterceptor((config) => ({
  ...config,
  baseURL: DOMAINS.BUSINESS_API,
}));

// 导出默认实例
export default http;

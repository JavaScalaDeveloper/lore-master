/**
 * API配置使用示例
 * 展示如何在项目中使用统一的API配置
 */

import { request } from '@tarojs/taro';
import { API_ENDPOINTS, buildApiUrl, getApiHeaders, apiLog, getConfigInfo } from './api';

// 示例1: 使用预定义的API端点
export async function getUserInfo(token: string) {
  try {
    apiLog('获取用户信息');
    
    const response = await request({
      url: API_ENDPOINTS.USER_INFO,
      method: 'GET',
      header: getApiHeaders(token)
    });
    
    return response.data;
  } catch (error) {
    apiLog('获取用户信息失败', error);
    throw error;
  }
}

// 示例2: 使用buildApiUrl构建自定义URL
export async function getCustomData(path: string, token?: string) {
  try {
    const url = buildApiUrl(path);
    apiLog('调用自定义API', { url });
    
    const response = await request({
      url,
      method: 'GET',
      header: getApiHeaders(token)
    });
    
    return response.data;
  } catch (error) {
    apiLog('调用自定义API失败', error);
    throw error;
  }
}

// 示例3: 上传文件
export async function uploadFile(filePath: string, token: string) {
  try {
    apiLog('上传文件', { filePath });
    
    // 使用Taro的uploadFile API
    const { uploadFile } = await import('@tarojs/taro');
    
    return new Promise((resolve, reject) => {
      uploadFile({
        url: API_ENDPOINTS.USER_AVATAR_UPLOAD,
        filePath,
        name: 'file',
        header: getApiHeaders(token),
        success: (res) => {
          apiLog('文件上传成功', res);
          resolve(res);
        },
        fail: (error) => {
          apiLog('文件上传失败', error);
          reject(error);
        }
      });
    });
  } catch (error) {
    apiLog('上传文件异常', error);
    throw error;
  }
}

// 示例4: 处理相对路径的图片URL
export function processImageUrl(imageUrl: string): string {
  if (!imageUrl) return '';
  
  // 如果是相对路径，转换为完整URL
  if (imageUrl.startsWith('/api/') || imageUrl.startsWith('/uploads/')) {
    return buildApiUrl(imageUrl);
  }
  
  // 如果已经是完整URL，直接返回
  return imageUrl;
}

// 示例5: 批量API调用
export async function batchApiCall(token: string) {
  try {
    apiLog('开始批量API调用');
    
    const [userInfo, coursesList, learningStats] = await Promise.all([
      request({
        url: API_ENDPOINTS.USER_INFO,
        method: 'GET',
        header: getApiHeaders(token)
      }),
      request({
        url: API_ENDPOINTS.COURSES_LIST,
        method: 'GET',
        header: getApiHeaders(token)
      }),
      request({
        url: API_ENDPOINTS.LEARNING_STATS,
        method: 'GET',
        header: getApiHeaders(token)
      })
    ]);
    
    apiLog('批量API调用成功');
    
    return {
      userInfo: userInfo.data,
      coursesList: coursesList.data,
      learningStats: learningStats.data
    };
  } catch (error) {
    apiLog('批量API调用失败', error);
    throw error;
  }
}

// 示例6: 错误处理和重试
export async function apiWithRetry(
  url: string, 
  options: any, 
  maxRetries: number = 3
): Promise<any> {
  let lastError: any;
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      apiLog(`API调用尝试 ${i + 1}/${maxRetries}`, { url });
      
      const response = await request({
        url,
        ...options
      });
      
      apiLog('API调用成功', { url, attempt: i + 1 });
      return response.data;
    } catch (error) {
      lastError = error;
      apiLog(`API调用失败 ${i + 1}/${maxRetries}`, { url, error });
      
      // 如果不是最后一次尝试，等待一段时间后重试
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
      }
    }
  }
  
  throw lastError;
}

// 示例7: 打印配置信息（调试用）
export function debugConfig() {
  const config = getConfigInfo();
  console.log('=== API配置调试信息 ===');
  console.log('当前环境:', config.environment);
  console.log('API地址:', config.baseUrl);
  console.log('请求超时:', config.timeout);
  console.log('日志启用:', config.enableLog);
  console.log('===================');
  
  console.log('=== API端点列表 ===');
  Object.entries(API_ENDPOINTS).forEach(([key, value]) => {
    console.log(`${key}: ${value}`);
  });
  console.log('================');
}

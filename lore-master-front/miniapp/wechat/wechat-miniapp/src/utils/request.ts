import Taro from '@tarojs/taro'
import { API_CONFIG } from '../config/api'

// 请求配置
const BASE_URL = API_CONFIG.baseUrl

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
}

interface ApiResponse<T = any> {
  success: boolean
  message: string
  data: T
  code?: number
}

// 请求拦截器
const requestInterceptor = (options: RequestOptions) => {
  // 添加基础URL
  if (!options.url.startsWith('http')) {
    options.url = BASE_URL + options.url
  }

  // 获取token并添加到请求头
  let token = ''
  try {
    token = Taro.getStorageSync('token')
  } catch (error) {
    console.warn('获取token失败:', error)
  }

  // 添加默认请求头
  options.header = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }), // 如果有token则添加Authorization头
    ...options.header
  }

  console.log('发起请求:', options)
  console.log('请求头包含token:', !!token)
  return options
}

// 响应拦截器
const responseInterceptor = (response: any) => {
  console.log('响应数据:', response)

  if (response.statusCode === 200) {
    return response.data
  } else if (response.statusCode === 401) {
    // token过期或无效，清除本地存储的认证信息
    console.warn('检测到401状态码，清除本地认证信息')
    try {
      Taro.removeStorageSync('token')
      Taro.removeStorageSync('refreshToken')
      Taro.removeStorageSync('userInfo')
      Taro.removeStorageSync('userId')
    } catch (error) {
      console.error('清除本地存储失败:', error)
    }

    // 提示用户重新登录
    Taro.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none',
      duration: 2000
    })

    // 跳转到个人中心页面重新登录
    setTimeout(() => {
      Taro.switchTab({
        url: '/pages/profile/profile'
      })
    }, 2000)

    throw new Error('登录已过期')
  } else {
    throw new Error(`请求失败: ${response.statusCode}`)
  }
}

// 封装请求方法
export const request = async <T = any>(options: RequestOptions): Promise<ApiResponse<T>> => {
  try {
    // 请求拦截
    const processedOptions = requestInterceptor(options)
    
    // 发起请求
    const response = await Taro.request({
      url: processedOptions.url,
      method: processedOptions.method || 'GET',
      data: processedOptions.data,
      header: processedOptions.header
    })
    
    // 响应拦截
    return responseInterceptor(response)
  } catch (error) {
    console.error('请求错误:', error)
    throw error
  }
}

// GET 请求
export const get = <T = any>(url: string, data?: any, header?: Record<string, string>): Promise<ApiResponse<T>> => {
  return request<T>({
    url,
    method: 'GET',
    data,
    header
  })
}

// POST 请求
export const post = <T = any>(url: string, data?: any, header?: Record<string, string>): Promise<ApiResponse<T>> => {
  return request<T>({
    url,
    method: 'POST',
    data,
    header
  })
}

// PUT 请求
export const put = <T = any>(url: string, data?: any, header?: Record<string, string>): Promise<ApiResponse<T>> => {
  return request<T>({
    url,
    method: 'PUT',
    data,
    header
  })
}

// DELETE 请求
export const del = <T = any>(url: string, data?: any, header?: Record<string, string>): Promise<ApiResponse<T>> => {
  return request<T>({
    url,
    method: 'DELETE',
    data,
    header
  })
}

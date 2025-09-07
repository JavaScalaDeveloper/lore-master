import { useState, useRef } from 'react'
import { useLoad, navigateBack, showToast, request, uploadFile, getStorageSync, connectSocket, onSocketOpen, onSocketMessage, onSocketClose, onSocketError, sendSocketMessage, closeSocket, getRecorderManager, authorize } from '@tarojs/taro'
import { View, Text, ScrollView, Textarea, Button, Image } from '@tarojs/components'
import MarkdownRenderer from '../../components/MarkdownRenderer/MarkdownRenderer'
import { API_CONFIG } from '../../config/api'
import './chat.css'

// API配置
const API_BASE_URL = API_CONFIG.baseUrl
const WS_BASE_URL = API_BASE_URL.replace('http://', 'ws://').replace('https://', 'wss://')
const API_ENDPOINTS = {
  CHAT_STREAM: `${API_BASE_URL}/api/chat/stream`,
  CHAT_MINIAPP_STREAM: `${API_BASE_URL}/api/chat/miniapp-stream`,
  CHAT_SEND: `${API_BASE_URL}/api/chat/send`,
  CHAT_HISTORY: `${API_BASE_URL}/api/chat/history`,
  WS_CHAT: `${WS_BASE_URL}/ws/chat`
}

// 工具函数
const getApiHeaders = (token?: string, contentType = 'application/x-www-form-urlencoded') => {
  const headers: Record<string, string> = {
    'content-type': contentType,
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

// 统一的API请求函数，自动添加登录态
const apiRequest = async (options: any) => {
  const token = getStorageSync('token')

  if (!token && !options.skipAuth) {
    throw new Error('未找到登录token，请重新登录')
  }

  const requestOptions = {
    ...options,
    header: {
      ...options.header,
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
  }

  console.log('API请求:', {
    url: options.url,
    method: options.method,
    hasToken: !!token
  })

  return request(requestOptions)
}

// 消息类型定义
interface Message {
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: number
  isTyping?: boolean
  isComplete?: boolean
}

const Chat = () => {
  const [messages, setMessages] = useState<Message[]>([])
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(true)
  const [currentTypingId, setCurrentTypingId] = useState<string | null>(null)
  const [wsConnected, setWsConnected] = useState(false)
  const [wsConnecting, setWsConnecting] = useState(false)
  const [userInfo, setUserInfo] = useState<any>(null)
  
  // 语音录制相关状态
  const [isRecording, setIsRecording] = useState(false)
  const [recordingTime, setRecordingTime] = useState(0)
  const [inputMode, setInputMode] = useState<'text' | 'voice'>('text')
  const [isConverting, setIsConverting] = useState(false)

  const scrollViewRef = useRef<any>()
  const currentMessageIdRef = useRef<string | null>(null)
  const reconnectTimeoutRef = useRef<any>(null)
  const reconnectAttemptsRef = useRef(0)
  const maxReconnectAttempts = 5
  
  // 录音管理器和定时器
  const recorderManagerRef = useRef<any>(null)
  const recordingTimerRef = useRef<any>(null)

  // 生成唯一ID
  const generateId = () => {
    return Date.now().toString() + Math.random().toString(36).substring(2, 11)
  }

  // 检查登录状态
  const checkLoginStatus = () => {
    const token = getStorageSync('token')
    const storedUserInfo = getStorageSync('userInfo')

    console.log('检查登录状态:', {
      hasToken: !!token,
      hasUserInfo: !!storedUserInfo,
      userId: (storedUserInfo && storedUserInfo.id) || 'none'
    })

    if (!token) {
      console.warn('未找到登录token')
      showToast({
        title: '请先登录',
        icon: 'none',
        duration: 3000
      })
      return false
    }

    return true
  }

  // 获取聊天历史
  const loadChatHistory = async (showToastOnSuccess = false) => {
    try {
      console.log('开始加载聊天历史...')
      setIsLoadingHistory(true)

      // 获取token
      const token = getStorageSync('token')
      if (!token) {
        console.log('未找到token，跳过加载聊天历史')
        initializeWelcomeMessage()
        setIsLoadingHistory(false)
        return
      }

      const response = await apiRequest({
        url: API_ENDPOINTS.CHAT_HISTORY,
        method: 'POST',
        header: {
          'content-type': 'application/json'
        },
        data: {
          page: 0,
          size: 20 // 获取最近20条消息
        }
      })

      console.log('聊天历史API响应:', response)

      if (response.statusCode === 200 && response.data && response.data.success) {
        const historyMessages = response.data.data || []
        console.log('获取到聊天历史:', historyMessages.length, '条')

        // 转换历史消息格式
        const convertedMessages: Message[] = historyMessages.map((msg: any) => ({
          id: msg.messageId || generateId(),
          type: msg.role === 'user' ? 'user' : 'ai',
          content: msg.content,
          timestamp: new Date(msg.createTime).getTime(),
          isComplete: true
        }))

        // 如果有历史消息，设置到状态中
        if (convertedMessages.length > 0) {
          setMessages(convertedMessages)
          console.log('已加载', convertedMessages.length, '条历史消息')

          // 如果是手动刷新，显示成功提示
          if (showToastOnSuccess) {
            showToast({
              title: `已加载${convertedMessages.length}条历史消息`,
              icon: 'success',
              duration: 2000
            })
          }
        } else {
          // 没有历史消息时显示欢迎消息
          initializeWelcomeMessage()

          // 如果是手动刷新，显示无历史消息提示
          if (showToastOnSuccess) {
            showToast({
              title: '暂无聊天历史',
              icon: 'none',
              duration: 2000
            })
          }
        }
      } else {
        console.error('获取聊天历史失败:', (response.data && response.data.message) || '未知错误')
        // 失败时显示欢迎消息
        initializeWelcomeMessage()

        // 显示错误提示（可选）
        if (response.statusCode !== 200) {
          showToast({
            title: '加载聊天历史失败',
            icon: 'none',
            duration: 2000
          })
        }
      }
    } catch (error) {
      console.error('加载聊天历史出错:', error)
      // 出错时显示欢迎消息
      initializeWelcomeMessage()

      // 显示网络错误提示
      showToast({
        title: '网络连接异常',
        icon: 'none',
        duration: 2000
      })
    } finally {
      setIsLoadingHistory(false)
    }
  }

  // 初始化欢迎消息
  const initializeWelcomeMessage = () => {
    const welcomeMessage: Message = {
      id: generateId(),
      type: 'ai',
      content: '你好！我是AI测评助手 🎯\n\n我可以帮助你进行学习测评。请告诉我你想要测评哪个方面吧！',
      timestamp: Date.now(),
      isComplete: true
    }
    setMessages([welcomeMessage])
  }

  useLoad(() => {
    console.log('聊天页面加载')

    // 获取用户信息
    const storedUserInfo = getStorageSync('userInfo')
    if (storedUserInfo) {
      setUserInfo(storedUserInfo)
    }

    // 检查登录状态
    if (!checkLoginStatus()) {
      // 如果未登录，仍然显示欢迎消息，但不加载历史记录
      initializeWelcomeMessage()
      setIsLoadingHistory(false)
      return
    }

    // 先尝试加载聊天历史，如果失败则显示欢迎消息
    loadChatHistory()

    // 延迟初始化WebSocket，确保页面完全加载
    setTimeout(() => {
      initializeWebSocket()
    }, 500)
  })

  // WebSocket连接诊断
  const diagnoseWebSocketConnection = () => {
    console.log('=== WebSocket连接诊断 ===')
    console.log('连接状态:', wsConnected ? '已连接' : '未连接')
    console.log('连接中状态:', wsConnecting ? '连接中' : '空闲')
    console.log('重连次数:', reconnectAttemptsRef.current)
    console.log('WebSocket URL:', API_ENDPOINTS.WS_CHAT)
    console.log('当前消息ID:', currentMessageIdRef.current)

    // 测试后端连接
    apiRequest({
      url: `${API_BASE_URL}/api/websocket/status`,
      method: 'GET',
      skipAuth: true, // 这个接口可能不需要认证
      success: (res) => {
        console.log('后端WebSocket状态:', res.data)
      },
      fail: (err) => {
        console.error('无法获取后端WebSocket状态:', err)
      }
    }).catch(err => {
      console.error('测试后端连接失败:', err)
    })
  }

  // 初始化WebSocket连接
  const initializeWebSocket = () => {
    if (wsConnecting) {
      console.log('WebSocket正在连接中，跳过重复初始化')
      return
    }

    try {
      console.log(`初始化WebSocket连接 (尝试 ${reconnectAttemptsRef.current + 1}/${maxReconnectAttempts})`)
      setWsConnecting(true)

      // 获取登录态信息
      const token = getStorageSync('token')
      const userId = getStorageSync('userId') || (userInfo && userInfo.id)

      if (!token) {
        console.warn('未找到token，WebSocket连接可能失败')
        showToast({
          title: '请先登录',
          icon: 'none',
          duration: 2000
        })
        setWsConnecting(false)
        return
      }

      // 构建带有登录态信息的WebSocket URL
      const wsUrl = `${API_ENDPOINTS.WS_CHAT}?token=${encodeURIComponent(token)}&userId=${encodeURIComponent(userId || '')}`
      console.log('WebSocket连接URL:', wsUrl.replace(token, '***TOKEN***')) // 日志中隐藏token

      connectSocket({
        url: wsUrl,
        protocols: [], // 小程序WebSocket协议
        header: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'User-Agent': 'WeChat-MiniProgram'
        },
        success: () => {
          console.log('WebSocket连接请求成功')
        },
        fail: (err) => {
          console.error('WebSocket连接请求失败', err)
          setWsConnecting(false)
          handleConnectionFailure()
        }
      })

      // 监听连接打开
      onSocketOpen(() => {
        console.log('WebSocket连接已建立')
        setWsConnected(true)
        setWsConnecting(false)
        reconnectAttemptsRef.current = 0 // 重置重连次数

        // 清除重连定时器
        if (reconnectTimeoutRef.current) {
          clearTimeout(reconnectTimeoutRef.current)
          reconnectTimeoutRef.current = null
        }

        // 立即发送认证信息
        const authMessage = {
          type: 'auth',
          token: token,
          userId: userId || 'anonymous',
          timestamp: Date.now()
        }

        console.log('发送WebSocket认证信息:', {
          type: 'auth',
          userId: userId || 'anonymous',
          hasToken: !!token,
          timestamp: authMessage.timestamp
        })

        sendSocketMessage({
          data: JSON.stringify(authMessage)
        })

        console.log('WebSocket认证信息已发送')
      })

      // 监听消息
      onSocketMessage((res) => {
        console.log('收到WebSocket消息:', res.data)
        handleWebSocketMessage(res.data)
      })

      // 监听连接关闭
      onSocketClose(() => {
        console.log('WebSocket连接已关闭')
        setWsConnected(false)
        setWsConnecting(false)

        // 如果不是主动关闭，尝试重连
        if (reconnectAttemptsRef.current < maxReconnectAttempts) {
          scheduleReconnect()
        }
      })

      // 监听错误
      onSocketError((err) => {
        console.error('WebSocket错误', err)
        setWsConnected(false)
        setWsConnecting(false)
        handleConnectionFailure()
      })
    } catch (error) {
      console.error('WebSocket初始化失败', error)
      setWsConnecting(false)
      handleConnectionFailure()
    }
  }

  // 处理连接失败
  const handleConnectionFailure = () => {
    if (reconnectAttemptsRef.current < maxReconnectAttempts) {
      scheduleReconnect()
    } else {
      console.log('WebSocket重连次数已达上限，停止重连')
    }
  }

  // 安排重连
  const scheduleReconnect = () => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 10000) // 指数退避，最大10秒
    console.log(`${delay}ms后尝试重连WebSocket`)

    reconnectTimeoutRef.current = setTimeout(() => {
      reconnectAttemptsRef.current++
      initializeWebSocket()
    }, delay)
  }

  // 手动重连WebSocket
  const reconnectWebSocket = () => {
    console.log('手动重连WebSocket')
    reconnectAttemptsRef.current = 0

    // 先关闭现有连接
    try {
      closeSocket()
    } catch (error) {
      console.log('关闭WebSocket时出错（可能已关闭）', error)
    }

    setTimeout(() => {
      initializeWebSocket()
    }, 1000)
  }

  // 处理WebSocket消息
  const handleWebSocketMessage = (data: string) => {
    try {
      // 处理认证响应
      if (data.startsWith('[AUTH_SUCCESS]')) {
        console.log('WebSocket认证成功:', data)
        showToast({
          title: '身份验证成功',
          icon: 'success'
        })
        return
      }

      if (data.startsWith('[AUTH_FAILED]') || data.startsWith('[AUTH_ERROR]')) {
        console.error('WebSocket认证失败:', data)
        showToast({
          title: '身份验证失败',
          icon: 'error'
        })
        return
      }

      const messageId = currentMessageIdRef.current
      if (!messageId) {
        console.log('收到WebSocket消息但没有当前消息ID:', data)
        return
      }

      // 处理特殊标记
      if (data === '[STREAM_START]') {
        console.log('流式响应开始')
        return
      }

      if (data === '[STREAM_END]') {
        console.log('流式响应结束')
        setMessages(prev => prev.map(msg =>
          msg.id === messageId
            ? { ...msg, isTyping: false, isComplete: true }
            : msg
        ))
        setCurrentTypingId(null)
        currentMessageIdRef.current = null
        setIsLoading(false) // 重要：重置加载状态，允许用户继续输入
        return
      }

      if (data.startsWith('[STREAM_ERROR]')) {
        console.error('流式响应错误:', data)
        setMessages(prev => prev.map(msg =>
          msg.id === messageId
            ? {
                ...msg,
                content: msg.content + '\n\n[错误: ' + data.substring(14) + ']',
                isTyping: false,
                isComplete: true
              }
            : msg
        ))
        setCurrentTypingId(null)
        currentMessageIdRef.current = null
        setIsLoading(false) // 重要：重置加载状态
        return
      }

      if (data.startsWith('[ERROR]')) {
        console.error('WebSocket错误:', data)
        setIsLoading(false) // 重要：重置加载状态
        return
      }

      // 处理普通流式内容
      setMessages(prev => prev.map(msg => {
        if (msg.id === messageId) {
          const newContent = msg.content + data
          return {
            ...msg,
            content: newContent,
            isTyping: true,
            isComplete: false
          }
        }
        return msg
      }))

      // 滚动到底部
      scrollToBottom()
    } catch (error) {
      console.error('处理WebSocket消息失败', error)
    }
  }



  // 发送消息
  const sendMessage = async (messageContent?: string) => {
    const content = messageContent || inputText.trim()
    if (!content) return

    // 检查登录状态
    if (!checkLoginStatus()) {
      return
    }

    const userMessage: Message = {
      id: generateId(),
      type: 'user',
      content: content,
      timestamp: Date.now(),
      isComplete: true
    }

    setMessages(prev => [...prev, userMessage])
    setInputText('')
    setIsLoading(true)

    // 创建AI消息占位符
    const aiMessageId = generateId()
    const aiMessage: Message = {
      id: aiMessageId,
      type: 'ai',
      content: '',
      timestamp: Date.now(),
      isTyping: true,
      isComplete: false
    }

    setMessages(prev => [...prev, aiMessage])
    setCurrentTypingId(aiMessageId)

    try {
      await callBackendAPI(userMessage.content, aiMessageId)
    } catch (error) {
      console.error('发送消息失败', error)

      // 更新消息为错误状态
      setMessages(prev => prev.map(msg =>
        msg.id === aiMessageId
          ? {
              ...msg,
              content: '抱歉，我现在无法回复。请稍后再试。\n\n可能的原因：\n• 网络连接问题\n• 服务器暂时不可用\n• 请求超时',
              isTyping: false,
              isComplete: true
            }
          : msg
      ))

      showToast({
        title: '发送失败，请重试',
        icon: 'error'
      })
    } finally {
      setIsLoading(false)
      setCurrentTypingId(null)
    }
  }

  // 调用后端API - 多种流式响应方案
  const callBackendAPI = async (message: string, aiMessageId: string) => {
    const token = getStorageSync('token')
    const storedUserInfo = getStorageSync('userInfo')
    const userId = (storedUserInfo && storedUserInfo.id) || (userInfo && userInfo.id) || getStorageSync('userId') || 'anonymous'

    // 检查登录态
    if (!token) {
      throw new Error('未找到登录token，请重新登录')
    }

    try {
      console.log('发送消息到后端', { message, userId, wsConnected })

      // 方案1：优先使用WebSocket（真正的流式响应）
      if (wsConnected) {
        console.log('=== 使用WebSocket流式方案 ===')
        const wsSuccess = await tryWebSocketStream(message, userId, aiMessageId)
        if (wsSuccess) return
      }

      // 方案2：备用方案 - 同步接口 + 快速打字机
      console.log('=== WebSocket不可用，使用同步接口 + 快速打字机 ===')
      // 直接使用同步接口，不再尝试downloadFile（已证实无效）
      await fallbackToSyncAPI(message, userId, token, aiMessageId)
    } catch (error) {
      console.error('API调用失败', error)

      // 显示友好的错误信息
      const fallbackContent = `我收到了你的消息："${message}"\n\n由于网络问题，暂时无法连接到AI服务。请检查网络连接后重试。\n\n错误信息：${error.message || '未知错误'}`
      await simulateRealTimeTyping(aiMessageId, fallbackContent)
    }
  }

  // 检查WebSocket连接状态
  const checkWebSocketConnection = (): boolean => {
    if (!wsConnected) {
      console.log('WebSocket未连接，尝试重连')
      reconnectWebSocket()
      return false
    }
    return true
  }

  // 使用WebSocket发送消息
  const tryWebSocketStream = async (message: string, userId: string, aiMessageId: string): Promise<boolean> => {
    try {
      console.log('使用WebSocket发送消息', { message, userId, aiMessageId, wsConnected, wsConnecting })

      // 检查连接状态
      if (!checkWebSocketConnection()) {
        return false
      }

      // 如果正在连接中，等待一下
      if (wsConnecting) {
        console.log('WebSocket正在连接中，等待连接完成')
        await new Promise(resolve => setTimeout(resolve, 2000))

        if (!wsConnected) {
          console.log('等待连接超时')
          return false
        }
      }

      // 设置当前消息ID，用于接收流式数据
      currentMessageIdRef.current = aiMessageId

      // 获取登录态信息
      const token = getStorageSync('token')

      // 发送消息到WebSocket，包含登录态信息
      const messageData = {
        message: message,
        userId: userId,
        messageId: aiMessageId,
        token: token // 添加token到消息数据中
      }

      console.log('发送WebSocket消息:', { ...messageData, token: '***TOKEN***' }) // 日志中隐藏token

      sendSocketMessage({
        data: JSON.stringify(messageData)
      })

      console.log('WebSocket消息已发送')
      return true
    } catch (error) {
      console.error('WebSocket发送失败', error)
      currentMessageIdRef.current = null

      // 发送失败时尝试重连
      if (!wsConnected) {
        reconnectWebSocket()
      }

      return false
    }
  }





  // 备用同步API
  const fallbackToSyncAPI = async (message: string, userId: string, token: string, aiMessageId: string) => {
    console.log('使用备用同步API')

    const response = await apiRequest({
      url: API_ENDPOINTS.CHAT_SEND,
      method: 'POST',
      data: `message=${encodeURIComponent(message)}&userId=${encodeURIComponent(userId)}`,
      header: getApiHeaders(token),
      timeout: 30000
    })

    console.log('同步接口响应', response)

    if (response.statusCode === 200 && response.data && response.data.success) {
      const content = (response.data.data && response.data.data.message) || '收到了你的消息，但无法生成详细回复。'

      // 使用快速打字机效果显示
      await simulateRealTimeTyping(aiMessageId, content)
    } else {
      throw new Error(`同步API请求失败: ${response.statusCode}`)
    }
  }

  // 快速实时打字效果 - 模拟真实流式响应
  const simulateRealTimeTyping = async (messageId: string, fullContent: string) => {
    const words = fullContent.split(' ')
    let currentContent = ''
    let wordIndex = 0

    const typeNextWord = async () => {
      if (wordIndex >= words.length) {
        // 完成打字效果
        setMessages(prev => prev.map(msg =>
          msg.id === messageId
            ? { ...msg, isTyping: false, isComplete: true }
            : msg
        ))

        setCurrentTypingId(null)
        scrollToBottom()
        return
      }

      const word = words[wordIndex]
      currentContent += (wordIndex === 0 ? '' : ' ') + word
      wordIndex++

      setMessages(prev => prev.map(msg =>
        msg.id === messageId
          ? { ...msg, content: currentContent }
          : msg
      ))

      // 更快的打字速度，按词显示而不是按字符
      let delay = 100 // 基础延迟100ms

      // 根据词的长度调整延迟
      if (word.length > 5) {
        delay = 150
      } else if (word.length < 3) {
        delay = 80
      }

      // 标点符号后稍微停顿
      if (word.endsWith('.') || word.endsWith('!') || word.endsWith('?') ||
          word.endsWith('。') || word.endsWith('！') || word.endsWith('？')) {
        delay = 300
      } else if (word.endsWith(',') || word.endsWith(';') ||
                 word.endsWith('，') || word.endsWith('；')) {
        delay = 200
      }

      // 每隔几个词滚动一次
      if (wordIndex % 5 === 0) {
        scrollToBottom()
      }

      // 继续下一个词
      setTimeout(typeNextWord, delay)
    }

    // 开始打字效果
    typeNextWord()
  }



  // 滚动到底部
  const scrollToBottom = () => {
    setTimeout(() => {
      if (scrollViewRef.current) {
        scrollViewRef.current.scrollTop = scrollViewRef.current.scrollHeight
      }
    }, 100)
  }

  // 处理输入
  const handleInput = (e: any) => {
    setInputText(e.detail.value)
  }

  // 初始化录音管理器
  const initRecorderManager = () => {
    if (recorderManagerRef.current) {
      return recorderManagerRef.current
    }

    const recorderManager = getRecorderManager()
    recorderManagerRef.current = recorderManager

    // 录音开始
    recorderManager.onStart(() => {
      console.log('录音开始')
      setIsRecording(true)
      setRecordingTime(0)
      
      // 开始计时
      recordingTimerRef.current = setInterval(() => {
        setRecordingTime(prev => prev + 1)
      }, 1000)
    })

    // 录音结束
    recorderManager.onStop((result) => {
      console.log('录音结束', result)
      setIsRecording(false)
      setRecordingTime(0)
      
      // 清除计时器
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current)
        recordingTimerRef.current = null
      }

      // 处理录音结果
      if (result.tempFilePath) {
        handleVoiceToText(result.tempFilePath)
      } else {
        showToast({
          title: '录音失败，请重试',
          icon: 'none'
        })
      }
    })

    // 录音错误
    recorderManager.onError((error) => {
      console.error('录音错误', error)
      setIsRecording(false)
      setRecordingTime(0)
      
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current)
        recordingTimerRef.current = null
      }

      showToast({
        title: '录音失败：' + (error.errMsg || '未知错误'),
        icon: 'none'
      })
    })

    return recorderManager
  }

  // 开始录音
  const startRecording = async () => {
    try {
      // 检查录音权限
      const authResult = await authorize({
        scope: 'scope.record'
      })
      
      console.log('录音权限检查结果:', authResult)
      
      const recorderManager = initRecorderManager()
      
      // 开始录音
      recorderManager.start({
        duration: 60000, // 最长60秒
        sampleRate: 16000, // 采样率
        numberOfChannels: 1, // 声道数
        encodeBitRate: 96000, // 编码码率
        format: 'mp3' // 音频格式
      })
    } catch (error) {
      console.error('开始录音失败', error)
      showToast({
        title: '无法开始录音，请检查权限',
        icon: 'none'
      })
    }
  }

  // 停止录音
  const stopRecording = () => {
    if (recorderManagerRef.current && isRecording) {
      recorderManagerRef.current.stop()
    }
  }

  // 语音转文字
  const handleVoiceToText = async (filePath: string) => {
    try {
      setIsConverting(true)
      
      showToast({
        title: '正在转换语音...',
        icon: 'loading',
        duration: 3000
      })

      console.log('开始语音转文字，文件路径:', filePath)
      
      // 获取用户信息
      const token = getStorageSync('token')
      
      if (!token) {
        throw new Error('未找到登录token，请重新登录')
      }

      // 读取文件并转换为Base64
      const fileManager = wx.getFileSystemManager()
      const fileData = fileManager.readFileSync(filePath, 'base64')
      const fileInfo = fileManager.statSync(filePath)
      
      console.log('文件信息:', { 
        path: filePath, 
        size: fileInfo.size,
        base64Length: fileData.length 
      })

      // 构建请求数据
      const requestData = {
        audioData: fileData,
        format: 'mp3',
        fileSize: fileInfo.size,
        fileName: 'voice_record.mp3',
        language: 'zh_cn'
      }
      
      console.log('请求数据:', {
        format: requestData.format,
        fileSize: requestData.fileSize,
        fileName: requestData.fileName,
        audioDataLength: requestData.audioData.length
      })
      
      // 调用后端语音转文字API
      const response = await request({
        url: `${API_BASE_URL}/api/voice/transcribe`,
        method: 'POST',
        data: requestData,
        header: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000 // 30秒超时
      })
      
      console.log('语音转文字API响应:', response)
      
      if (response.statusCode === 200 && response.data) {
        const result = typeof response.data === 'string' ? JSON.parse(response.data) : response.data
        
        if (result.success && result.data && result.data.text) {
          const recognizedText = result.data.text.trim()
          
          if (recognizedText) {
            setInputText(recognizedText)
            
            showToast({
              title: '语音转换完成',
              icon: 'success'
            })
            
            console.log('语音识别结果:', recognizedText)
            
            // 自动发送转换后的文字
            setTimeout(() => {
              sendMessage(recognizedText)
            }, 500)
          } else {
            throw new Error('语音识别结果为空，请重新录音')
          }
        } else {
          throw new Error(result.message || '语音转换服务返回错误')
        }
      } else {
        throw new Error(`语音转换请求失败，状态码: ${response.statusCode}`)
      }
    } catch (error) {
      console.error('语音转文字失败:', error)
      setIsConverting(false)
      
      let errorMessage = '语音转换失败'
      
      if (error.message) {
        if (error.message.includes('网络')) {
          errorMessage = '网络连接失败，请检查网络后重试'
        } else if (error.message.includes('token')) {
          errorMessage = '登录已过期，请重新登录'
        } else if (error.message.includes('timeout')) {
          errorMessage = '语音转换超时，请重试'
        } else {
          errorMessage = error.message
        }
      }
      
      showToast({
        title: errorMessage,
        icon: 'none',
        duration: 3000
      })
      
      // 如果是登录问题，清除token
      if (errorMessage.includes('登录')) {
        // 可以考虑跳转到登录页面
        console.log('需要重新登录')
      }
    } finally {
      setIsConverting(false)
    }
  }

  // 切换输入模式
  const toggleInputMode = () => {
    setInputMode(prev => prev === 'text' ? 'voice' : 'text')
  }

  // 处理长按开始录音
  const handleTouchStart = () => {
    if (inputMode === 'voice') {
      startRecording()
    }
  }

  // 处理松开结束录音
  const handleTouchEnd = () => {
    if (inputMode === 'voice' && isRecording) {
      stopRecording()
    }
  }

  // 返回首页
  const handleBack = () => {
    navigateBack()
  }

  return (
    <View className='chat-container'>
      {/* 顶部导航栏 */}
      <View className='chat-header'>
        <View className='header-left' onClick={handleBack}>
          <Text className='back-icon'>←</Text>
        </View>
        <View className='header-title'>
          <Text className='title-text'>AI测评助手</Text>
        </View>
        <View className='header-right'>
          {/* 刷新聊天历史按钮 */}
          <View
            className='refresh-btn'
            onClick={() => {
              showToast({
                title: '正在刷新聊天历史',
                icon: 'loading',
                duration: 1000
              })
              loadChatHistory(true)
            }}
            style={{ marginRight: '10px' }}
          >
            <Text className='refresh-icon'>🔄</Text>
          </View>
          {/* WebSocket状态 */}
          <View
            className='ws-status'
            onClick={wsConnected ? undefined : reconnectWebSocket}
            onLongPress={diagnoseWebSocketConnection}
          >
            <Text className='status-icon'>
              {wsConnecting ? '🟡' : (wsConnected ? '🟢' : '🔴')}
            </Text>
            <Text className='status-text'>
              {wsConnecting ? '连接中' : (wsConnected ? 'WS' : 'HTTP')}
            </Text>
          </View>
        </View>
      </View>

      {/* 消息列表 */}
      <ScrollView
        className='messages-container'
        scrollY
        ref={scrollViewRef}
        scrollIntoView={`msg-${messages.length - 1}`}
      >
        {/* 加载历史消息指示器 */}
        {isLoadingHistory && (
          <View className='loading-history'>
            <Text className='loading-text'>正在加载聊天历史...</Text>
          </View>
        )}

        {/* 调试信息：显示消息数量和登录状态 */}
        {!isLoadingHistory && (
          <View className='debug-info'>
            <Text className='debug-text'>
              消息: {messages.length} 条 |
              登录: {getStorageSync('token') ? '✓' : '✗'} |
              WS: {wsConnected ? '✓' : '✗'}
            </Text>
          </View>
        )}

        {messages.map((message, messageIndex) => (
          <View key={message.id} id={`msg-${messageIndex}`} className={`message-item ${message.type}`}>
            {message.type === 'ai' ? (
              <View className='message-ai'>
                <View className='avatar-container'>
                  <Text className='ai-avatar'>🤖</Text>
                </View>
                <View className='message-content'>
                  <View className='message-bubble ai-bubble'>
                    {/* 使用Markdown渲染器显示AI回复 */}
                    <MarkdownRenderer
                      content={message.content}
                      className='ai-message-markdown'
                    />
                    {message.isTyping && (
                      <View className='typing-indicator'>
                        <Text className='typing-dot'>●</Text>
                        <Text className='typing-dot'>●</Text>
                        <Text className='typing-dot'>●</Text>
                      </View>
                    )}
                  </View>
                  <Text className='message-time'>
                    {new Date(message.timestamp).toLocaleTimeString()}
                  </Text>
                </View>
              </View>
            ) : (
              <View className='message-user'>
                <View className='message-content'>
                  <View className='message-bubble user-bubble'>
                    <Text className='message-text'>{message.content}</Text>
                  </View>
                  <Text className='message-time'>
                    {new Date(message.timestamp).toLocaleTimeString()}
                  </Text>
                </View>
                <View className='avatar-container user-avatar-container'>
                  {userInfo && userInfo.avatarUrl ? (
                    <Image
                      src={userInfo.avatarUrl}
                      className='user-avatar-image'
                      mode='aspectFill'
                      onError={() => {
                        console.log('用户头像加载失败，使用默认头像')
                      }}
                    />
                  ) : (
                    <Text className='user-avatar-text'>👤</Text>
                  )}
                </View>
              </View>
            )}
          </View>
        ))}
        
        {/* 加载指示器 */}
        {isLoading && (
          <View className='loading-container'>
            <Text className='loading-text'>AI正在思考中...</Text>
          </View>
        )}
      </ScrollView>

      {/* 输入区域 */}
      <View className='input-container'>
        <View className='input-row'>
          {/* 输入模式切换按钮 */}
          <View className='mode-switch-btn' onClick={toggleInputMode}>
            <Text className='mode-icon'>
              {inputMode === 'text' ? '🎤' : '⌨️'}
            </Text>
          </View>
          
          <View className='input-wrapper'>
            {inputMode === 'text' ? (
              /* 文字输入模式 */
              <Textarea
                className='message-input'
                placeholder='输入消息...'
                value={inputText}
                onInput={handleInput}
                maxlength={1000}
                autoHeight
                showConfirmBar={false}
              />
            ) : (
              /* 语音输入模式 */
              <View
                className={`voice-input ${isRecording ? 'recording' : ''}`}
                onTouchStart={handleTouchStart}
                onTouchEnd={handleTouchEnd}
                onTouchCancel={handleTouchEnd}
              >
                {isRecording ? (
                  <View className='recording-indicator'>
                    <Text className='recording-text'>🎙️ 正在录音 {recordingTime}s</Text>
                    <Text className='recording-hint'>松开发送</Text>
                  </View>
                ) : isConverting ? (
                  <View className='converting-indicator'>
                    <Text className='converting-text'>🔄 正在转换语音...</Text>
                  </View>
                ) : (
                  <View className='voice-prompt'>
                    <Text className='voice-text'>🎤 按住说话</Text>
                  </View>
                )}
              </View>
            )}
          </View>
          
          {inputMode === 'text' && (
            <Button 
              className={`send-btn ${inputText.trim() ? 'active' : ''}`}
              onClick={() => sendMessage()}
              disabled={!inputText.trim() || isLoading}
            >
              <Text className='send-icon'>➤</Text>
            </Button>
          )}
        </View>
        
        {/* 录音状态提示 */}
        {isRecording && (
          <View className='recording-status'>
            <View className='recording-wave'>
              <View className='wave-bar'></View>
              <View className='wave-bar'></View>
              <View className='wave-bar'></View>
              <View className='wave-bar'></View>
              <View className='wave-bar'></View>
            </View>
            <Text className='recording-status-text'>正在录音中，松开手指发送</Text>
          </View>
        )}
      </View>
    </View>
  )
}

export default Chat

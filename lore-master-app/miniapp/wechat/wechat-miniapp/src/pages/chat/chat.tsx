import { useState, useRef } from 'react'
import { useLoad, navigateBack, showToast, request, getStorageSync, connectSocket, onSocketOpen, onSocketMessage, onSocketClose, onSocketError, sendSocketMessage, closeSocket } from '@tarojs/taro'
import { View, Text, ScrollView, Textarea, Button } from '@tarojs/components'
import MarkdownRenderer from '../../components/MarkdownRenderer/MarkdownRenderer'
import './chat.css'

// API配置
const API_BASE_URL = 'http://localhost:8082'
const WS_BASE_URL = 'ws://localhost:8082'
const API_ENDPOINTS = {
  CHAT_STREAM: `${API_BASE_URL}/api/chat/stream`,
  CHAT_MINIAPP_STREAM: `${API_BASE_URL}/api/chat/miniapp-stream`,
  CHAT_SEND: `${API_BASE_URL}/api/chat/send`,
  WS_CHAT: `${WS_BASE_URL}/ws/chat`
}

// 工具函数
const getApiHeaders = (token?: string) => {
  const headers: Record<string, string> = {
    'content-type': 'application/x-www-form-urlencoded',
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
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
  const [currentTypingId, setCurrentTypingId] = useState<string | null>(null)
  const [wsConnected, setWsConnected] = useState(false)
  const [wsConnecting, setWsConnecting] = useState(false)

  const scrollViewRef = useRef<any>()
  const currentMessageIdRef = useRef<string | null>(null)
  const reconnectTimeoutRef = useRef<any>(null)
  const reconnectAttemptsRef = useRef(0)
  const maxReconnectAttempts = 5

  // 生成唯一ID
  const generateId = () => {
    return Date.now().toString() + Math.random().toString(36).substring(2, 11)
  }

  useLoad(() => {
    console.log('聊天页面加载')
    initializeChat()

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
    request({
      url: `${API_BASE_URL}/api/websocket/status`,
      method: 'GET',
      success: (res) => {
        console.log('后端WebSocket状态:', res.data)
      },
      fail: (err) => {
        console.error('无法获取后端WebSocket状态:', err)
      }
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

      connectSocket({
        url: API_ENDPOINTS.WS_CHAT,
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
      })

      // 监听消息
      onSocketMessage((res) => {
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

  // 初始化聊天
  const initializeChat = () => {
    const welcomeMessage: Message = {
      id: generateId(),
      type: 'ai',
      content: '你好！我是AI测评助手 🎯\n\n我可以帮助你进行学习测评。请告诉我你想要测评哪个方面吧！',
      timestamp: Date.now(),
      isComplete: true
    }
    setMessages([welcomeMessage])
  }

  // 发送消息
  const sendMessage = async () => {
    if (!inputText.trim()) return

    const userMessage: Message = {
      id: generateId(),
      type: 'user',
      content: inputText.trim(),
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
    const userId = getStorageSync('userInfo')?.id || 'anonymous'

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

      // 发送消息到WebSocket
      const messageData = {
        message: message,
        userId: userId,
        messageId: aiMessageId
      }

      console.log('发送WebSocket消息:', messageData)

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

    const response = await request({
      url: API_ENDPOINTS.CHAT_SEND,
      method: 'POST',
      data: `message=${encodeURIComponent(message)}&userId=${encodeURIComponent(userId)}`,
      header: getApiHeaders(token),
      timeout: 30000
    })

    console.log('同步接口响应', response)

    if (response.statusCode === 200 && response.data?.success) {
      const content = response.data.data?.message || '收到了你的消息，但无法生成详细回复。'

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
        <View
          className='header-right'
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

      {/* 消息列表 */}
      <ScrollView
        className='messages-container'
        scrollY
        ref={scrollViewRef}
        scrollIntoView={`msg-${messages.length - 1}`}
      >
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
                <View className='avatar-container'>
                  <Text className='user-avatar-text'>👤</Text>
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
          <View className='input-wrapper'>
            <Textarea
              className='message-input'
              placeholder='输入消息...'
              value={inputText}
              onInput={handleInput}
              maxlength={1000}
              autoHeight
              showConfirmBar={false}
            />
          </View>
          
          <Button 
            className={`send-btn ${inputText.trim() ? 'active' : ''}`}
            onClick={sendMessage}
            disabled={!inputText.trim() || isLoading}
          >
            <Text className='send-icon'>➤</Text>
          </Button>
        </View>
      </View>
    </View>
  )
}

export default Chat

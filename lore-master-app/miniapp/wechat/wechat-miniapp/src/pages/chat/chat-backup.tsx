import { useState, useRef } from 'react'
import { useLoad, navigateBack, showToast, request, getStorageSync } from '@tarojs/taro'
import { View, Text, ScrollView, Textarea, Button } from '@tarojs/components'
import './chat.css'

// 简单的API配置
const API_BASE_URL = 'http://localhost:8082'
const API_ENDPOINTS = {
  CHAT_SEND_MESSAGE: `${API_BASE_URL}/api/chat/sendMessageStream`
}

// 简单的工具函数
const apiLog = (message: string, data?: any) => {
  console.log(`[API] ${message}`, data || '')
}

const getApiHeaders = (token?: string) => {
  const headers: Record<string, string> = {
    'content-type': 'application/json',
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

  const scrollViewRef = useRef<any>()

  // 生成唯一ID
  const generateId = () => {
    return Date.now().toString() + Math.random().toString(36).substring(2, 11)
  }

  useLoad(() => {
    console.log('聊天页面加载')
    try {
      initializeChat()
    } catch (error) {
      console.error('初始化聊天失败', error)
    }
  })

  // 调用流式API
  const callStreamAPI = async (message: string, aiMessageId: string) => {
    const token = getStorageSync('token')
    const userId = getStorageSync('userInfo')?.id || 'anonymous'

    try {
      apiLog('发送消息到后端', { message, userId })

      // 调用后端接口
      const response = await request({
        url: API_ENDPOINTS.CHAT_SEND_MESSAGE,
        method: 'POST',
        data: {
          message: message,
          userId: userId
        },
        header: getApiHeaders(token),
        timeout: 30000
      })

      apiLog('后端响应', response)

      if (response.statusCode === 200) {
        let content = '收到了你的消息，这是AI的回复。'

        // 尝试解析响应
        if (response.data) {
          if (typeof response.data === 'string') {
            content = response.data
          } else if (response.data.success && response.data.data?.message) {
            content = response.data.data.message
          } else if (response.data.message) {
            content = response.data.message
          }
        }

        // 开始打字机效果
        await simulateTypingEffect(aiMessageId, content)
      } else {
        throw new Error(`API请求失败: ${response.statusCode}`)
      }
    } catch (error) {
      apiLog('API调用失败', error)

      // 显示模拟回复
      const fallbackContent = `我收到了你的消息："${message}"\n\n由于网络问题，这是一个模拟回复。请检查网络连接后重试。`
      await simulateTypingEffect(aiMessageId, fallbackContent)
    }
  }



  // 模拟打字机效果
  const simulateTypingEffect = async (messageId: string, fullContent: string) => {
    const chars = fullContent.split('')
    let currentContent = ''
    let charIndex = 0

    const typeNextChar = async () => {
      if (charIndex >= chars.length) {
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

      const char = chars[charIndex]
      currentContent += char
      charIndex++

      setMessages(prev => prev.map(msg =>
        msg.id === messageId
          ? { ...msg, content: currentContent }
          : msg
      ))

      // 动态调整打字速度
      let delay = 30

      // 标点符号后稍微停顿
      if (['.', '!', '?', '。', '！', '？'].includes(char)) {
        delay = 200
      } else if ([',', ';', '，', '；'].includes(char)) {
        delay = 100
      } else if (char === '\n') {
        delay = 150
      } else if (char === ' ') {
        delay = 50
      }

      // 每隔一定字符数滚动一次
      if (charIndex % 15 === 0) {
        scrollToBottom()
      }

      // 继续下一个字符
      setTimeout(typeNextChar, delay)
    }

    // 开始打字效果
    typeNextChar()
  }

  // 滚动到底部
  const scrollToBottom = () => {
    // 小程序中的滚动处理
    setTimeout(() => {
      if (scrollViewRef.current) {
        scrollViewRef.current.scrollTop = scrollViewRef.current.scrollHeight
      }
    }, 100)
  }

  // 初始化聊天
  const initializeChat = () => {
    try {
      const welcomeMessage: Message = {
        id: generateId(),
        type: 'ai',
        content: '你好！我是AI测评助手 🎯\n\n我可以帮助你进行学习测评。请告诉我你想要测评哪个方面吧！',
        timestamp: Date.now(),
        isComplete: true
      }
      setMessages([welcomeMessage])
      console.log('聊天初始化完成')
    } catch (error) {
      console.error('初始化聊天消息失败', error)
    }
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
      await callStreamAPI(userMessage.content, aiMessageId)
    } catch (error) {
      apiLog('发送消息失败', error)

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

  // 处理输入
  const handleInput = (e: any) => {
    setInputText(e.detail.value)
  }

  // 返回首页
  const handleBack = () => {
    navigateBack()
  }

  // 继续聊天
  const handleContinueChat = () => {
    if (currentTypingId) {
      const lastMessage = messages.find(msg => msg.id === currentTypingId)
      if (lastMessage && !lastMessage.isComplete) {
        // 重新发送最后一条用户消息
        const lastUserMessage = messages.filter(msg => msg.type === 'user').pop()
        if (lastUserMessage) {
          // 清除当前未完成的AI消息
          setMessages(prev => prev.filter(msg => msg.id !== currentTypingId))
          setCurrentTypingId(null)

          // 重新发送
          setTimeout(() => {
            setInputText(lastUserMessage.content)
            sendMessage()
          }, 100)
        }
      }
    }
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
          <Text className='status-icon'>🟢</Text>
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
                    <Text className='message-text'>{message.content}</Text>
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

        {/* 继续聊天按钮 */}
        {currentTypingId && (
          <View className='continue-container'>
            <Button className='continue-btn' onClick={handleContinueChat}>
              继续聊天
            </Button>
          </View>
        )}
      </View>
    </View>
  )
}

export default Chat

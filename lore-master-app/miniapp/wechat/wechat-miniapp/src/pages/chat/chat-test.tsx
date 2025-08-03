import { useState } from 'react'
import { useLoad, navigateBack } from '@tarojs/taro'
import { View, Text, ScrollView, Textarea, Button } from '@tarojs/components'
import './chat.css'

// 简单的消息类型
interface SimpleMessage {
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: number
}

const Chat = () => {
  const [messages, setMessages] = useState<SimpleMessage[]>([])
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  // 生成唯一ID
  const generateId = () => {
    return Date.now().toString() + Math.random().toString(36).substring(2, 11)
  }

  useLoad(() => {
    console.log('聊天页面加载')
    initializeChat()
  })

  // 初始化聊天
  const initializeChat = () => {
    const welcomeMessage: SimpleMessage = {
      id: generateId(),
      type: 'ai',
      content: '你好！我是AI测评助手 🎯\n\n我可以帮助你进行学习测评。请告诉我你想要测评哪个方面吧！',
      timestamp: Date.now()
    }
    setMessages([welcomeMessage])
  }

  // 发送消息
  const sendMessage = async () => {
    if (!inputText.trim()) return

    const userMessage: SimpleMessage = {
      id: generateId(),
      type: 'user',
      content: inputText.trim(),
      timestamp: Date.now()
    }

    setMessages(prev => [...prev, userMessage])
    setInputText('')
    setIsLoading(true)

    // 模拟AI回复
    setTimeout(() => {
      const aiMessage: SimpleMessage = {
        id: generateId(),
        type: 'ai',
        content: `我收到了你的消息："${userMessage.content}"\n\n这是一个测试回复。页面现在应该可以正常显示了！`,
        timestamp: Date.now()
      }
      setMessages(prev => [...prev, aiMessage])
      setIsLoading(false)
    }, 1000)
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
        <View className='header-right'>
          <Text className='status-icon'>🟢</Text>
        </View>
      </View>

      {/* 消息列表 */}
      <ScrollView className='messages-container' scrollY>
        {messages.map((message) => (
          <View key={message.id} className={`message-item ${message.type}`}>
            {message.type === 'ai' ? (
              <View className='message-ai'>
                <View className='avatar-container'>
                  <Text className='ai-avatar'>🤖</Text>
                </View>
                <View className='message-content'>
                  <View className='message-bubble ai-bubble'>
                    <Text className='message-text'>{message.content}</Text>
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

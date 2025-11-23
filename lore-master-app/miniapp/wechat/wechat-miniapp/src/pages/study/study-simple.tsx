import { useState } from 'react'
import { useLoad } from '@tarojs/taro'
import { View, Text } from '@tarojs/components'
import './study.css'

export default function Study() {
  const [message, setMessage] = useState('学习中心页面加载中...')

  useLoad(() => {
    console.log('Study page loaded.')
    setMessage('学习中心页面已加载')
  })

  return (
    <View className='study-container'>
      <Text className='study-title'>学习中心</Text>
      <Text className='study-content'>{message}</Text>
      <View className='test-section'>
        <Text>这是一个测试文本</Text>
      </View>
    </View>
  )
}

import { useLoad } from '@tarojs/taro'
import './study.css'

export default function Study () {
  useLoad(() => {
    console.log('Study page loaded.')
  })

  return (
    <View className='study-container'>
      <Text className='study-title'>学习中心</Text>
      <Text className='study-content'>欢迎来到学习中心页面</Text>
    </View>
  )
}
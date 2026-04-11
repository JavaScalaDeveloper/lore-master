import { useState } from 'react'
import { useRouter, useLoad, navigateBack } from '@tarojs/taro'
import { View, Text, Button } from '@tarojs/components'

const MindmapDebug = () => {
  const router = useRouter()
  const [params, setParams] = useState<any>({})

  useLoad(() => {
    console.log('调试页面 - 路由参数:', router.params)
    setParams(router.params)
  })

  const handleBack = () => {
    navigateBack()
  }

  return (
    <View style={{ padding: '20px', backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      <View style={{ marginBottom: '20px' }}>
        <Button onClick={handleBack}>返回</Button>
      </View>
      
      <View style={{ backgroundColor: '#fff', padding: '20px', borderRadius: '10px', marginBottom: '20px' }}>
        <Text style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '10px' }}>
          路由参数调试信息
        </Text>
        
        <View style={{ marginBottom: '10px' }}>
          <Text style={{ color: '#666' }}>所有参数:</Text>
          <Text style={{ backgroundColor: '#f0f0f0', padding: '10px', borderRadius: '5px', marginTop: '5px' }}>
            {JSON.stringify(router.params, null, 2)}
          </Text>
        </View>

        <View style={{ marginBottom: '10px' }}>
          <Text style={{ color: '#666' }}>skillCode:</Text>
          <Text style={{ color: params.skillCode ? '#52c41a' : '#f5222d' }}>
            {params.skillCode || '未找到'}
          </Text>
        </View>

        <View style={{ marginBottom: '10px' }}>
          <Text style={{ color: '#666' }}>skillName:</Text>
          <Text style={{ color: params.skillName ? '#52c41a' : '#f5222d' }}>
            {params.skillName ? decodeURIComponent(params.skillName) : '未找到'}
          </Text>
        </View>

        <View style={{ marginBottom: '10px' }}>
          <Text style={{ color: '#666' }}>参数数量:</Text>
          <Text>{Object.keys(params).length}</Text>
        </View>
      </View>

      <View style={{ backgroundColor: '#fff', padding: '20px', borderRadius: '10px' }}>
        <Text style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '10px' }}>
          预期参数格式
        </Text>
        
        <Text style={{ color: '#666', marginBottom: '5px' }}>URL应该是:</Text>
        <Text style={{ backgroundColor: '#f0f0f0', padding: '10px', borderRadius: '5px', fontSize: '12px' }}>
          /pages/mindmap/mindmap?skillCode=java_expert&skillName=Java%E4%B8%93%E5%AE%B6
        </Text>
        
        <Text style={{ color: '#666', marginTop: '10px', marginBottom: '5px' }}>解析后应该得到:</Text>
        <Text style={{ backgroundColor: '#f0f0f0', padding: '10px', borderRadius: '5px', fontSize: '12px' }}>
          {JSON.stringify({
            skillCode: 'java_expert',
            skillName: 'Java专家'
          }, null, 2)}
        </Text>
      </View>
    </View>
  )
}

export default MindmapDebug

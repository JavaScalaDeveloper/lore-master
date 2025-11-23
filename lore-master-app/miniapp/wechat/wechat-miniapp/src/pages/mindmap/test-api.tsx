import { useState } from 'react'
import { useLoad, request, getStorageSync } from '@tarojs/taro'
import { View, Text, Button } from '@tarojs/components'
import { API_CONFIG, getApiHeaders } from '../../config/api'

const TestApi = () => {
  const [results, setResults] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  // 测试技能目录API
  const testSkillCatalogApi = async () => {
    setLoading(true)
    try {
      const token = getStorageSync('token')
      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/consumer/skill-catalog/tree`,
        method: 'POST',
        header: getApiHeaders(token),
        data: {
          treeStructure: true,
          isActive: true
        }
      })
      
      setResults(prev => [...prev, {
        api: '技能目录API',
        status: response.statusCode,
        success: response.data.success,
        dataCount: response.data.data?.length || 0,
        data: response.data.data
      }])
    } catch (e) {
      setResults(prev => [...prev, {
        api: '技能目录API',
        status: 'ERROR',
        error: e.message
      }])
    }
    setLoading(false)
  }

  // 测试消费者端知识图谱API
  const testKnowledgeMapApi = async () => {
    setLoading(true)
    try {
      const token = getStorageSync('token')
      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/consumer/knowledge-map/getSkillTree?rootCode=java_expert`,
        method: 'POST',
        header: getApiHeaders(token)
      })

      setResults(prev => [...prev, {
        api: '消费者端知识图谱API (java_expert)',
        status: response.statusCode,
        success: response.data.success,
        data: response.data.data
      }])
    } catch (e) {
      setResults(prev => [...prev, {
        api: '消费者端知识图谱API (java_expert)',
        status: 'ERROR',
        error: e.message
      }])
    }
    setLoading(false)
  }

  // 测试获取消费者端根节点列表
  const testRootNodes = async () => {
    setLoading(true)
    try {
      const token = getStorageSync('token')
      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/consumer/knowledge-map/getRootNodes`,
        method: 'GET',
        header: getApiHeaders(token)
      })

      setResults(prev => [...prev, {
        api: '消费者端根节点列表API',
        status: response.statusCode,
        success: response.data.success,
        dataCount: response.data.data?.length || 0,
        data: response.data.data
      }])
    } catch (e) {
      setResults(prev => [...prev, {
        api: '消费者端根节点列表API',
        status: 'ERROR',
        error: e.message
      }])
    }
    setLoading(false)
  }

  const clearResults = () => {
    setResults([])
  }

  return (
    <View style={{ padding: '20px', backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      <Text style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
        API测试工具
      </Text>
      
      <View style={{ marginBottom: '20px' }}>
        <Button 
          onClick={testSkillCatalogApi} 
          disabled={loading}
          style={{ marginBottom: '10px' }}
        >
          测试技能目录API
        </Button>
        
        <Button
          onClick={testKnowledgeMapApi}
          disabled={loading}
          style={{ marginBottom: '10px' }}
        >
          测试消费者端知识图谱API
        </Button>

        <Button
          onClick={testRootNodes}
          disabled={loading}
          style={{ marginBottom: '10px' }}
        >
          测试消费者端根节点列表API
        </Button>
        
        <Button onClick={clearResults} disabled={loading}>
          清空结果
        </Button>
      </View>

      {loading && (
        <Text style={{ color: '#1890ff', marginBottom: '20px' }}>
          正在测试API...
        </Text>
      )}

      {results.map((result, index) => (
        <View 
          key={index}
          style={{ 
            backgroundColor: '#fff', 
            padding: '15px', 
            borderRadius: '10px', 
            marginBottom: '15px',
            border: result.status === 200 ? '2px solid #52c41a' : '2px solid #f5222d'
          }}
        >
          <Text style={{ fontWeight: 'bold', marginBottom: '10px' }}>
            {result.api}
          </Text>
          
          <Text style={{ color: '#666', marginBottom: '5px' }}>
            状态: {result.status}
          </Text>
          
          {result.success !== undefined && (
            <Text style={{ color: result.success ? '#52c41a' : '#f5222d', marginBottom: '5px' }}>
              成功: {result.success ? '是' : '否'}
            </Text>
          )}
          
          {result.dataCount !== undefined && (
            <Text style={{ color: '#666', marginBottom: '5px' }}>
              数据条数: {result.dataCount}
            </Text>
          )}
          
          {result.error && (
            <Text style={{ color: '#f5222d', marginBottom: '5px' }}>
              错误: {result.error}
            </Text>
          )}
          
          {result.data && (
            <View style={{ marginTop: '10px' }}>
              <Text style={{ color: '#666', marginBottom: '5px' }}>数据:</Text>
              <Text style={{ 
                backgroundColor: '#f0f0f0', 
                padding: '10px', 
                borderRadius: '5px',
                fontSize: '12px',
                fontFamily: 'monospace'
              }}>
                {JSON.stringify(result.data, null, 2)}
              </Text>
            </View>
          )}
        </View>
      ))}
    </View>
  )
}

export default TestApi

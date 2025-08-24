import { useState, useEffect } from 'react'
import { useRouter, request, showToast, showLoading, hideLoading, navigateTo, getStorageSync } from '@tarojs/taro'
import { View, Text, ScrollView, Button } from '@tarojs/components'
import { buildApiUrl, getApiHeaders } from '../../../config/api'
import './collection.css'

// 课程数据类型定义
interface CourseVO {
  id: number
  courseCode: string
  title: string
  description: string
  author: string
  courseType: 'NORMAL' | 'COLLECTION'
  contentType: 'ARTICLE' | 'VIDEO'
  difficultyLevel: string
  coverImageUrl?: string
  thumbnailUrl?: string
  viewCount: number
  likeCount: number
  collectCount: number
  publishTime: string
  sortOrder?: number
}

interface CollectionDetailVO {
  id: number
  title: string
  description: string
  author: string
  difficultyLevel: string
  viewCount: number
  likeCount: number
  collectCount: number
  publishTime: string
  subCourses: CourseVO[]
}

export default function Collection() {
  const router = useRouter()
  const [collection, setCollection] = useState<CollectionDetailVO | null>(null)
  const [loading, setLoading] = useState(true)

  const courseCode = router.params.courseCode
  const title = router.params.title ? decodeURIComponent(router.params.title) : '合集详情'

  // 添加调试日志
  console.log('Collection页面参数:', router.params)
  console.log('获取到的courseCode:', courseCode)
  console.log('获取到的title:', title)

  useEffect(() => {
    if (courseCode) {
      console.log('开始加载合集详情，courseCode:', courseCode)
      loadCollectionDetail()
    } else {
      console.error('courseCode为空，无法加载合集详情')
      showToast({
        title: '课程参数错误',
        icon: 'error'
      })
    }
  }, [courseCode])

  // 加载合集详情
  const loadCollectionDetail = async () => {
    try {
      setLoading(true)
      showLoading({ title: '加载中...' })

      // 获取token
      let token = ''
      try {
        token = getStorageSync('token')
      } catch (error) {
        console.warn('获取token失败:', error)
      }

      // 调用getCourseByCode接口获取课程详情
      console.log('准备调用API，courseCode:', courseCode)
      const response = await request({
        url: buildApiUrl('/api/consumer/course/getCourseByCode'),
        method: 'POST',
        data: {
          courseCode: courseCode,
          includeSubCourses: true
        },
        header: getApiHeaders(token),
        timeout: 30000
      })

      console.log('合集详情响应:', response)

      if (response && response.data && response.data.success) {
        const courseDetail = response.data.data

        // 如果是合集，设置合集信息
        if (courseDetail.courseType === 'COLLECTION') {
          setCollection({
            ...courseDetail,
            subCourses: courseDetail.subCourses || []
          })
        } else {
          showToast({
            title: '这不是一个合集',
            icon: 'error'
          })
        }
      } else {
        const errorMsg = response?.data?.message || '加载失败'
        showToast({
          title: errorMsg,
          icon: 'error'
        })
      }
    } catch (error) {
      console.error('加载合集详情失败:', error)
      showToast({
        title: '网络错误，请检查网络连接',
        icon: 'error'
      })
    } finally {
      setLoading(false)
      hideLoading()
    }
  }

  // 处理子课程点击
  const handleSubCourseClick = (course: CourseVO) => {
    console.log('点击子课程:', course)
    console.log('子课程编码:', course.courseCode)
    console.log('子课程ID:', course.id)

    if (course.contentType === 'ARTICLE') {
      navigateTo({
        url: `/pages/course/article/article?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
      })
    } else if (course.contentType === 'VIDEO') {
      navigateTo({
        url: `/pages/course/video/video?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
      })
    }
  }

  // 格式化时间
  const formatTime = (timeStr: string) => {
    const date = new Date(timeStr)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }

  // 格式化数字
  const formatNumber = (num: number) => {
    if (num >= 10000) {
      return `${(num / 10000).toFixed(1)}万`
    }
    return num.toString()
  }

  if (loading) {
    return (
      <View className='collection-container'>
        <View className='loading-state'>
          <Text className='loading-text'>加载中...</Text>
        </View>
      </View>
    )
  }

  if (!collection) {
    return (
      <View className='collection-container'>
        <View className='error-state'>
          <Text className='error-text'>加载失败</Text>
          <Button className='retry-btn' onTap={loadCollectionDetail}>
            重试
          </Button>
        </View>
      </View>
    )
  }

  return (
    <View className='collection-container'>
      {/* 合集信息 */}
      <View className='collection-header'>
        <View className='header-content'>
          <Text className='collection-title'>{collection.title}</Text>
          <Text className='collection-desc'>{collection.description}</Text>
          
          <View className='collection-meta'>
            <View className='meta-row'>
              <Text className='meta-label'>作者：</Text>
              <Text className='meta-value'>{collection.author}</Text>
            </View>
            <View className='meta-row'>
              <Text className='meta-label'>难度：</Text>
              <Text className='meta-value'>{collection.difficultyLevel}</Text>
            </View>
            <View className='meta-row'>
              <Text className='meta-label'>发布：</Text>
              <Text className='meta-value'>{formatTime(collection.publishTime)}</Text>
            </View>
          </View>

          <View className='collection-stats'>
            <View className='stat-item'>
              <Text className='stat-icon'>👁</Text>
              <Text className='stat-text'>{formatNumber(collection.viewCount)}</Text>
            </View>
            <View className='stat-item'>
              <Text className='stat-icon'>👍</Text>
              <Text className='stat-text'>{formatNumber(collection.likeCount)}</Text>
            </View>
            <View className='stat-item'>
              <Text className='stat-icon'>⭐</Text>
              <Text className='stat-text'>{formatNumber(collection.collectCount)}</Text>
            </View>
            <View className='stat-item'>
              <Text className='stat-icon'>📚</Text>
              <Text className='stat-text'>{collection.subCourses.length}课</Text>
            </View>
          </View>
        </View>
      </View>

      {/* 子课程列表 */}
      <ScrollView className='sub-courses-list' scrollY>
        <View className='list-header'>
          <Text className='list-title'>课程列表</Text>
          <Text className='list-count'>共{collection.subCourses.length}个课程</Text>
        </View>

        {collection.subCourses.length === 0 ? (
          <View className='empty-state'>
            <Text className='empty-text'>暂无课程内容</Text>
          </View>
        ) : (
          <View className='sub-courses'>
            {collection.subCourses.map((course, index) => (
              <View
                key={course.id}
                className='sub-course-item'
                onTap={() => handleSubCourseClick(course)}
              >
                <View className='course-index'>
                  <Text className='index-text'>{index + 1}</Text>
                </View>
                
                <View className='course-content'>
                  <View className='course-header'>
                    <Text className='course-title'>{course.title}</Text>
                    <View className='course-badges'>
                      <Text className={`content-badge ${course.contentType?.toLowerCase()}`}>
                        {course.contentType === 'VIDEO' ? '视频' : '图文'}
                      </Text>
                      <Text className='level-badge'>{course.difficultyLevel}</Text>
                    </View>
                  </View>
                  
                  <Text className='course-desc'>{course.description}</Text>
                  
                  <View className='course-meta'>
                    <Text className='meta-text'>作者：{course.author}</Text>
                    <Text className='meta-text'>发布：{formatTime(course.publishTime)}</Text>
                  </View>

                  <View className='course-stats'>
                    <View className='stat-item'>
                      <Text className='stat-icon'>👁</Text>
                      <Text className='stat-text'>{formatNumber(course.viewCount)}</Text>
                    </View>
                    <View className='stat-item'>
                      <Text className='stat-icon'>👍</Text>
                      <Text className='stat-text'>{formatNumber(course.likeCount)}</Text>
                    </View>
                  </View>
                </View>

                <View className='course-arrow'>
                  <Text className='arrow-text'>▶</Text>
                </View>
              </View>
            ))}
          </View>
        )}
      </ScrollView>
    </View>
  )
}

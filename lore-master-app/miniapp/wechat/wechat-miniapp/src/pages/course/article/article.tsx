import { useState, useEffect } from 'react'
import { useRouter, request, showToast, showLoading, hideLoading } from '@tarojs/taro'
import { View, Text, ScrollView, Button, RichText } from '@tarojs/components'
import { buildApiUrl, getApiHeaders } from '../../../config/api'
import './article.css'

// 课程详情数据类型
interface CourseDetailVO {
  id: number
  courseCode: string
  title: string
  description: string
  author: string
  courseType: 'NORMAL' | 'COLLECTION'
  contentType: 'ARTICLE' | 'VIDEO'
  difficultyLevel: string
  contentMarkdown?: string
  contentHtml?: string
  viewCount: number
  likeCount: number
  collectCount: number
  publishTime: string
  tags?: string
  tagList?: string[]
}

export default function Article() {
  const router = useRouter()
  const [course, setCourse] = useState<CourseDetailVO | null>(null)
  const [loading, setLoading] = useState(true)
  const [liked, setLiked] = useState(false)
  const [collected, setCollected] = useState(false)

  const courseId = router.params.courseId
  const title = router.params.title ? decodeURIComponent(router.params.title) : '图文详情'

  useEffect(() => {
    if (courseId) {
      loadCourseDetail()
    }
  }, [courseId])

  // 加载课程详情
  const loadCourseDetail = async () => {
    try {
      setLoading(true)
      showLoading({ title: '加载中...' })

      // 调用getCourseById接口获取课程详情
      const response = await request({
        url: buildApiUrl('/api/consumer/course/getCourseById'),
        method: 'POST',
        data: {
          courseId: parseInt(courseId),
          userId: 'miniapp_user', // TODO: 获取真实用户ID
          includeSubCourses: false
        },
        header: getApiHeaders(),
        timeout: 30000
      })

      console.log('课程详情响应:', response)

      if (response && response.data && response.data.success) {
        const courseDetail = response.data.data

        if (courseDetail.contentType === 'ARTICLE') {
          setCourse(courseDetail)
          // 记录浏览已在后端接口中自动处理
        } else {
          showToast({
            title: '这不是图文内容',
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
      console.error('加载课程详情失败:', error)
      showToast({
        title: '网络错误，请检查网络连接',
        icon: 'error'
      })
    } finally {
      setLoading(false)
      hideLoading()
    }
  }



  // 点赞
  const handleLike = async () => {
    try {
      const response = await request({
        url: buildApiUrl(`/api/consumer/course/like/${courseId}`),
        method: 'POST',
        data: {
          userId: 'miniapp_user' // TODO: 获取真实用户ID
        },
        header: getApiHeaders()
      })

      if (response && response.data && response.data.success) {
        setLiked(!liked)
        if (course) {
          setCourse({
            ...course,
            likeCount: liked ? course.likeCount - 1 : course.likeCount + 1
          })
        }
        showToast({
          title: liked ? '取消点赞' : '点赞成功',
          icon: 'success'
        })
      }
    } catch (error) {
      console.error('点赞失败:', error)
      showToast({
        title: '操作失败',
        icon: 'error'
      })
    }
  }

  // 收藏
  const handleCollect = async () => {
    try {
      const response = await request({
        url: buildApiUrl(`/api/consumer/course/collect/${courseId}`),
        method: 'POST',
        data: {
          userId: 'miniapp_user' // TODO: 获取真实用户ID
        },
        header: getApiHeaders()
      })

      if (response && response.data && response.data.success) {
        setCollected(!collected)
        if (course) {
          setCourse({
            ...course,
            collectCount: collected ? course.collectCount - 1 : course.collectCount + 1
          })
        }
        showToast({
          title: collected ? '取消收藏' : '收藏成功',
          icon: 'success'
        })
      }
    } catch (error) {
      console.error('收藏失败:', error)
      showToast({
        title: '操作失败',
        icon: 'error'
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
      <View className='article-container'>
        <View className='loading-state'>
          <Text className='loading-text'>加载中...</Text>
        </View>
      </View>
    )
  }

  if (!course) {
    return (
      <View className='article-container'>
        <View className='error-state'>
          <Text className='error-text'>加载失败</Text>
          <Button className='retry-btn' onTap={loadCourseDetail}>
            重试
          </Button>
        </View>
      </View>
    )
  }

  return (
    <View className='article-container'>
      {/* 文章头部 */}
      <View className='article-header'>
        <Text className='article-title'>{course.title}</Text>
        
        <View className='article-meta'>
          <View className='meta-row'>
            <Text className='meta-label'>作者：</Text>
            <Text className='meta-value'>{course.author}</Text>
          </View>
          <View className='meta-row'>
            <Text className='meta-label'>难度：</Text>
            <Text className='meta-value'>{course.difficultyLevel}</Text>
          </View>
          <View className='meta-row'>
            <Text className='meta-label'>发布：</Text>
            <Text className='meta-value'>{formatTime(course.publishTime)}</Text>
          </View>
        </View>

        <View className='article-stats'>
          <View className='stat-item'>
            <Text className='stat-icon'>👁</Text>
            <Text className='stat-text'>{formatNumber(course.viewCount)}</Text>
          </View>
          <View className='stat-item'>
            <Text className='stat-icon'>👍</Text>
            <Text className='stat-text'>{formatNumber(course.likeCount)}</Text>
          </View>
          <View className='stat-item'>
            <Text className='stat-icon'>⭐</Text>
            <Text className='stat-text'>{formatNumber(course.collectCount)}</Text>
          </View>
        </View>

        {/* 标签 */}
        {course.tagList && course.tagList.length > 0 && (
          <View className='article-tags'>
            {course.tagList.map((tag, index) => (
              <Text key={index} className='tag-item'>#{tag}</Text>
            ))}
          </View>
        )}
      </View>

      {/* 文章内容 */}
      <ScrollView className='article-content' scrollY>
        {course.description && (
          <View className='article-desc'>
            <Text className='desc-text'>{course.description}</Text>
          </View>
        )}

        {course.contentHtml ? (
          <View className='content-html'>
            <RichText nodes={course.contentHtml} />
          </View>
        ) : course.contentMarkdown ? (
          <View className='content-markdown'>
            <Text className='markdown-text'>{course.contentMarkdown}</Text>
          </View>
        ) : (
          <View className='no-content'>
            <Text className='no-content-text'>暂无内容</Text>
          </View>
        )}
      </ScrollView>

      {/* 底部操作栏 */}
      <View className='article-actions'>
        <Button
          className={`action-btn ${liked ? 'liked' : ''}`}
          onTap={handleLike}
        >
          <Text className='action-icon'>{liked ? '❤️' : '🤍'}</Text>
          <Text className='action-text'>点赞</Text>
        </Button>

        <Button
          className={`action-btn ${collected ? 'collected' : ''}`}
          onTap={handleCollect}
        >
          <Text className='action-icon'>{collected ? '⭐' : '☆'}</Text>
          <Text className='action-text'>收藏</Text>
        </Button>

        <Button className='action-btn' onTap={() => showToast({ title: '分享功能开发中', icon: 'none' })}>
          <Text className='action-icon'>📤</Text>
          <Text className='action-text'>分享</Text>
        </Button>
      </View>
    </View>
  )
}

import { useState } from 'react'
import { useLoad, request, showToast, showLoading, hideLoading, navigateTo } from '@tarojs/taro'
import { View, Text, Input, Button, ScrollView, Picker } from '@tarojs/components'
import { buildApiUrl, getApiHeaders } from '../../config/api'
import './study.css'

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
  subCourseCount?: number
}

interface CoursePageVO {
  courses: CourseVO[]
  currentPage: number
  pageSize: number
  totalPages: number
  totalElements: number
  hasNext: boolean
}

export default function Study() {
  const [searchKeyword, setSearchKeyword] = useState('')
  const [courses, setCourses] = useState<CourseVO[]>([])
  const [loading, setLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)

  // 筛选条件
  const [selectedLevel, setSelectedLevel] = useState('')
  const [selectedType, setSelectedType] = useState('')
  const [selectedContent, setSelectedContent] = useState('')

  // 筛选选项
  const levelOptions = ['', 'L1', 'L2', 'L3', 'L4', 'L5']
  const typeOptions = ['', '普通', '合集']
  const contentOptions = ['', '图文', '视频']

  const levelLabels = ['全部等级', 'L1', 'L2', 'L3', 'L4', 'L5']
  const typeLabels = ['全部类型', '普通', '合集']
  const contentLabels = ['全部内容', '图文', '视频']

  useLoad(() => {
    console.log('Study page loaded.')
    // 添加延迟以避免初始加载过快导致的问题
    setTimeout(() => loadCourses(true), 500)
  })

  // 加载课程列表
  const loadCourses = async (reset = false) => {
    if (loading) return

    const page = reset ? 0 : currentPage

    try {
      setLoading(true)
      if (reset) {
        showLoading({ title: '加载中...' })
      }

      const queryParams = {
        page,
        size: 10,
        publishedOnly: true,
        keyword: searchKeyword.trim() || undefined,
        difficultyLevel: selectedLevel || undefined,
        courseType: selectedType === '普通' ? 'NORMAL' : selectedType === '合集' ? 'COLLECTION' : undefined,
        contentType: selectedContent === '图文' ? 'ARTICLE' : selectedContent === '视频' ? 'VIDEO' : undefined
      }

      console.log('请求参数:', queryParams)

      // 检查API配置是否正确
      const apiUrl = buildApiUrl('/api/consumer/course/queryCourseList')
      console.log('API URL:', apiUrl)

      const response = await request({
        url: apiUrl,
        method: 'POST',
        data: queryParams,
        header: getApiHeaders(),
        // 添加超时设置
        timeout: 30000
      })

      console.log('API响应:', response)

      if (response && response.data && response.data.success) {
        const pageData: CoursePageVO = response.data.data

        if (reset) {
          setCourses(pageData.courses || [])
          setCurrentPage(0)
        } else {
          setCourses(prev => [...prev, ...(pageData.courses || [])])
        }

        setCurrentPage(page + 1)
        setHasMore(!!pageData.hasNext)
      } else {
        const errorMsg = response?.data?.message || '加载失败'
        console.error('API请求失败:', errorMsg)
        showToast({
          title: errorMsg,
          icon: 'error'
        })
        // 确保即使API失败，也有一个空列表而不是undefined
        if (reset) {
          setCourses([])
        }
      }
    } catch (error) {
      console.error('加载课程失败:', error)
      showToast({
        title: '网络错误，请检查网络连接',
        icon: 'error'
      })
      // 确保即使网络错误，也有一个空列表而不是undefined
      if (reset) {
        setCourses([])
      }
    } finally {
      setLoading(false)
      hideLoading()
    }
  }

  // 搜索处理
  const handleSearch = () => {
    loadCourses(true)
  }

  // 筛选条件变化处理
  const handleFilterChange = () => {
    setTimeout(() => loadCourses(true), 100)
  }

  // 加载更多
  const loadMore = () => {
    if (hasMore && !loading) {
      loadCourses(false)
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

  // 处理课程卡片点击
  const handleCourseClick = (course: CourseVO) => {
    console.log('=== 开始处理课程点击 ===')
    console.log('点击课程:', course)
    console.log('课程ID:', course.id)
    console.log('课程标题:', course.title)
    console.log('课程类型:', course.courseType)
    console.log('内容类型:', course.contentType)

    // 先显示一个简单的提示
    showToast({
      title: `点击了课程: ${course.title}`,
      icon: 'none',
      duration: 2000
    })

    // 简化跳转逻辑，先测试基本功能
    setTimeout(() => {
      if (course.courseType === 'COLLECTION') {
        console.log('准备跳转到合集页面')
        const url = `/pages/course/collection/collection?courseId=${course.id}&title=${encodeURIComponent(course.title)}`
        console.log('跳转URL:', url)
        navigateTo({ url })
      } else if (course.contentType === 'ARTICLE') {
        console.log('准备跳转到图文页面')
        const url = `/pages/course/article/article?courseId=${course.id}&title=${encodeURIComponent(course.title)}`
        console.log('跳转URL:', url)
        navigateTo({ url })
      } else if (course.contentType === 'VIDEO') {
        console.log('准备跳转到视频页面')
        const url = `/pages/course/video/video?courseId=${course.id}&title=${encodeURIComponent(course.title)}`
        console.log('跳转URL:', url)
        navigateTo({ url })
      } else {
        console.log('不支持的课程类型')
        showToast({
          title: '暂不支持此类型内容',
          icon: 'none'
        })
      }
    }, 500)

    console.log('=== 课程点击处理完成 ===')
  }

  return (
    <View className='study-container'>
      {/* 搜索框 */}
      <View className='search-section'>
        <View className='search-box'>
          <Input
            className='search-input'
            placeholder='搜索学习内容...'
            value={searchKeyword}
            onInput={(e) => setSearchKeyword(e.detail.value)}
            onConfirm={handleSearch}
          />
          <Button
            className='search-btn'
            onTap={handleSearch}
            disabled={loading}
          >
            搜索
          </Button>
        </View>
      </View>

      {/* 筛选条件 */}
      <View className='filter-section'>
        <View className='filter-row'>
          <View className='filter-item'>
            <Text className='filter-label'>等级：</Text>
            <Picker
              mode='selector'
              range={levelLabels}
              value={levelOptions.indexOf(selectedLevel)}
              onChange={(e) => {
                const index = e.detail.value
                setSelectedLevel(levelOptions[index])
                handleFilterChange()
              }}
            >
              <View className='picker-view'>
                <Text className='picker-text'>{levelLabels[levelOptions.indexOf(selectedLevel)]}</Text>
                <Text className='picker-arrow'>▼</Text>
              </View>
            </Picker>
          </View>

          <View className='filter-item'>
            <Text className='filter-label'>类型：</Text>
            <Picker
              mode='selector'
              range={typeLabels}
              value={typeOptions.indexOf(selectedType)}
              onChange={(e) => {
                const index = e.detail.value
                setSelectedType(typeOptions[index])
                handleFilterChange()
              }}
            >
              <View className='picker-view'>
                <Text className='picker-text'>{typeLabels[typeOptions.indexOf(selectedType)]}</Text>
                <Text className='picker-arrow'>▼</Text>
              </View>
            </Picker>
          </View>

          <View className='filter-item'>
            <Text className='filter-label'>内容：</Text>
            <Picker
              mode='selector'
              range={contentLabels}
              value={contentOptions.indexOf(selectedContent)}
              onChange={(e) => {
                const index = e.detail.value
                setSelectedContent(contentOptions[index])
                handleFilterChange()
              }}
            >
              <View className='picker-view'>
                <Text className='picker-text'>{contentLabels[contentOptions.indexOf(selectedContent)]}</Text>
                <Text className='picker-arrow'>▼</Text>
              </View>
            </Picker>
          </View>
        </View>
      </View>

      {/* 课程列表 */}
      <ScrollView
        className='course-list'
        scrollY
        onScrollToLower={loadMore}
        lowerThreshold={100}
      >
        {courses.length === 0 && !loading ? (
          <View className='empty-state'>
            <Text className='empty-text'>暂无学习内容</Text>
            <Text className='empty-desc'>试试调整搜索条件或筛选条件</Text>
          </View>
        ) : (
          <View className='course-grid'>
            {courses.map((course) => (
              <View key={course.id} className='course-card'>
                {/* 简单的点击测试区域 */}
                <View
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    zIndex: 10,
                    backgroundColor: 'rgba(0,0,0,0.01)'
                  }}
                  onTap={() => {
                    console.log('透明层被点击！', course.id, course.title)
                    showToast({
                      title: `点击了: ${course.title}`,
                      icon: 'none'
                    })
                    handleCourseClick(course)
                  }}
                />
                {/* 课程封面 */}
                <View className='course-cover'>
                  <View className='cover-placeholder'>
                    <Text className='placeholder-text'>
                      {course.contentType === 'VIDEO' ? '📹' : '📄'}
                    </Text>
                  </View>

                  {/* 课程类型标签 */}
                  <View className={`type-tag ${course.courseType.toLowerCase()}`}>
                    {course.courseType === 'COLLECTION' && (
                      <Text className='type-text'>合集</Text>
                    )}
                  </View>

                  {/* 难度等级标签 */}
                  <View className='level-tag'>
                    <Text className='level-text'>{course.difficultyLevel}</Text>
                  </View>
                </View>

                {/* 课程信息 */}
                <View className='course-info'>
                  <Text className='course-title' numberOfLines={2}>
                    {course.title}
                  </Text>

                  <Text className='course-desc' numberOfLines={2}>
                    {course.description}
                  </Text>

                  {/* 作者和时间 */}
                  <View className='course-meta'>
                    <View className='meta-item'>
                      <Text className='meta-label'>作者：</Text>
                      <Text className='meta-value'>{course.author}</Text>
                    </View>
                    <View className='meta-item'>
                      <Text className='meta-label'>时间：</Text>
                      <Text className='meta-value'>{formatTime(course.publishTime)}</Text>
                    </View>
                  </View>

                  {/* 统计信息 */}
                  <View className='course-stats'>
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

                    {/* 合集子课程数量 */}
                    {course.courseType === 'COLLECTION' && course.subCourseCount && (
                      <View className='stat-item'>
                        <Text className='stat-icon'>📚</Text>
                        <Text className='stat-text'>{course.subCourseCount}课</Text>
                      </View>
                    )}
                  </View>

                  {/* 内容类型标识 */}
                  <View className='content-type'>
                    <Text className={`content-tag ${course.contentType?.toLowerCase() || 'unknown'}`}>
                      {course.contentType === 'VIDEO' ? '视频' : course.contentType === 'ARTICLE' ? '图文' : '未知'}
                    </Text>
                  </View>

                  {/* 测试按钮 */}
                  <Button
                    className='test-btn'
                    onTap={(e) => {
                      e.stopPropagation()
                      console.log('测试按钮被点击！')
                      showToast({
                        title: '测试按钮点击成功！',
                        icon: 'success'
                      })
                      handleCourseClick(course)
                    }}
                    style={{
                      marginTop: '10rpx',
                      backgroundColor: '#007aff',
                      color: 'white',
                      fontSize: '24rpx',
                      padding: '10rpx'
                    }}
                  >
                    点击测试
                  </Button>
                </View>
              </View>
            ))}
          </View>
        )}

        {/* 加载更多提示 */}
        {loading && courses.length > 0 && (
          <View className='loading-more'>
            <Text className='loading-text'>加载中...</Text>
          </View>
        )}

        {!hasMore && courses.length > 0 && (
          <View className='no-more'>
            <Text className='no-more-text'>没有更多内容了</Text>
          </View>
        )}
      </ScrollView>
    </View>
  )
}

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

  const levelLabels = ['全部', 'L1', 'L2', 'L3', 'L4', 'L5']
  const typeLabels = ['全部', '普通', '合集']
  const contentLabels = ['全部', '图文', '视频']

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



  // 处理课程卡片点击 - 跳转到详情页面
  const handleCourseClick = (course: CourseVO) => {
    console.log('=== 点击课程卡片 ===')
    console.log('课程对象完整信息:', JSON.stringify(course, null, 2))
    console.log('课程编码:', course.courseCode)
    console.log('课程标题:', course.title)
    console.log('课程类型:', course.courseType)
    console.log('内容类型:', course.contentType)
    console.log('课程ID:', course.id)

    // 显示跳转提示
    showToast({
      title: `正在打开: ${course.title}`,
      icon: 'loading',
      duration: 1500
    })

    try {
      let url = ''

      // 根据课程类型确定跳转URL，使用courseCode而不是courseId
      if (course.courseType === 'COLLECTION') {
        console.log('跳转到合集页面')
        url = `/pages/course/collection/collection?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
      } else if (course.contentType === 'ARTICLE') {
        console.log('跳转到图文页面')
        url = `/pages/course/article/article?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
      } else if (course.contentType === 'VIDEO') {
        console.log('跳转到视频页面')
        url = `/pages/course/video/video?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
      } else {
        console.log('不支持的课程类型')
        showToast({
          title: '暂不支持此类型内容',
          icon: 'none'
        })
        return
      }

      console.log('跳转URL:', url)

      // 执行跳转
      navigateTo({
        url,
        success: (res) => {
          console.log('跳转成功:', res)
        },
        fail: (err) => {
          console.error('跳转失败:', err)
          showToast({
            title: '跳转失败，请重试',
            icon: 'error'
          })
        }
      })

    } catch (error) {
      console.error('处理跳转时发生错误:', error)
      showToast({
        title: '发生错误，请重试',
        icon: 'error'
      })
    }

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
          <View className='course-list-container'>
            {courses.map((course, index) => (
              <View
                key={course.id}
                className='course-list-item'
                onTap={() => {
                  console.log('=== 列表项点击事件触发 ===')
                  console.log('点击的课程索引:', index)
                  console.log('点击的课程编码:', course.courseCode)
                  console.log('点击的课程标题:', course.title)
                  handleCourseClick(course)
                }}
              >
                {/* 左侧图标 */}
                <View className='course-icon'>
                  <Text className='icon-text'>
                    {course.contentType === 'VIDEO' ? '📹' : '📄'}
                  </Text>
                  {course.courseType === 'COLLECTION' && (
                    <Text className='collection-badge'>合集</Text>
                  )}
                </View>

                {/* 中间内容 */}
                <View className='course-content'>
                  <View className='title-row'>
                    <Text className='course-title' numberOfLines={1}>
                      {course.title}
                    </Text>
                    <View className='title-meta'>
                      <Text className='level-tag'>{course.difficultyLevel}</Text>
                      <Text className='time-text'>{formatTime(course.publishTime)}</Text>
                    </View>
                  </View>
                  <Text className='course-desc' numberOfLines={2}>
                    {course.description}
                  </Text>
                </View>

                {/* 右侧箭头 */}
                <View className='course-arrow'>
                  <Text className='arrow-text'>›</Text>
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

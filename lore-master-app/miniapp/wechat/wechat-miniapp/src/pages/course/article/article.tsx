import { useState, useEffect } from 'react'
import { useRouter, request, showToast, showLoading, hideLoading, getStorageSync } from '@tarojs/taro'
import { View, Text, ScrollView, Button } from '@tarojs/components'
import { buildApiUrl, getApiHeaders } from '../../../config/api'
import './article.css'

// 导入 Taro HTML 样式
import '@tarojs/taro/html.css'

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
  const [parsedContent, setParsedContent] = useState<string>('')

  const courseCode = router.params.courseCode
  const courseId = router.params.courseId
  const title = router.params.title ? decodeURIComponent(router.params.title) : '图文详情'

  // 简化的 Markdown 解析函数，专注于表格渲染
  const parseMarkdown = (markdown: string): string => {
    try {
      let html = markdown
      
      // 首先处理表格
      html = parseMarkdownTables(html)
      
      // 处理标题
      html = html.replace(/^### (.*)$/gm, '<h3 class="h3">$1</h3>')
      html = html.replace(/^## (.*)$/gm, '<h2 class="h2">$1</h2>')
      html = html.replace(/^# (.*)$/gm, '<h1 class="h1">$1</h1>')
      
      // 处理加粗和斜体
      html = html.replace(/\*\*([^*]+)\*\*/g, '<strong class="strong">$1</strong>')
      html = html.replace(/\*([^*]+)\*/g, '<em class="em">$1</em>')
      
      // 处理内联代码
      html = html.replace(/`([^`]+)`/g, '<code class="code">$1</code>')
      
      // 处理引用块
      html = html.replace(/^> (.*)$/gm, '<blockquote class="blockquote">$1</blockquote>')
      
      // 处理换行
      html = html.replace(/\n\n/g, '</p><p class="p">')
      html = html.replace(/\n/g, '<br>')
      html = '<p class="p">' + html + '</p>'
      
      return html
    } catch (error) {
      console.error('Markdown 解析失败:', error)
      // 如果解析失败，返回原始内容但处理换行
      return markdown.replace(/\n/g, '<br>')
    }
  }
  
  // 简化的表格解析函数
  const parseMarkdownTables = (markdown: string): string => {
    // 匹配简单的 Markdown 表格
    const tableRegex = /\n(\|.+\|)\s*\n(\|[-:\s]+\|)\s*\n((\|.+\|\s*\n?)+)/g
    
    return markdown.replace(tableRegex, (match, headerRow, separatorRow, bodyRows) => {
      try {
        // 解析表头
        const headers = headerRow.split('|').slice(1, -1).map(cell => cell.trim())
        
        // 解析表格体
        const rows = bodyRows.trim().split('\n').map(row => {
          return row.split('|').slice(1, -1).map(cell => cell.trim())
        })
        
        // 生成 HTML
        let html = '<table class="table">\n'
        html += '  <thead><tr class="tr">\n'
        headers.forEach(header => {
          html += `    <th class="th">${header}</th>\n`
        })
        html += '  </tr></thead>\n  <tbody>\n'
        
        rows.forEach(row => {
          html += '    <tr class="tr">\n'
          row.forEach(cell => {
            html += `      <td class="td">${cell}</td>\n`
          })
          html += '    </tr>\n'
        })
        
        html += '  </tbody>\n</table>'
        return html
      } catch (error) {
        console.error('表格解析错误:', error)
        return match
      }
    })
  }

  // 添加调试日志
  console.log('Article页面参数:', router.params)
  console.log('获取到的courseCode:', courseCode)
  console.log('获取到的courseId:', courseId)
  console.log('获取到的title:', title)

  useEffect(() => {
    if (courseCode || courseId) {
      console.log('开始加载课程详情，courseCode:', courseCode, 'courseId:', courseId)
      loadCourseDetail()
    } else {
      console.error('courseCode和courseId都为空，无法加载课程详情')
      showToast({
        title: '课程参数错误',
        icon: 'error'
      })
    }
  }, [courseCode, courseId])

  // 加载课程详情
  const loadCourseDetail = async () => {
    try {
      setLoading(true)
      showLoading({ title: '加载中...' })

      let response

      // 获取token
      let token = ''
      try {
        token = getStorageSync('token')
      } catch (error) {
        console.warn('获取token失败:', error)
      }

      // 根据参数类型选择不同的API
      if (courseCode) {
        console.log('准备调用getCourseByCode API，courseCode:', courseCode)
        response = await request({
          url: buildApiUrl('/api/consumer/course/getCourseByCode'),
          method: 'POST',
          data: {
            courseCode: courseCode,
            includeSubCourses: false
          },
          header: getApiHeaders(token),
          timeout: 30000
        })
      } else if (courseId) {
        console.log('准备调用getCourseById API，courseId:', courseId)
        response = await request({
          url: buildApiUrl('/api/consumer/course/getCourseById'),
          method: 'POST',
          data: {
            courseId: parseInt(courseId),
            includeSubCourses: false
          },
          header: getApiHeaders(token),
          timeout: 30000
        })
      } else {
        throw new Error('缺少课程参数')
      }

      console.log('课程详情响应:', response)

      if (response && response.data && response.data.success) {
        const courseDetail = response.data.data

        if (courseDetail.contentType === 'ARTICLE') {
          setCourse(courseDetail)
          
          // 简化内容处理逻辑
          if (courseDetail.contentHtml) {
            // 如果有 HTML 内容，直接使用
            setParsedContent(courseDetail.contentHtml)
          } else if (courseDetail.contentMarkdown) {
            // 否则解析 Markdown 内容
            const htmlContent = parseMarkdown(courseDetail.contentMarkdown)
            setParsedContent(htmlContent)
          }
          
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
    if (!course) return

    try {
      const response = await request({
        url: buildApiUrl(`/api/consumer/course/like/${course.id}`),
        method: 'POST',
        data: {},
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
    if (!course) return

    try {
      const response = await request({
        url: buildApiUrl(`/api/consumer/course/collect/${course.id}`),
        method: 'POST',
        data: {},
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

        {parsedContent ? (
          <View
            className='taro_html content-html'
            dangerouslySetInnerHTML={{ __html: parsedContent }}
          />
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

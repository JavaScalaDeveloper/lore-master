import { useState, useEffect } from 'react'
import { useRouter, request, showToast, showLoading, hideLoading, getStorageSync } from '@tarojs/taro'
import { View, Text, Button, ScrollView, Image } from '@tarojs/components'
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
  const [markdownElements, setMarkdownElements] = useState<any[]>([])

  const courseCode = router.params.courseCode
  const courseId = router.params.courseId

  // 组件加载时获取课程详情
  useEffect(() => {
    loadCourseDetail()
  }, [courseCode, courseId])

  // 图片代理处理函数
  const processImageUrls = (html: string): string => {
    // 匹配所有img标签中的src属性
    return html.replace(/<img([^>]*?)src=["']([^"']*)["']([^>]*?)>/gi, (match, before, src, after) => {
      console.log('Processing miniapp image:', src)
      // 检查是否是外部链接（http/https开头）
      if (src.startsWith('http')) {
        // 使用免费的图片代理服务
        const proxySrc = `https://images.weserv.nl/?url=${encodeURIComponent(src)}`
        console.log('Converting miniapp image to proxy URL:', proxySrc)
        return `<img${before}src="${proxySrc}"${after} style="max-width: 100%; height: auto; border-radius: 6px; margin: 8px 0;">`
      }
      return match
    })
  }


  // 原生Markdown表格渲染组件
  const renderMarkdownTable = (tableContent: string) => {
    console.log('渲染表格内容:', tableContent)

    const lines = tableContent.trim().split('\n').filter(line => line.trim() && line.includes('|'))
    if (lines.length < 2) return null

    // 找到分隔行
    let headerIndex = 0
    let dataStartIndex = 1

    for (let i = 1; i < lines.length; i++) {
      if (lines[i].match(/\|[\s-:|]+\|/)) {
        dataStartIndex = i + 1
        break
      }
    }

    // 解析表头
    const headers = lines[headerIndex].split('|')
      .map(cell => cell.trim())
      .filter(cell => cell !== '')

    // 解析数据行
    const dataLines = lines.slice(dataStartIndex)
    const rows = dataLines.map(line => {
      return line.split('|')
        .map(cell => cell.trim())
        .filter(cell => cell !== '')
    })

    if (headers.length === 0 || rows.length === 0) return null

    return (
      <View className='markdown-table-container' style={{ margin: '16px 0', overflowX: 'auto' }}>
        <View className='markdown-table' style={{
          width: '100%',
          border: '1px solid #e2e8f0',
          borderRadius: '8px',
          background: 'white',
          fontSize: '14px'
        }}>
          {/* 表头 */}
          <View className='table-header' style={{
            display: 'flex',
            background: '#f7fafc',
            borderBottom: '2px solid #e2e8f0'
          }}>
            {headers.map((header, index) => (
              <View key={index} className='table-header-cell' style={{
                flex: 1,
                padding: '12px 16px',
                borderRight: index < headers.length - 1 ? '1px solid #e2e8f0' : 'none',
                minWidth: '100px'
              }}>
                <Text style={{ fontWeight: 'bold', color: '#2d3748', fontSize: '14px' }}>
                  {header}
                </Text>
              </View>
            ))}
          </View>

          {/* 表格体 */}
          {rows.map((row, rowIndex) => (
            <View key={rowIndex} className='table-row' style={{
              display: 'flex',
              borderBottom: rowIndex < rows.length - 1 ? '1px solid #e2e8f0' : 'none',
              background: rowIndex % 2 === 0 ? '#ffffff' : '#f9f9f9'
            }}>
              {headers.map((_, cellIndex) => (
                <View key={cellIndex} className='table-cell' style={{
                  flex: 1,
                  padding: '10px 16px',
                  borderRight: cellIndex < headers.length - 1 ? '1px solid #e2e8f0' : 'none',
                  minWidth: '100px',
                  display: 'flex',
                  alignItems: 'center'
                }}>
                  <Text style={{ color: '#4a5568', fontSize: '14px', lineHeight: '1.4' }}>
                    {row[cellIndex] || ''}
                  </Text>
                </View>
              ))}
            </View>
          ))}
        </View>
      </View>
    )
  }

  // 图片代理处理函数
  const processImageUrl = (url: string): string => {
    if (url.startsWith('http')) {
      return `https://images.weserv.nl/?url=${encodeURIComponent(url)}`
    }
    return url
  }

  // 改进的Markdown解析函数，支持多种内容类型
  const parseMarkdownWithNativeTable = (markdown: string) => {
    console.log('开始解析Markdown内容:', markdown.substring(0, 200) + '...')

    const elements: any[] = []
    const lines = markdown.split('\n')
    let currentIndex = 0

    while (currentIndex < lines.length) {
      const line = lines[currentIndex]

      // 跳过空行
      if (!line.trim()) {
        currentIndex++
        continue
      }

      // 检查是否是表格开始
      if (line.includes('|') && line.trim().startsWith('|') && line.trim().endsWith('|')) {
        const nextLine = lines[currentIndex + 1]
        if (nextLine && (nextLine.includes('---') || nextLine.includes(':-'))) {
          console.log('发现表格，开始解析...')

          // 收集表格行
          const tableLines = [line]
          let tableIndex = currentIndex + 1

          // 跳过分隔行
          if (lines[tableIndex] && (lines[tableIndex].includes('---') || lines[tableIndex].includes(':-'))) {
            tableIndex++
          }

          // 收集数据行
          while (tableIndex < lines.length &&
                 lines[tableIndex] &&
                 lines[tableIndex].includes('|') &&
                 lines[tableIndex].trim().startsWith('|') &&
                 lines[tableIndex].trim().endsWith('|')) {
            tableLines.push(lines[tableIndex])
            tableIndex++
          }

          if (tableLines.length >= 2) {
            const tableContent = tableLines.join('\n')
            console.log('表格内容:', tableContent)
            elements.push({
              type: 'table',
              content: tableContent
            })
            currentIndex = tableIndex
            continue
          }
        }
      }

      // 检查Markdown图片
      const imageMatch = line.match(/!\[([^\]]*)\]\(([^)]+)\)/)
      if (imageMatch) {
        const [, alt, src] = imageMatch
        elements.push({
          type: 'image',
          alt: alt || '',
          src: processImageUrl(src)
        })
        currentIndex++
        continue
      }

      // 检查HTML图片标签 - 支持多行和复杂属性
      if (line.includes('<img') && line.includes('src=')) {
        // 收集完整的img标签（可能跨多行）
        let imgTagContent = line
        let imgIndex = currentIndex + 1
        
        // 如果img标签没有闭合，继续收集下一行
        while (imgIndex < lines.length && !imgTagContent.includes('>')) {
          imgTagContent += ' ' + lines[imgIndex].trim()
          imgIndex++
        }
        
        // 提取src属性
        const srcMatch = imgTagContent.match(/src=["']([^"']+)["']/)
        if (srcMatch) {
          const src = srcMatch[1]
          // 提取alt属性
          const altMatch = imgTagContent.match(/alt=["']([^"']*)["']/)
          const alt = altMatch ? altMatch[1] : ''
          
          elements.push({
            type: 'image',
            alt: alt,
            src: processImageUrl(src)
          })
          currentIndex = imgIndex
          continue
        }
      }

      // 检查标题
      if (line.startsWith('###')) {
        elements.push({
          type: 'h3',
          content: line.substring(3).trim()
        })
      } else if (line.startsWith('##')) {
        elements.push({
          type: 'h2',
          content: line.substring(2).trim()
        })
      } else if (line.startsWith('#')) {
        elements.push({
          type: 'h1',
          content: line.substring(1).trim()
        })
      }
      // 检查代码块
      else if (line.startsWith('```')) {
        const codeLines: string[] = []
        let codeIndex = currentIndex + 1
        
        // 收集代码内容
        while (codeIndex < lines.length && !lines[codeIndex].startsWith('```')) {
          codeLines.push(lines[codeIndex])
          codeIndex++
        }
        
        if (codeIndex < lines.length) { // 找到结束标记
          // 过滤掉语言标记行和空行
          const filteredLines = codeLines.filter(codeLine => {
            const trimmed = codeLine.trim()
            // 过滤掉单独的语言标记行（如 "java", "javascript" 等）
            return trimmed !== '' && 
                   !trimmed.match(/^(java|javascript|python|cpp|c\+\+|html|css|sql|bash|shell|json|xml|yaml)$/i)
          })
          
          const codeContent = filteredLines.join('\n')
          
          if (codeContent.trim()) {
            elements.push({
              type: 'code',
              content: codeContent,
              language: line.substring(3).trim()
            })
          }
          currentIndex = codeIndex + 1
          continue
        }
      }
      // 检查数字列表
      else if (line.match(/^[\s]*\d+\.\s+/)) {
        const match = line.match(/^[\s]*(\d+)\.\s+(.*)/)
        if (match) {
          elements.push({
            type: 'numbered-list-item',
            number: match[1],
            content: match[2].trim()
          })
        }
      }
      // 检查普通列表项
      else if (line.match(/^[\s]*[-*+]\s+/)) {
        elements.push({
          type: 'list-item',
          content: line.replace(/^[\s]*[-*+]\s+/, '').trim()
        })
      }
      // 检查引用
      else if (line.startsWith('>')) {
        elements.push({
          type: 'quote',
          content: line.substring(1).trim()
        })
      }
      // 普通段落 - 检查是否包含格式化内容
      else {
        const content = line.trim()
        // 检查是否包含格式化元素（粗体、链接、URL）
        if (content.includes('**') || content.includes('[') && content.includes('](') || 
            content.match(/https?:\/\/[^\s]+/)) {
          elements.push({
            type: 'formatted-paragraph',
            content: content
          })
        } else {
          elements.push({
            type: 'paragraph',
            content: content
          })
        }
      }

      currentIndex++
    }

    return elements
  }

  // 处理内联格式（粗体、链接、URL）
  const renderFormattedText = (text: string) => {
    let partIndex = 0
    
    // 创建一个统一的处理函数
    const processText = (inputText: string) => {
      const elements: JSX.Element[] = []
      let currentText = inputText
      let currentIndex = 0
      
      // 匹配所有格式化元素：粗体、Markdown链接、纯URL
      const formatRegex = /(\*\*([^*]+)\*\*|\[([^\]]+)\]\(([^)]+)\)|(https?:\/\/[^\s<>]+))/g
      let match
      
      while ((match = formatRegex.exec(currentText)) !== null) {
        // 添加匹配前的普通文本
        if (match.index > currentIndex) {
          const beforeText = currentText.substring(currentIndex, match.index)
          if (beforeText) {
            elements.push(
              <Text key={`text-${partIndex++}`} className="markdown-text">
                {beforeText}
              </Text>
            )
          }
        }
        
        // 判断匹配的类型并添加相应元素
        if (match[2]) {
          // 粗体文本 **text**
          elements.push(
            <Text key={`bold-${partIndex++}`} className="markdown-bold">
              {match[2]}
            </Text>
          )
        } else if (match[3] && match[4]) {
          // Markdown链接 [text](url)
          elements.push(
            <Text key={`link-${partIndex++}`} className="markdown-link">
              {match[3]}
            </Text>
          )
        } else if (match[5]) {
          // 纯URL
          elements.push(
            <Text key={`url-${partIndex++}`} className="markdown-url">
              {match[5]}
            </Text>
          )
        }
        
        currentIndex = match.index + match[0].length
      }
      
      // 添加剩余的普通文本
      if (currentIndex < currentText.length) {
        const finalText = currentText.substring(currentIndex)
        if (finalText) {
          elements.push(
            <Text key={`text-${partIndex++}`} className="markdown-text">
              {finalText}
            </Text>
          )
        }
      }
      
      return elements
    }
    
    const processedElements = processText(text)
    return processedElements.length > 0 ? processedElements : [<Text key="default" className="markdown-text">{text}</Text>]
  }

  // 渲染不同类型的内容元素
  const renderContentElement = (element: any, index: number) => {
    switch (element.type) {
      case 'table':
        return (
          <View key={index} className="content-element">
            {renderMarkdownTable(element.content)}
          </View>
        )
      
      case 'image':
        return (
          <View key={index} className="content-element">
            <Image
              className="markdown-image"
              src={element.src}
              mode="widthFix"
              lazyLoad
              onError={() => console.log('图片加载失败:', element.src)}
            />
            {element.alt && (
              <Text className="markdown-image-alt">{element.alt}</Text>
            )}
          </View>
        )
      
      case 'h1':
        return (
          <View key={index} className="content-element">
            <Text className="markdown-h1">{element.content}</Text>
          </View>
        )
      
      case 'h2':
        return (
          <View key={index} className="content-element">
            <Text className="markdown-h2">{element.content}</Text>
          </View>
        )
      
      case 'h3':
        return (
          <View key={index} className="content-element">
            <Text className="markdown-h3">{element.content}</Text>
          </View>
        )
      
      case 'code':
        return (
          <View key={index} className="content-element">
            <View className="markdown-code-block">
              <Text className="markdown-code">{element.content}</Text>
            </View>
          </View>
        )
      
      case 'inline-code':
        return (
          <View key={index} className="content-element">
            <Text className="markdown-inline-code">{element.content}</Text>
          </View>
        )
      
      case 'numbered-list-item':
        return (
          <View key={index} className="content-element">
            <View className="markdown-list-item">
              <Text className="markdown-list-number">{element.number}. </Text>
              <Text className="markdown-list-text">{element.content}</Text>
            </View>
          </View>
        )
      
      case 'list-item':
        return (
          <View key={index} className="content-element">
            <View className="markdown-list-item">
              <Text className="markdown-list-bullet">• </Text>
              <Text className="markdown-list-text">{element.content}</Text>
            </View>
          </View>
        )
      
      case 'quote':
        return (
          <View key={index} className="content-element">
            <View className="markdown-quote">
              <Text className="markdown-quote-text">{element.content}</Text>
            </View>
          </View>
        )
      
      case 'paragraph-with-links':
        return (
          <View key={index} className="content-element">
            <View className="markdown-paragraph-container">
              {renderFormattedText(element.content)}
            </View>
          </View>
        )
      
      case 'formatted-paragraph':
        return (
          <View key={index} className="content-element">
            <View className="markdown-paragraph-container">
              {renderFormattedText(element.content)}
            </View>
          </View>
        )
      
      case 'paragraph':
      default:
        return (
          <View key={index} className="content-element">
            <Text className="markdown-paragraph">{element.content}</Text>
          </View>
        )
    }
  }


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

          // 使用新的内容处理逻辑
          if (courseDetail.contentMarkdown) {
            // 优先使用 Markdown 内容，支持原生表格渲染
            const elements = parseMarkdownWithNativeTable(courseDetail.contentMarkdown)
            setMarkdownElements(elements)
            setParsedContent('') // 清空HTML内容，使用原生渲染
          } else if (courseDetail.contentHtml) {
            // 如果有 HTML 内容，处理其中的图片后使用
            const processedHtml = processImageUrls(courseDetail.contentHtml)
            setParsedContent(processedHtml)
            setMarkdownElements([]) // 清空原生元素
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

        {markdownElements.length > 0 ? (
          <View className='content-native'>
            {markdownElements.map((element, index) => renderContentElement(element, index))}
          </View>
        ) : parsedContent ? (
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

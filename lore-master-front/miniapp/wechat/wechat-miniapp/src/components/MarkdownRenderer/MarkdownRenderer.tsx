import { View, Text, ScrollView } from '@tarojs/components'
import './MarkdownRenderer.css'

interface MarkdownRendererProps {
  content: string
  className?: string
}

const MarkdownRenderer = ({ content, className = '' }: MarkdownRendererProps) => {
  // 增强的Markdown解析器
  const parseMarkdown = (text: string) => {
    if (!text) return []

    const lines = text.split('\n')
    const elements: any[] = []
    let currentList: any[] = []
    let inCodeBlock = false
    let codeBlockContent = ''
    let codeBlockLanguage = ''

    const flushList = () => {
      if (currentList.length > 0) {
        elements.push({
          type: 'list',
          items: [...currentList]
        })
        currentList = []
      }
    }

    // 解析行内格式（粗体、斜体、行内代码）
    const parseInlineFormats = (text: string) => {
      const parts: any[] = []
      let currentText = text

      while (currentText.length > 0) {
        // 查找下一个格式标记
        const boldMatch = currentText.match(/\*\*(.*?)\*\*/)
        const italicMatch = currentText.match(/\*(.*?)\*/)
        const codeMatch = currentText.match(/`(.*?)`/)

        const matches = [
          { match: boldMatch, type: 'bold', marker: '**' },
          { match: italicMatch, type: 'italic', marker: '*' },
          { match: codeMatch, type: 'code', marker: '`' }
        ].filter(m => m.match).sort((a, b) => a.match!.index! - b.match!.index!)

        if (matches.length === 0) {
          // 没有更多格式，添加剩余文本
          if (currentText.trim()) {
            parts.push({ type: 'text', content: currentText })
          }
          break
        }

        const firstMatch = matches[0]
        const match = firstMatch.match!

        // 添加匹配前的文本
        if (match.index! > 0) {
          const beforeText = currentText.substring(0, match.index!)
          if (beforeText.trim()) {
            parts.push({ type: 'text', content: beforeText })
          }
        }

        // 添加格式化文本
        parts.push({
          type: firstMatch.type,
          content: match[1]
        })

        // 继续处理剩余文本
        currentText = currentText.substring(match.index! + match[0].length)
      }

      return parts
    }

    lines.forEach((line, index) => {
      // 代码块处理
      if (line.startsWith('```')) {
        if (inCodeBlock) {
          // 结束代码块
          elements.push({
            type: 'codeblock',
            content: codeBlockContent,
            language: codeBlockLanguage
          })
          inCodeBlock = false
          codeBlockContent = ''
          codeBlockLanguage = ''
        } else {
          // 开始代码块
          flushList()
          inCodeBlock = true
          codeBlockLanguage = line.slice(3).trim()
        }
        return
      }

      if (inCodeBlock) {
        codeBlockContent += (codeBlockContent ? '\n' : '') + line
        return
      }

      // 标题处理
      if (line.startsWith('# ')) {
        flushList()
        elements.push({
          type: 'h1',
          content: line.slice(2).trim(),
          parts: parseInlineFormats(line.slice(2).trim())
        })
        return
      }

      if (line.startsWith('## ')) {
        flushList()
        elements.push({
          type: 'h2',
          content: line.slice(3).trim(),
          parts: parseInlineFormats(line.slice(3).trim())
        })
        return
      }

      if (line.startsWith('### ')) {
        flushList()
        elements.push({
          type: 'h3',
          content: line.slice(4).trim(),
          parts: parseInlineFormats(line.slice(4).trim())
        })
        return
      }

      // 列表处理
      if (line.startsWith('• ') || line.startsWith('- ') || line.startsWith('* ')) {
        currentList.push(line.slice(2).trim())
        return
      }

      // 数字列表处理
      const numberListMatch = line.match(/^\d+\.\s+(.+)/)
      if (numberListMatch) {
        currentList.push(numberListMatch[1])
        return
      }

      // 空行处理
      if (line.trim() === '') {
        flushList()
        if (elements.length > 0 && elements[elements.length - 1].type !== 'break') {
          elements.push({ type: 'break' })
        }
        return
      }

      // 表格处理
      if (line.includes('|') && line.trim().startsWith('|') && line.trim().endsWith('|')) {
        // 检查是否是表格的开始
        const nextLine = lines[index + 1]
        if (nextLine && (nextLine.includes('---') || nextLine.includes(':-'))) {
          // 这是一个表格的开始
          flushList()
          
          const tableLines: string[] = []
          tableLines.push(line) // 表头
          
          // 跳过分隔行
          let currentIndex = index + 1
          if (lines[currentIndex] && (lines[currentIndex].includes('---') || lines[currentIndex].includes(':-'))) {
            currentIndex++
          }
          
          // 收集表格数据行
          while (currentIndex < lines.length && 
                 lines[currentIndex] && 
                 lines[currentIndex].includes('|') && 
                 lines[currentIndex].trim().startsWith('|') && 
                 lines[currentIndex].trim().endsWith('|')) {
            tableLines.push(lines[currentIndex])
            currentIndex++
          }
          
          if (tableLines.length >= 2) {
            // 解析表格
            const headers = tableLines[0].split('|').slice(1, -1).map(cell => cell.trim())
            const rows = tableLines.slice(1).map(row => 
              row.split('|').slice(1, -1).map(cell => cell.trim())
            )
            
            elements.push({
              type: 'table',
              headers,
              rows
            })
            
            // 跳过已处理的行
            for (let i = index + 1; i < currentIndex; i++) {
              if (i < lines.length) {
                lines[i] = '' // 标记为已处理
              }
            }
            return
          }
        }
      }

      // 普通段落
      flushList()

      // 处理行内格式
      elements.push({
        type: 'paragraph',
        content: line.trim(),
        parts: parseInlineFormats(line.trim())
      })
    })

    flushList()
    return elements
  }



  const renderInlineContent = (parts: any[]) => {
    return parts.map((part, index) => {
      switch (part.type) {
        case 'bold':
          return (
            <Text key={index} className='md-bold'>
              {part.content}
            </Text>
          )
        case 'italic':
          return (
            <Text key={index} className='md-italic'>
              {part.content}
            </Text>
          )
        case 'code':
          return (
            <Text key={index} className='md-inline-code'>
              {part.content}
            </Text>
          )
        default:
          return (
            <Text key={index}>
              {part.content}
            </Text>
          )
      }
    })
  }

  const elements = parseMarkdown(content)

  return (
    <View className={`markdown-renderer ${className}`}>
      {elements.map((element, index) => {
        switch (element.type) {
          case 'h1':
            return (
              <View key={index} className='md-h1'>
                {element.parts ? renderInlineContent(element.parts) : (
                  <Text className='md-h1-text'>{element.content}</Text>
                )}
              </View>
            )
          case 'h2':
            return (
              <View key={index} className='md-h2'>
                {element.parts ? renderInlineContent(element.parts) : (
                  <Text className='md-h2-text'>{element.content}</Text>
                )}
              </View>
            )
          case 'h3':
            return (
              <View key={index} className='md-h3'>
                {element.parts ? renderInlineContent(element.parts) : (
                  <Text className='md-h3-text'>{element.content}</Text>
                )}
              </View>
            )
          case 'paragraph':
            return (
              <View key={index} className='md-paragraph'>
                {element.parts ? renderInlineContent(element.parts) : (
                  <Text className='md-paragraph-text'>{element.content}</Text>
                )}
              </View>
            )
          case 'list':
            return (
              <View key={index} className='md-list'>
                {element.items.map((item: string, itemIndex: number) => (
                  <View key={itemIndex} className='md-list-item'>
                    <Text className='md-list-bullet'>•</Text>
                    <Text className='md-list-content'>{item}</Text>
                  </View>
                ))}
              </View>
            )
          case 'codeblock':
            return (
              <ScrollView 
                key={index} 
                className='md-codeblock-container'
                scrollX
                showScrollbar={false}
              >
                <View className='md-codeblock'>
                  {element.language && (
                    <Text className='md-codeblock-language'>
                      {element.language}
                    </Text>
                  )}
                  <Text className='md-codeblock-content'>
                    {element.content}
                  </Text>
                </View>
              </ScrollView>
            )
          case 'table':
            return (
              <View key={index} className='md-table-container'>
                <View className='md-table'>
                  {/* 表头 */}
                  <View className='md-table-header'>
                    {element.headers.map((header: string, headerIndex: number) => (
                      <View key={headerIndex} className='md-table-header-cell'>
                        <Text className='md-table-header-text'>{header}</Text>
                      </View>
                    ))}
                  </View>
                  {/* 表格体 */}
                  {element.rows.map((row: string[], rowIndex: number) => (
                    <View key={rowIndex} className='md-table-row'>
                      {row.map((cell: string, cellIndex: number) => (
                        <View key={cellIndex} className='md-table-cell'>
                          <Text className='md-table-cell-text'>{cell}</Text>
                        </View>
                      ))}
                    </View>
                  ))}
                </View>
              </View>
            )
          case 'break':
            return <View key={index} className='md-break' />
          default:
            return null
        }
      })}
    </View>
  )
}

export default MarkdownRenderer

import { useState, useEffect, useRef } from 'react'
import { useRouter, useLoad, request, showToast, navigateBack, getStorageSync, createCanvasContext, getSystemInfo } from '@tarojs/taro'
import { View, Text, Canvas, Button } from '@tarojs/components'
import { API_CONFIG, getApiHeaders, apiLog } from '../../config/api'
import './mindmap.css'

interface TreeNode {
  nodeCode: string
  nodeName: string
  nodeType: string
  levelDepth: number
  levelType?: string
  difficultyLevel: string
  estimatedHours: number
  description?: string
  sortOrder: number
  children?: TreeNode[]
}

interface SkillTreeData {
  rootCode: string
  rootName: string
  children: TreeNode[]
}

interface NodePosition {
  x: number
  y: number
  width: number
  height: number
  node: TreeNode
  color: string
  radius: number
}

// 颜色主题配置
const COLOR_THEMES = {
  ROOT: '#FF6B6B',      // 红色 - 根节点
  LEVEL: '#4ECDC4',     // 青色 - 层级节点
  BRANCH: '#45B7D1',    // 蓝色 - 分支节点
  LEAF: '#96CEB4',      // 绿色 - 叶子节点
  DEFAULT: '#FFEAA7'    // 黄色 - 默认
}

// 难度等级颜色
const DIFFICULTY_COLORS = {
  BEGINNER: '#2ECC71',    // 绿色 - 初级
  INTERMEDIATE: '#F39C12', // 橙色 - 中级
  ADVANCED: '#E74C3C',    // 红色 - 高级
  EXPERT: '#9B59B6'       // 紫色 - 专家
}

const Mindmap = () => {
  const router = useRouter()
  const canvasRef = useRef<any>(null)
  const [skillTreeData, setSkillTreeData] = useState<SkillTreeData | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string>('')
  const [skillCatalogCode, setSkillCatalogCode] = useState<string>('')
  const [skillName, setSkillName] = useState<string>('')
  const [canvasWidth, setCanvasWidth] = useState<number>(375)
  const [canvasHeight, setCanvasHeight] = useState<number>(600)
  const [scale, setScale] = useState<number>(1)
  const [offsetX, setOffsetX] = useState<number>(0)
  const [offsetY, setOffsetY] = useState<number>(0)
  const [nodePositions, setNodePositions] = useState<NodePosition[]>([])

  useLoad(() => {
    // 从路由参数获取技能目录编码和名称
    const { skillCode: code, skillName: name } = router.params
    console.log('思维导图页面接收到的参数:', { skillCode: code, skillName: name })

    if (code) {
      setSkillCatalogCode(code)
      setSkillName(decodeURIComponent(name || '学习目标'))
      loadSkillTree(code)
    } else {
      console.error('缺少skillCode参数:', router.params)
      setError('缺少必要参数')
      setLoading(false)
    }

    // 获取屏幕尺寸
    getSystemInfo({
      success: (res) => {
        console.log('屏幕尺寸:', res)
        // 减去导航栏和控制面板的高度，以及margin
        const width = res.windowWidth - 40 // 减去左右margin
        const height = res.windowHeight - 200 // 减去导航栏、控制面板和上下margin
        setCanvasWidth(width)
        setCanvasHeight(height)
        console.log('设置Canvas尺寸:', { width, height })
      }
    })
  })

  // 加载技能树数据
  const loadSkillTree = async (code: string) => {
    try {
      setLoading(true)
      setError('')
      
      const token = getStorageSync('token')
      if (!token) {
        setError('请先登录')
        return
      }

      apiLog('获取知识图谱数据...', { skillCode: code })

      // 直接使用技能编码作为根节点编码，不再添加_expert后缀
      const rootCode = code
      console.log('使用的根节点编码:', rootCode)

      // 调用消费者端知识图谱API
      const knowledgeMapUrl = `${API_CONFIG.baseUrl}/api/consumer/knowledge-map/getSkillTree?rootCode=${rootCode}`
      console.log('消费者端知识图谱API URL:', knowledgeMapUrl)

      const response = await request({
        url: knowledgeMapUrl,
        method: 'POST',
        header: getApiHeaders(token)
      })

      console.log('知识图谱API响应:', response)
      console.log('响应状态码:', response.statusCode)
      console.log('响应数据:', response.data)

      if (response.statusCode === 200 && response.data.success) {
        const knowledgeMapData = response.data.data
        console.log('知识图谱数据:', knowledgeMapData)

        // 知识图谱API返回的是树形结构，直接使用
        if (knowledgeMapData) {
          const convertedData = {
            rootCode: knowledgeMapData.rootCode,
            rootName: knowledgeMapData.rootName,
            children: convertKnowledgeMapToTreeNodes(knowledgeMapData.children || [])
          }
          console.log('转换后的数据:', convertedData)
          setSkillTreeData(convertedData)

          // 计算节点位置
          setTimeout(() => {
            const positions = calculateNodePositions(convertedData)
            setNodePositions(positions)

            // 确保初始偏移值在合理范围内
            const clampedOffset = clampOffset(offsetX, offsetY, scale)
            if (clampedOffset.x !== offsetX || clampedOffset.y !== offsetY) {
              setOffsetX(clampedOffset.x)
              setOffsetY(clampedOffset.y)
            }

            // 等待状态更新后再绘制
            setTimeout(() => {
              drawMindmap(convertedData, positions)
            }, 50)
          }, 100)
        } else {
          console.error('知识图谱数据为空或无效')
          setError('知识图谱数据为空')
        }
      } else {
        console.error('API调用失败:', response.data)
        setError(response.data.message || '获取知识图谱失败')
      }
    } catch (e) {
      console.error('获取技能树失败:', e)
      setError('网络错误，请重试')
    } finally {
      setLoading(false)
    }
  }

  // 转换知识图谱数据为树形节点
  const convertKnowledgeMapToTreeNodes = (knowledgeMapNodes: any[]): TreeNode[] => {
    return knowledgeMapNodes.map((item: any) => ({
      nodeCode: item.nodeCode,
      nodeName: item.nodeName,
      nodeType: item.nodeType,
      levelDepth: item.levelDepth,
      levelType: item.levelType || `L${item.levelDepth}`,
      difficultyLevel: item.difficultyLevel || 'BEGINNER',
      estimatedHours: item.estimatedHours || 0,
      description: item.description,
      sortOrder: item.sortOrder || 0,
      children: item.children ? convertKnowledgeMapToTreeNodes(item.children) : []
    }))
  }

  // 获取节点颜色
  const getNodeColor = (node: TreeNode): string => {
    // 优先使用难度等级颜色
    if (node.difficultyLevel && DIFFICULTY_COLORS[node.difficultyLevel as keyof typeof DIFFICULTY_COLORS]) {
      return DIFFICULTY_COLORS[node.difficultyLevel as keyof typeof DIFFICULTY_COLORS]
    }

    // 其次使用节点类型颜色
    if (node.nodeType && COLOR_THEMES[node.nodeType as keyof typeof COLOR_THEMES]) {
      return COLOR_THEMES[node.nodeType as keyof typeof COLOR_THEMES]
    }

    // 默认颜色
    return COLOR_THEMES.DEFAULT
  }

  // 转换技能目录数据为树形节点（保留作为备用）
  const convertSkillCatalogToTreeNodes = (catalogList: any[], parentCode: string): TreeNode[] => {
    const children = catalogList.filter((item: any) => item.parentCode === parentCode)

    return children.map((item: any) => ({
      nodeCode: item.skillCode,
      nodeName: item.skillName,
      nodeType: item.level === 1 ? 'ROOT' : item.level === 2 ? 'LEVEL' : 'LEAF',
      levelDepth: item.level,
      levelType: item.levelName || `L${item.level}`,
      difficultyLevel: getDifficultyLevelText(item.difficultyLevel),
      estimatedHours: item.estimatedHours || 0,
      description: item.description,
      sortOrder: item.sortOrder || 0,
      children: convertSkillCatalogToTreeNodes(catalogList, item.skillCode)
    }))
  }

  // 转换难度等级
  const getDifficultyLevelText = (level: string): string => {
    const levelMap: Record<string, string> = {
      'beginner': '初级',
      'intermediate': '中级',
      'advanced': '高级'
    }
    return levelMap[level] || '初级'
  }

  // 计算节点位置
  const calculateNodePositions = (treeData: SkillTreeData): NodePosition[] => {
    const positions: NodePosition[] = []
    const centerX = canvasWidth / 2
    const centerY = canvasHeight / 2
    const levelHeight = 120
    const nodeWidth = 120
    const nodeHeight = 60

    // 递归计算节点位置
    const calculatePosition = (nodes: TreeNode[], level: number, parentX: number, parentY: number, startAngle: number, angleRange: number) => {
      const nodeCount = nodes.length
      if (nodeCount === 0) return

      nodes.forEach((node, index) => {
        let x: number, y: number

        if (level === 0) {
          // 根节点居中
          x = centerX
          y = centerY
        } else {
          // 子节点围绕父节点分布
          const angle = startAngle + (angleRange * index) / Math.max(nodeCount - 1, 1)
          const radius = level * levelHeight
          x = parentX + Math.cos(angle) * radius
          y = parentY + Math.sin(angle) * radius
        }

        positions.push({
          x: x - nodeWidth / 2,
          y: y - nodeHeight / 2,
          width: nodeWidth,
          height: nodeHeight,
          node,
          color: getNodeColor(node),
          radius: nodeWidth / 2
        })

        // 递归处理子节点
        if (node.children && node.children.length > 0) {
          const childAngleRange = Math.PI / 3 // 60度范围
          const childStartAngle = -childAngleRange / 2
          calculatePosition(node.children, level + 1, x, y, childStartAngle, childAngleRange)
        }
      })
    }

    // 添加根节点
    const rootNode = {
      nodeCode: treeData.rootCode,
      nodeName: treeData.rootName,
      nodeType: 'ROOT',
      levelDepth: 0,
      difficultyLevel: 'EXPERT',
      estimatedHours: 0,
      sortOrder: 0,
      children: treeData.children
    }

    positions.push({
      x: centerX - nodeWidth / 2,
      y: centerY - nodeHeight / 2,
      width: nodeWidth,
      height: nodeHeight,
      node: rootNode,
      color: getNodeColor(rootNode),
      radius: nodeWidth / 2
    })

    // 从根节点的子节点开始计算
    if (treeData.children && treeData.children.length > 0) {
      calculatePosition(treeData.children, 1, centerX, centerY, 0, Math.PI * 2)
    }

    return positions
  }

  // 绘制思维导图
  const drawMindmap = (treeData: SkillTreeData, positions?: NodePosition[]) => {
    const positionsToUse = positions || nodePositions

    const canvas = createCanvasContext('mindmap-canvas')

    // 清空画布
    canvas.clearRect(0, 0, canvasWidth, canvasHeight)

    // 手动应用变换到坐标（微信小程序Canvas变换支持有限）
    const transformedPositions = positionsToUse.map(pos => ({
      ...pos,
      x: (pos.x * scale) + offsetX,
      y: (pos.y * scale) + offsetY,
      width: pos.width * scale,
      height: pos.height * scale,
      radius: pos.radius * scale
    }))

    // 绘制连接线
    drawConnections(canvas, treeData, transformedPositions)

    // 绘制节点
    drawNodes(canvas, transformedPositions)

    canvas.draw()
  }

  // 绘制连接线
  const drawConnections = (canvas: any, treeData: SkillTreeData, positions: NodePosition[]) => {
    const drawNodeConnections = (nodes: TreeNode[], parentPos?: NodePosition) => {
      nodes.forEach(node => {
        const nodePos = positions.find(pos => pos.node.nodeCode === node.nodeCode)
        if (!nodePos) return

        // 如果有父节点，绘制连接线
        if (parentPos) {
          canvas.setStrokeStyle(nodePos.color)
          const lineWidth = Math.max(1, 3 * (scale || 1))
          canvas.setLineWidth(lineWidth)
          if (typeof canvas.setLineCap === 'function') {
            canvas.setLineCap('round')
          }
          canvas.beginPath()
          canvas.moveTo(
            parentPos.x + parentPos.width / 2,
            parentPos.y + parentPos.height / 2
          )
          canvas.lineTo(
            nodePos.x + nodePos.width / 2,
            nodePos.y + nodePos.height / 2
          )
          canvas.stroke()
        }

        // 递归绘制子节点连接线
        if (node.children && node.children.length > 0) {
          drawNodeConnections(node.children, nodePos)
        }
      })
    }

    // 找到根节点位置
    const rootPos = positions.find(pos => pos.node.nodeCode === treeData.rootCode)

    // 从根节点开始绘制连接线到子节点
    if (treeData.children && rootPos) {
      drawNodeConnections(treeData.children, rootPos)
    }
  }

  // 绘制节点
  const drawNodes = (canvas: any, positions: NodePosition[]) => {
    positions.forEach((pos) => {
      const { x, y, width, height, node, color, radius } = pos
      const centerX = x + width / 2
      const centerY = y + height / 2

      // 绘制圆形节点背景（微信小程序不支持阴影和渐变，使用简单填充）
      canvas.setFillStyle(color)
      canvas.beginPath()
      canvas.arc(centerX, centerY, radius, 0, 2 * Math.PI)
      canvas.fill()

      // 绘制节点边框
      canvas.setStrokeStyle('#ffffff')
      const borderWidth = Math.max(1, 3 * (scale || 1))
      canvas.setLineWidth(borderWidth)
      canvas.beginPath()
      canvas.arc(centerX, centerY, radius, 0, 2 * Math.PI)
      canvas.stroke()

      // 绘制节点文字（使用微信小程序兼容的API）
      canvas.setFillStyle('#ffffff')
      const baseFontSize = node.nodeType === 'ROOT' ? 16 : 12

      // 根据节点名称长度调整字体大小
      const textLength = node.nodeName.length
      let fontSize = baseFontSize
      if (textLength > 8) {
        fontSize = 10
      } else if (textLength > 6) {
        fontSize = 11
      }

      // 应用缩放到字体大小
      const scaledFontSize = Math.max(8, fontSize * (scale || 1))
      canvas.setFontSize(scaledFontSize)

      // 绘制节点名称（简化版本，先确保能显示）
      try {
        // 尝试设置文字对齐（如果支持的话）
        if (typeof canvas.setTextAlign === 'function') {
          canvas.setTextAlign('center')
        }
        if (typeof canvas.setTextBaseline === 'function') {
          canvas.setTextBaseline('middle')
        }

        canvas.fillText(node.nodeName, centerX, centerY)

        // 绘制时长信息
        const timeInfoFontSize = Math.max(6, 9 * (scale || 1))
        canvas.setFontSize(timeInfoFontSize)
        canvas.fillText(`${node.estimatedHours || 0}h`, centerX, centerY + (20 * (scale || 1)))
      } catch (error) {
        console.error('绘制文字时出错:', error)
      }
    })
  }



  // 调整颜色亮度
  const adjustColorBrightness = (color: string, amount: number): string => {
    // 简单的颜色亮度调整 - 返回稍微暗一点的颜色用于渐变
    const hex = color.replace('#', '')
    const r = Math.max(0, Math.min(255, parseInt(hex.substring(0, 2), 16) + amount))
    const g = Math.max(0, Math.min(255, parseInt(hex.substring(2, 4), 16) + amount))
    const b = Math.max(0, Math.min(255, parseInt(hex.substring(4, 6), 16) + amount))
    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
  }

  // 触摸状态
  const [lastTouchDistance, setLastTouchDistance] = useState<number>(0)
  const [lastTouchCenter, setLastTouchCenter] = useState<{x: number, y: number}>({x: 0, y: 0})
  const [isDragging, setIsDragging] = useState<boolean>(false)
  const [lastTouchPos, setLastTouchPos] = useState<{x: number, y: number}>({x: 0, y: 0})

  // 边界检查函数
  const clampOffset = (offsetX: number, offsetY: number, scale: number) => {
    // 根据缩放级别动态调整边界
    const maxOffset = Math.max(canvasWidth, canvasHeight) * (0.5 + scale * 0.3)
    const minOffset = -maxOffset

    return {
      x: Math.max(minOffset, Math.min(maxOffset, offsetX)),
      y: Math.max(minOffset, Math.min(maxOffset, offsetY))
    }
  }

  // 计算两点间距离
  const getDistance = (touch1: any, touch2: any) => {
    const dx = touch1.x - touch2.x
    const dy = touch1.y - touch2.y
    return Math.sqrt(dx * dx + dy * dy)
  }

  // 计算两点中心
  const getCenter = (touch1: any, touch2: any) => {
    return {
      x: (touch1.x + touch2.x) / 2,
      y: (touch1.y + touch2.y) / 2
    }
  }

  // 处理触摸开始
  const handleTouchStart = (e: any) => {
    const touches = e.touches

    if (touches.length === 1) {
      // 单指拖拽
      setIsDragging(true)
      setLastTouchPos({
        x: touches[0].x,
        y: touches[0].y
      })
    } else if (touches.length === 2) {
      // 双指缩放
      setIsDragging(false)
      const distance = getDistance(touches[0], touches[1])
      const center = getCenter(touches[0], touches[1])
      setLastTouchDistance(distance)
      setLastTouchCenter(center)
    }
  }

  // 处理触摸移动
  const handleTouchMove = (e: any) => {
    const touches = e.touches

    if (touches.length === 1 && isDragging) {
      // 单指拖拽
      const deltaX = touches[0].x - lastTouchPos.x
      const deltaY = touches[0].y - lastTouchPos.y

      // 计算新的偏移值并应用边界限制
      const newOffsetX = offsetX + deltaX
      const newOffsetY = offsetY + deltaY
      const clampedOffset = clampOffset(newOffsetX, newOffsetY, scale)

      setOffsetX(clampedOffset.x)
      setOffsetY(clampedOffset.y)

      setLastTouchPos({
        x: touches[0].x,
        y: touches[0].y
      })

      // 重新绘制
      if (skillTreeData) {
        setTimeout(() => drawMindmap(skillTreeData), 16)
      }
    } else if (touches.length === 2) {
      // 双指缩放
      const distance = getDistance(touches[0], touches[1])
      const center = getCenter(touches[0], touches[1])

      if (lastTouchDistance > 0) {
        const scaleChange = distance / lastTouchDistance
        const newScale = Math.max(0.5, Math.min(3, scale * scaleChange))

        setScale(newScale)

        // 缩放后检查并调整偏移边界
        const clampedOffset = clampOffset(offsetX, offsetY, newScale)
        setOffsetX(clampedOffset.x)
        setOffsetY(clampedOffset.y)

        // 重新绘制
        if (skillTreeData) {
          setTimeout(() => drawMindmap(skillTreeData), 16)
        }
      }

      setLastTouchDistance(distance)
      setLastTouchCenter(center)
    }
  }

  // 处理触摸结束
  const handleTouchEnd = (e: any) => {
    setIsDragging(false)
    setLastTouchDistance(0)
  }

  // 重置视图
  const resetView = () => {
    setScale(1)
    setOffsetX(0)
    setOffsetY(0)
    if (skillTreeData) {
      setTimeout(() => drawMindmap(skillTreeData), 100)
    }
  }

  // 返回上一页
  const handleBack = () => {
    navigateBack()
  }

  return (
    <View className='mindmap-container'>
      {/* 头部导航 */}
      <View className='mindmap-header'>
        <View className='header-left' onClick={handleBack}>
          <Text className='back-icon'>←</Text>
          <Text className='back-text'>返回</Text>
        </View>
        <View className='header-center'>
          <Text className='header-title'>{skillName}</Text>
          <Text className='header-subtitle'>技能树思维导图</Text>
        </View>
        <View className='header-right'>
          <Text className='zoom-text'>{Math.round(scale * 100)}%</Text>
        </View>
      </View>

      {/* 内容区域 */}
      <View className='mindmap-content'>
        {loading ? (
          <View className='loading-container'>
            <Text className='loading-text'>加载中...</Text>
          </View>
        ) : error ? (
          <View className='error-container'>
            <Text className='error-text'>{error}</Text>
            <View className='debug-info' style={{ marginTop: '20px', padding: '10px', backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: '10px' }}>
              <Text style={{ color: '#fff', fontSize: '12px' }}>调试信息:</Text>
              <Text style={{ color: '#fff', fontSize: '12px' }}>原始skillCode: {skillCatalogCode}</Text>
              <Text style={{ color: '#fff', fontSize: '12px' }}>映射rootCode: {skillCatalogCode === 'java' ? 'java_expert' : `${skillCatalogCode}_expert`}</Text>
              <Text style={{ color: '#fff', fontSize: '12px' }}>skillName: {skillName}</Text>
              <Text style={{ color: '#fff', fontSize: '12px' }}>API: 消费者端知识图谱API</Text>
            </View>
            <View className='retry-btn' onClick={() => loadSkillTree(skillCatalogCode)}>
              重试
            </View>
          </View>
        ) : (
          <View className='canvas-container'>
            <Canvas
              ref={canvasRef}
              canvasId='mindmap-canvas'
              className='mindmap-canvas'
              style={{ width: `${canvasWidth}px`, height: `${canvasHeight}px` }}
              onTouchStart={handleTouchStart}
              onTouchMove={handleTouchMove}
              onTouchEnd={handleTouchEnd}
            />
          </View>
        )}
      </View>

      {/* 控制面板 */}
      {!loading && !error && (
        <View className='control-panel'>
          <View className='zoom-controls'>
            <Button
              className='zoom-btn'
              onClick={() => {
                const newScale = Math.max(0.5, scale - 0.1)
                setScale(newScale)
                // 缩放后检查并调整偏移边界
                const clampedOffset = clampOffset(offsetX, offsetY, newScale)
                setOffsetX(clampedOffset.x)
                setOffsetY(clampedOffset.y)
                if (skillTreeData) setTimeout(() => drawMindmap(skillTreeData), 16)
              }}
            >
              -
            </Button>
            <Text className='zoom-text'>{Math.round(scale * 100)}%</Text>
            <Button
              className='zoom-btn'
              onClick={() => {
                const newScale = Math.min(3, scale + 0.1)
                setScale(newScale)
                // 缩放后检查并调整偏移边界
                const clampedOffset = clampOffset(offsetX, offsetY, newScale)
                setOffsetX(clampedOffset.x)
                setOffsetY(clampedOffset.y)
                if (skillTreeData) setTimeout(() => drawMindmap(skillTreeData), 16)
              }}
            >
              +
            </Button>
          </View>
          <Button className='reset-btn' onClick={resetView}>
            重置视图
          </Button>
        </View>
      )}
    </View>
  )
}

export default Mindmap

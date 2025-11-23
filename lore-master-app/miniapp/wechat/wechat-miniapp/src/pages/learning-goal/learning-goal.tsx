import { useState, useEffect } from 'react'
import { useLoad, navigateBack, showToast, request, getStorageSync } from '@tarojs/taro'
import { View, Text, Button, ScrollView } from '@tarojs/components'
import { API_CONFIG, getApiHeaders, apiLog } from '../../config/api'
import './learning-goal.css'

interface SkillCatalog {
  id: number
  skillCode: string
  skillName: string
  name: string // 保留兼容性
  skillPath: string
  level: number
  parentCode?: string
  parentId?: number // 保留兼容性
  children?: SkillCatalog[]
  hasChildren?: boolean
  icon?: string
  description?: string
  difficultyLevel?: string
  estimatedHours?: number
}

interface LearningGoalRequest {
  skillCode: string
  skillName: string
  skillPath: string
  targetLevel?: string
  description?: string
}

const LearningGoal = () => {
  const [skillTree, setSkillTree] = useState<SkillCatalog[]>([])
  const [selectedPath, setSelectedPath] = useState<SkillCatalog[]>([])
  const [currentLevel, setCurrentLevel] = useState<SkillCatalog[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  useLoad(() => {
    console.log('学习目标页面加载')
    loadSkillCatalogTree()
  })

  // 获取难度等级显示文本
  const getDifficultyText = (level: string) => {
    switch (level) {
      case 'beginner':
        return '初级'
      case 'intermediate':
        return '中级'
      case 'advanced':
        return '高级'
      default:
        return level
    }
  }

  // 加载技能目录树
  const loadSkillCatalogTree = async () => {
    try {
      setLoading(true)
      apiLog('调用技能目录树接口...')
      
      const token = getStorageSync('token')
      if (!token) {
        showToast({
          title: '请先登录',
          icon: 'none'
        })
        navigateBack()
        return
      }

      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/consumer/skill-catalog/tree`,
        method: 'POST',
        data: {}, // 查询所有技能目录
        header: getApiHeaders(token)
      })

      console.log('技能目录树响应:', response)

      if (response.statusCode === 200 && response.data.success) {
        const tree = response.data.data || []
        setSkillTree(tree)
        // 设置第一级分类为当前级别
        setCurrentLevel(tree)
      } else {
        throw new Error(response.data.message || '获取技能目录失败')
      }
    } catch (e) {
      console.error('加载技能目录树失败:', e)
      showToast({
        title: e.message || '加载失败',
        icon: 'none'
      })
    } finally {
      setLoading(false)
    }
  }

  // 选择技能分类
  const selectSkill = (skill: SkillCatalog) => {
    console.log('选择技能:', skill)
    
    // 添加到选择路径
    const newPath = [...selectedPath, skill]
    setSelectedPath(newPath)

    // 如果有子分类，显示子分类
    if (skill.children && skill.children.length > 0) {
      setCurrentLevel(skill.children)
    } else if (skill.hasChildren) {
      // 如果标记有子分类但没有加载，需要加载子分类
      loadChildrenSkills(skill.id)
    } else {
      // 没有子分类，可以保存目标
      console.log('已选择到最终分类，可以保存目标')
    }
  }

  // 加载子分类
  const loadChildrenSkills = async (parentId: number) => {
    try {
      setLoading(true)
      const token = getStorageSync('token')
      
      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/consumer/skill-catalog/tree`,
        method: 'POST',
        data: {
          parentId: parentId
        },
        header: getApiHeaders(token)
      })

      if (response.statusCode === 200 && response.data.success) {
        const children = response.data.data || []
        setCurrentLevel(children)
      } else {
        throw new Error(response.data.message || '获取子分类失败')
      }
    } catch (e) {
      console.error('加载子分类失败:', e)
      showToast({
        title: e.message || '加载子分类失败',
        icon: 'none'
      })
    } finally {
      setLoading(false)
    }
  }

  // 返回上一级
  const goBack = () => {
    if (selectedPath.length === 0) {
      // 已经在根级别，返回上一页
      navigateBack()
      return
    }

    // 移除最后一个选择
    const newPath = selectedPath.slice(0, -1)
    setSelectedPath(newPath)

    if (newPath.length === 0) {
      // 返回到根级别
      setCurrentLevel(skillTree)
    } else {
      // 返回到上一级的子分类
      const parentSkill = newPath[newPath.length - 1]
      if (parentSkill.children) {
        setCurrentLevel(parentSkill.children)
      } else {
        loadChildrenSkills(parentSkill.id)
      }
    }
  }

  // 保存学习目标
  const saveLearningGoal = async () => {
    if (selectedPath.length === 0) {
      showToast({
        title: '请选择学习目标',
        icon: 'none'
      })
      return
    }

    const finalSkill = selectedPath[selectedPath.length - 1]
    
    // 检查是否还有子分类
    if (finalSkill.hasChildren || (finalSkill.children && finalSkill.children.length > 0)) {
      showToast({
        title: '请选择到具体的学习目标',
        icon: 'none'
      })
      return
    }

    try {
      setSaving(true)
      const token = getStorageSync('token')
      console.log('获取到的token:', token)
      
      // 构建技能路径
      const skillPath = finalSkill.skillPath || selectedPath.map(skill =>
        (skill.skillCode || skill.name || `skill_${skill.id}`).toLowerCase().replace(/\s+/g, '_')
      ).join('/')

      // 构建技能编码
      const skillCode = finalSkill.skillCode || `skill_${finalSkill.id}`

      // 构建技能名称
      const skillName = finalSkill.skillName || finalSkill.name || '未知技能'

      const requestData: LearningGoalRequest = {
        skillCode: skillCode,
        skillName: skillName,
        skillPath: skillPath,
        description: selectedPath.map(skill => skill.skillName || skill.name).join(' > ')
      }

      console.log('构建的请求数据:', requestData)

      console.log('保存学习目标请求:', requestData)

      const headers = getApiHeaders(token)
      console.log('请求头:', headers)

      const response = await request({
        url: `${API_CONFIG.baseUrl}/api/user/learning-goal/saveLearningGoal`,
        method: 'POST',
        data: requestData,
        header: headers
      })

      console.log('保存学习目标响应:', response)

      if (response.statusCode === 200 && response.data.success) {
        showToast({
          title: '学习目标设置成功',
          icon: 'success'
        })
        
        // 延迟返回上一页
        setTimeout(() => {
          navigateBack()
        }, 1500)
      } else {
        throw new Error(response.data.message || '保存学习目标失败')
      }
    } catch (e) {
      console.error('保存学习目标失败:', e)
      showToast({
        title: e.message || '保存失败',
        icon: 'none'
      })
    } finally {
      setSaving(false)
    }
  }

  // 判断是否可以保存（选择到了最终分类）
  const canSave = selectedPath.length > 0 && (() => {
    const finalSkill = selectedPath[selectedPath.length - 1]
    return !finalSkill.hasChildren && (!finalSkill.children || finalSkill.children.length === 0)
  })()

  return (
    <View className='learning-goal-container'>
      {/* 头部导航 */}
      <View className='header'>
        <View className='back-btn' onClick={goBack}>
          <Text className='back-icon'>←</Text>
        </View>
        <Text className='header-title'>设置学习目标</Text>
        <View className='header-placeholder'></View>
      </View>

      {/* 当前分类信息 */}
      <View className='category-info'>
        <View className='current-category'>
          <Text className='category-label'>当前分类：</Text>
          <Text className='category-name'>
            {currentLevel.length > 0 ?
              (selectedPath.length > 0 ?
                `${selectedPath[selectedPath.length - 1].skillName || selectedPath[selectedPath.length - 1].name} 的子分类` :
                '一级分类'
              ) :
              '加载中...'
            }
          </Text>
        </View>

        {/* 父类信息 */}
        {selectedPath.length > 0 && (
          <View className='parent-category'>
            <Text className='parent-label'>父级分类：</Text>
            <Text className='parent-name'>
              {selectedPath.length > 1 ?
                selectedPath[selectedPath.length - 2].skillName || selectedPath[selectedPath.length - 2].name :
                '根分类'
              }
            </Text>
          </View>
        )}

        {/* 选择路径显示 */}
        {selectedPath.length > 0 && (
          <View className='breadcrumb'>
            <Text className='breadcrumb-label'>完整路径：</Text>
            <Text className='breadcrumb-text'>
              {selectedPath.map(skill => skill.skillName || skill.name).join(' → ')}
            </Text>
          </View>
        )}

        {/* 技能路径 */}
        {selectedPath.length > 0 && selectedPath[selectedPath.length - 1].skillPath && (
          <View className='skill-path'>
            <Text className='path-label'>技能路径：</Text>
            <Text className='path-text'>
              {selectedPath[selectedPath.length - 1].skillPath}
            </Text>
          </View>
        )}
      </View>

      {/* 技能分类列表 */}
      <ScrollView className='skill-list' scrollY>
        {loading ? (
          <View className='loading-container'>
            <Text className='loading-text'>加载中...</Text>
          </View>
        ) : (
          currentLevel.map((skill) => (
            <View
              key={skill.id}
              className='skill-item'
              onClick={() => selectSkill(skill)}
            >
              <View className='skill-icon'>
                {skill.hasChildren || (skill.children && skill.children.length > 0) ? '📁' : '🎯'}
              </View>
              <View className='skill-info'>
                <View className='skill-name-container'>
                  <Text className='skill-name'>{skill.name || skill.skillName}</Text>
                  {skill.skillName && skill.name !== skill.skillName && (
                    <Text className='skill-name-detail'>({skill.skillName})</Text>
                  )}
                </View>
                <View className='skill-meta'>
                  <Text className='skill-level'>第 {skill.level} 级</Text>
                  {skill.difficultyLevel && (
                    <Text className='skill-difficulty'>{getDifficultyText(skill.difficultyLevel)}</Text>
                  )}
                  {skill.estimatedHours && (
                    <Text className='skill-hours'>{skill.estimatedHours}h</Text>
                  )}
                  {skill.hasChildren || (skill.children && skill.children.length > 0) ? (
                    <Text className='skill-desc'>包含子分类</Text>
                  ) : (
                    <Text className='skill-desc'>可设为目标</Text>
                  )}
                </View>
                {skill.description && (
                  <Text className='skill-description'>{skill.description}</Text>
                )}
              </View>
              <View className='skill-arrow'>
                {skill.hasChildren || (skill.children && skill.children.length > 0) ? '→' : '✓'}
              </View>
            </View>
          ))
        )}
      </ScrollView>

      {/* 底部保存按钮 */}
      <View className='footer'>
        {canSave && (
          <Button
            type='primary'
            className='save-btn'
            onClick={saveLearningGoal}
            loading={saving}
            disabled={saving}
          >
            {saving ? '保存中...' : '设置为我的学习目标'}
          </Button>
        )}
      </View>
    </View>
  )
}

export default LearningGoal

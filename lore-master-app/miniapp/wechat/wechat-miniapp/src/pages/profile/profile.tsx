import { useState, useEffect } from 'react'
import { useLoad, login, getUserProfile, getStorageSync, setStorageSync, removeStorageSync, request, showToast, getSystemInfo, getNetworkType, chooseImage, hideLoading, showActionSheet, uploadFile } from '@tarojs/taro'
import { View, Text, Button, Image } from '@tarojs/components'
import { API_ENDPOINTS, buildApiUrl, getApiHeaders, apiLog } from '../../config/api'
import './profile.css'

const Profile = () => {
  const [userInfo, setUserInfo] = useState<any>(null)
  const [isLogin, setIsLogin] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(false) // 添加加载状态，防止重复点击
  const [lastLoginTime, setLastLoginTime] = useState<number>(0) // 记录上次登录请求时间，防止短时间内重复请求
  const [fontSize, setFontSize] = useState<number>(16) // 字体大小状态，默认16px
  const MIN_LOGIN_INTERVAL = 2000 // 最小登录间隔(毫秒)
  const DEFAULT_AVATAR = 'https://via.placeholder.com/100?text=默认头像' // 默认头像地址

  useLoad(() => {
    console.log('页面加载，开始检查登录状态')
    checkLoginStatus()
  })

  // 添加useEffect来确保组件挂载时也检查登录状态
  useEffect(() => {
    console.log('组件挂载，检查本地存储的登录状态')
    checkLocalLoginStatus()
  }, [])

  // 检查本地存储的登录状态（不调用接口）
  const checkLocalLoginStatus = () => {
    try {
      console.log('检查本地存储的登录状态...')
      const token = getStorageSync('token')
      const userInfo = getStorageSync('userInfo')

      console.log('本地存储检查结果 - token:', token ? '存在' : '不存在')
      console.log('本地存储检查结果 - userInfo:', userInfo ? '存在' : '不存在')

      if (token && userInfo) {
        // 本地有token和用户信息，直接设置登录状态
        setUserInfo(userInfo)
        setIsLogin(true)
        console.log('从本地存储恢复登录状态成功')
      } else {
        // 本地没有完整的登录信息
        setUserInfo(null)
        setIsLogin(false)
        console.log('本地存储中没有完整的登录信息')
      }
    } catch (e) {
      console.error('检查本地登录状态异常:', e)
      setUserInfo(null)
      setIsLogin(false)
    }
  }

  // 调用用户信息接口并同步到cookie
  const fetchUserInfo = async () => {
    try {
      apiLog('调用/api/user/info接口获取用户信息...')
      const token = getStorageSync('token')
      if (!token) {
        apiLog('token不存在，无法获取用户信息')
        return null
      }

      const response = await request({
        url: API_ENDPOINTS.USER_INFO,
        method: 'GET', // 接口已改为GET请求
        header: getApiHeaders(token)
      })

      console.log('用户信息接口响应:', response)

      if (response.statusCode === 200 && response.data.success) {
        const userInfo = response.data.userInfo
        console.log('获取用户信息成功，userInfo内容:', userInfo) // 添加日志确认userInfo结构
        console.log('获取用户信息成功，准备同步到storage...')

        // 同步到storage (小程序中模拟cookie)
        if (userInfo) {
          try {
            const avatarUrl = userInfo.avatarUrl;
            const fullAvatarUrl = avatarUrl && avatarUrl.startsWith('/api/') ?
              buildApiUrl(avatarUrl) : avatarUrl;

            const userData = {
              nickName: userInfo.nickName || '微信用户',
              avatarUrl: fullAvatarUrl || DEFAULT_AVATAR,
              userId: userInfo.userId || '' // 添加userId到userData
            };

            setStorageSync('userInfo', userData) // 只存储userData
            console.log('用户信息已同步到storage')

            // 更新状态
            setUserInfo(userData)
          } catch (e) {
            console.error('同步用户信息到storage失败:', e)
          }
        }

        return userInfo
      } else {
        console.error('获取用户信息失败:', response.data.message || '未知错误')
        return null
      }
    } catch (e) {
      console.error('调用用户信息接口异常:', e)
      return null
    }
  }

  const checkLoginStatus = async () => {
    try {
      console.log('检查登录状态...')
      // 优先检查token是否存在
      const token = getStorageSync('token')
      console.log('存储获取结果 - token:', token ? '存在' : '不存在')

      if (token) {
        // token存在，调用接口获取最新用户信息
        const userInfo = await fetchUserInfo()
        console.log('获取用户信息:', userInfo)
        if (userInfo) {
          // 确保用户信息包含必要字段
          const avatarUrl = userInfo.avatarUrl;
          const fullAvatarUrl = avatarUrl && avatarUrl.startsWith('/api/') ?
            buildApiUrl(avatarUrl) : avatarUrl;

          const userData = {
            nickName: userInfo.nickName || '微信用户',
            avatarUrl: fullAvatarUrl || DEFAULT_AVATAR,
            userId: userInfo.userId || '' // 添加userId到userData
          };

          setUserInfo(userData)
          setIsLogin(true)
        } else {
          // 获取用户信息失败，可能是token失效
          console.log('获取用户信息失败，可能token失效，但先检查本地存储')
          // 先尝试从本地存储获取用户信息
          const localUserInfo = getStorageSync('userInfo')
          if (localUserInfo) {
            console.log('使用本地存储的用户信息')
            setUserInfo(localUserInfo)
            setIsLogin(true)
          } else {
            console.log('本地也没有用户信息，清除登录状态')
            setUserInfo(null)
            setIsLogin(false)
            removeStorageSync('token')
            removeStorageSync('refreshToken')
          }
        }
      } else {
        // token不存在，清除状态
        setUserInfo(null)
        setIsLogin(false)
      }
    } catch (e) {
      console.error('获取登录状态异常:', e)
      showToast({
        title: '获取登录状态失败',
        icon: 'none'
      })
    }
  }


  const handleLogin = async () => {
    // 检查是否在加载中或短时间内重复请求
    const currentTime = Date.now();
    if (isLoading || (currentTime - lastLoginTime < MIN_LOGIN_INTERVAL)) {
      console.log('登录请求被阻止: 正在加载中或请求间隔过短');
      return;
    }
    console.log('开始登录流程')
    setIsLoading(true); // 设置加载状态为true
    setLastLoginTime(currentTime); // 记录登录请求时间
    try {
      // 1. 获取用户信息
      console.log('1. 获取用户信息...')
      const userProfileRes = await getUserProfile({
        desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，不超过30个字符
        success: (res) => {

        },
        fail: (res) => {
          console.error('获取用户信息失败:', res)
        },
        complete: () => {
          console.log('获取用户信息接口调用完成')
        }
      })

      if (!userProfileRes.userInfo) {
        throw new Error('获取用户信息失败')
      }

      // 2. 获取微信登录code
      console.log('2. 获取微信登录code...')
      const loginRes = await login()
      console.log('登录结果:', loginRes)
      if (!loginRes.code) {
        throw new Error('获取登录code失败')
      }

      // 3. 获取系统信息和网络类型
      console.log('3. 获取系统信息和网络类型...')
      const systemInfo = await getSystemInfo()
      const networkType = await getNetworkType()

      // 4. 准备请求参数
      const requestData = {
        loginType: 'wechat', // 添加登录类型
        wechatUserInfo: userProfileRes.userInfo, // 重命名为wechatUserInfo
        code: loginRes.code,
        systemInfo: {
          model: systemInfo.model,
          system: systemInfo.system,
          version: systemInfo.version,
          platform: systemInfo.platform
        },
        networkType: networkType.networkType
      }

      console.log('发送给后端的登录参数:', requestData)

      // 5. 调用后端登录接口
      apiLog('4. 调用后端登录接口...')
      const response = await request({
        url: API_ENDPOINTS.USER_LOGIN,
        method: 'POST',
        data: requestData,
        header: getApiHeaders()
      })

      console.log('后端登录响应:', response)

      if (response.statusCode === 200 && response.data.success) {
        // 6. 登录成功，保存用户信息和token
        console.log('5. 登录成功，保存用户信息和token...')

        // 优先使用后端返回的用户信息，其次使用微信信息，最后使用默认值
        const backendAvatarUrl = response.data.data.userInfo?.avatarUrl;
        const fullBackendAvatarUrl = backendAvatarUrl && backendAvatarUrl.startsWith('/api/') ?
          buildApiUrl(backendAvatarUrl) : backendAvatarUrl;

        const userData = {
          nickName: response.data.data.userInfo?.nickName || userProfileRes.userInfo?.nickName || '微信用户',
          avatarUrl: fullBackendAvatarUrl || userProfileRes.userInfo?.avatarUrl || DEFAULT_AVATAR,
          userId: response.data.data.userInfo?.userId || '' // 添加userId到userData
        };

        setUserInfo(userData)

        try {
          console.log('尝试保存userInfo...')
          setStorageSync('userInfo', userData)
          console.log('userInfo保存成功')
        } catch (e) {
          console.error('保存userInfo失败:', e)
        }

        try {
          console.log('尝试保存token...')
          setStorageSync('token', response.data.data.token)
          console.log('token保存成功', response.data.data.token)
        } catch (e) {
          console.error('保存token失败:', e)
        }

        try {
          // 确保登录状态为true
          setIsLogin(true)
        } catch (e) {
          // 如果保存userId失败，强制重新检查登录状态
          checkLoginStatus()
        }

        try {
          console.log('尝试保存refreshToken...')
          setStorageSync('refreshToken', response.data.data.refreshToken)
          console.log('refreshToken保存成功')
        } catch (e) {
          console.error('保存refreshToken失败:', e)
        }

        setIsLogin(true)
        showToast({
          title: '登录成功',
          icon: 'success'
        })
      } else {
        console.error('后端返回登录失败:', response)
        throw new Error(response.data.message || '登录失败，请稍后重试')
      }
    } catch (e) {
      console.error('登录异常:', e)
      // 处理code已被使用的情况
      if (e.message && e.message.includes('code已被使用')) {
        showToast({
          title: '登录code已失效，正在重新获取code...',
          icon: 'none'
        });
        // 延迟1秒后重新获取code并登录
        setTimeout(async () => {
          try {
            console.log('重新获取code...');
            // 直接获取新的code
            const newLoginRes = await login();
            if (newLoginRes.code) {
              console.log('获取新code成功:', newLoginRes.code);
              // 调用处理登录逻辑，但传入新的code
              handleLoginWithNewCode(newLoginRes.code);
            } else {
              throw new Error('重新获取code失败');
            }
          } catch (retryError) {
            console.error('重新获取code异常:', retryError);
            showToast({
              title: '重新获取code失败，请手动重试',
              icon: 'none'
            });
          }
        }, 1000);
      } else {
        showToast({
          title: e.message || '登录失败',
          icon: 'none'
        });
      }
    } finally {
      setIsLoading(false); // 无论成功失败，都设置加载状态为false
    }
  }

  // 使用新code进行登录的处理函数
  const handleLoginWithNewCode = async (newCode: string) => {
    if (isLoading) return;
    console.log('使用新code开始登录流程:', newCode)
    setIsLoading(true);
    try {
      // 1. 获取用户信息
      console.log('1. 获取用户信息...')
      const userProfileRes = await getUserProfile({
        desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，不超过30个字符
        success: (res) => {
          console.log('用户信息获取结果:', res)
        },
        fail: (res) => {
          console.error('获取用户信息失败:', res)
        },
        complete: () => {
          console.log('获取用户信息接口调用完成')
        }
      })
      if (!userProfileRes.userInfo) {
        throw new Error('获取用户信息失败')
      }

      // 2. 这里使用传入的新code
      console.log('2. 使用新code:', newCode)

      // 3. 获取系统信息和网络类型
      console.log('3. 获取系统信息和网络类型...')
      const systemInfo = await getSystemInfo()
      const networkType = await getNetworkType()

      // 4. 准备请求参数
      const requestData = {
        loginType: 'wechat',
        wechatUserInfo: userProfileRes.userInfo,
        code: newCode,
        systemInfo: {
          model: systemInfo.model,
          system: systemInfo.system,
          version: systemInfo.version,
          platform: systemInfo.platform
        },
        networkType: networkType.networkType
      }

      console.log('发送给后端的登录参数(使用新code):', requestData)

      // 5. 调用后端登录接口
      apiLog('4. 调用后端登录接口...')
      const response = await request({
        url: API_ENDPOINTS.USER_LOGIN,
        method: 'POST',
        data: requestData,
        header: getApiHeaders()
      })

      console.log('后端登录响应:', response)

      if (response.statusCode === 200 && response.data.success) {
        // 6. 登录成功，保存用户信息和token
        console.log('5. 登录成功，保存用户信息和token...')

        // 优先使用后端返回的用户信息，其次使用微信信息，最后使用默认值
        const backendAvatarUrl = response.data.userInfo?.avatarUrl;
        const fullBackendAvatarUrl = backendAvatarUrl && backendAvatarUrl.startsWith('/api/') ?
          buildApiUrl(backendAvatarUrl) : backendAvatarUrl;

        const userData = {
          nickName: response.data.userInfo?.nickName || userProfileRes.userInfo?.nickName || '微信用户',
          avatarUrl: fullBackendAvatarUrl || userProfileRes.userInfo?.avatarUrl || DEFAULT_AVATAR
        };

        setUserInfo(userData)

        try {
          setStorageSync('userInfo', userData)
        } catch (e) {
          console.error('保存userInfo失败:', e)
        }

        try {
          setStorageSync('token', response.data.token)
        } catch (e) {
          console.error('保存token失败:', e)
        }

        try {
          console.log('尝试保存userId...')
          setStorageSync('userId', response.data.userInfo.userId)
          console.log('userId保存成功')
          // 确保登录状态为true
          setIsLogin(true)
        } catch (e) {
          console.error('保存userId失败:', e)
          // 如果保存userId失败，强制重新检查登录状态
          checkLoginStatus()
        }

        try {
          setStorageSync('refreshToken', response.data.refreshToken)
        } catch (e) {
          console.error('保存refreshToken失败:', e)
        }

        setIsLogin(true)
        showToast({
          title: '登录成功',
          icon: 'success'
        })
      } else {
        console.error('后端返回登录失败:', response)
        throw new Error(response.data.message || '登录失败，请稍后重试')
      }
    } catch (e) {
      console.error('使用新code登录异常:', e)
      showToast({
        title: e.message || '登录失败',
        icon: 'none'
      });
    } finally {
      setIsLoading(false);
    }
  }

  // 上传头像函数
  const uploadAvatar = async () => {
    // 只检查登录状态
    // if (!isLogin) {
    //   console.log('上传头像失败: 未登录', {isLogin})
    //   showToast({
    //     title: '请先登录',
    //     icon: 'none'
    //   })
    //   return
    // }
    console.log('开始上传头像')

    try {
      // 选择图片
      const chooseResult = await chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera']
      })

      const tempFilePath = chooseResult.tempFilePaths[0]
      console.log('选择的图片路径:', tempFilePath)

      showToast({
        title: '上传中...',
        icon: 'loading'
      })

      // 上传头像到后端
      return new Promise((resolve, reject) => {
        uploadFile({
          url: API_ENDPOINTS.USER_AVATAR_UPLOAD,
          filePath: tempFilePath,
          name: 'file',
          header: getApiHeaders(getStorageSync('token')),
          formData: {
            remark: '用户头像'
          },
          success: (res: any) => {
            console.log('头像上传结果:', res)
            const data = JSON.parse(res.data)
            if (res.statusCode === 200 && data.success) {
              // 上传成功，更新用户头像
              // 拼接完整的URL，因为后端返回的是相对路径
              const newAvatarUrl = data.accessUrl.startsWith('/api/') ?
                buildApiUrl(data.accessUrl) : data.accessUrl;
              const updatedUserInfo = {
                ...userInfo,
                avatarUrl: newAvatarUrl
              }

              setUserInfo(updatedUserInfo)

              // 更新本地存储
              try {
                setStorageSync('userInfo', updatedUserInfo)
                console.log('更新本地用户信息成功')
              } catch (e) {
                console.error('更新本地用户信息失败:', e)
              }

              hideLoading()
              showToast({
                title: '头像上传成功',
                icon: 'success'
              })
              resolve(newAvatarUrl)
            } else {
              const errorMsg = data.message || '头像上传失败'
              hideLoading()
              showToast({
                title: errorMsg,
                icon: 'none'
              })
              reject(new Error(errorMsg))
            }
          },
          fail: (err: any) => {
            console.error('头像上传失败:', err)
            hideLoading()
            showToast({
              title: '头像上传失败',
              icon: 'none'
            })
            reject(err)
          }
        })
      })
    } catch (e) {
      console.error('头像上传异常:', e)
      hideLoading()
      showToast({
        title: e.message || '头像上传失败',
        icon: 'none'
      })
      return Promise.reject(e)
    }
  }

  const handleLogout = () => {
    try {
      console.log('开始退出登录...')

      // 清除所有登录相关信息
      try {
        console.log('尝试删除userInfo...')
        removeStorageSync('userInfo')
        console.log('userInfo删除成功')
      } catch (e) {
        console.error('删除userInfo失败:', e)
      }

      try {
        console.log('尝试删除token...')
        removeStorageSync('token')
        console.log('token删除成功')
      } catch (e) {
        console.error('删除token失败:', e)
      }

      try {
        console.log('尝试删除userId...')
        removeStorageSync('userId')
        console.log('userId删除成功')
      } catch (e) {
        console.error('删除userId失败:', e)
      }

      try {
        console.log('尝试删除refreshToken...')
        removeStorageSync('refreshToken')
        console.log('refreshToken删除成功')
      } catch (e) {
        console.error('删除refreshToken失败:', e)
      }

      // 更新状态
      setUserInfo(null)
      setIsLogin(false)

      showToast({
        title: '退出成功',
        icon: 'success'
      })
    } catch (e) {
      console.error('退出登录异常:', e)
      showToast({
        title: '退出失败',
        icon: 'none'
      })
    }
  }

  // 处理设置按钮点击事件
  const handleSettings = async () => {
    try {
      const res = await showActionSheet({
        itemList: ['小号字体 (12px)', '默认字体 (16px)', '大号字体 (20px)', '超大字体 (24px)'],
        itemColor: '#000000'
      })

      // 根据用户选择设置字体大小
      const fontSizeMap = [12, 16, 20, 24]
      const selectedFontSize = fontSizeMap[res.tapIndex]
      setFontSize(selectedFontSize)

      showToast({
        title: `字体大小已设置为 ${selectedFontSize}px`,
        icon: 'success'
      })
    } catch (err) {
      // 用户取消选择
      console.log('用户取消了字体大小选择')
    }
  }

  return (
    <View className='profile-container' style={{ fontSize: `${fontSize}px` }}>
      {/* 用户信息区域 */}
      {isLogin ? (
        <View className='user-info-card'>
          <Image
            src={userInfo?.avatarUrl || DEFAULT_AVATAR}
            className='user-avatar'
            mode='aspectFill'
            onError={(e: any) => {
              console.error('头像加载失败:', e)
              setUserInfo(prev => prev ? { ...prev, avatarUrl: DEFAULT_AVATAR } : null)
            }}
            onClick={uploadAvatar}
          />
          <View className='user-info-content'>
            <Text className='user-nickname'>{userInfo?.nickName || '微信用户'}</Text>
            <Text className='user-id-text'>ID: {userInfo?.userId || '未知'}</Text>
          </View>
          <View className='edit-arrow' onClick={() => showToast({ title: '功能开发中' })}>
            <Text className='arrow-text'>{'>'}</Text>
          </View>
        </View>
      ) : (
        <View className='user-info-section'>
          <View className='avatar-container'>
            <View className='avatar-placeholder'>
              <Text className='placeholder-text'>未登录</Text>
            </View>
          </View>
          <View className='user-details'>
            <Text className='not-login'>请登录</Text>
          </View>
        </View>
      )}

      {/* 学习数据统计 */}
      {isLogin && (
        <View className='stats-container'>
          <View className='stat-item' onClick={() => showToast({title: '查看学习天数详情'})}>
            <Text className='stat-icon'>📅</Text>
            <Text className='stat-value'>0</Text>
            <Text className='stat-label'>学习天数</Text>
          </View>
          <View className='stat-item' onClick={() => showToast({title: '查看学习时长详情'})}>
            <Text className='stat-icon'>⏰</Text>
            <Text className='stat-value'>0</Text>
            <Text className='stat-label'>学习时长(h)</Text>
          </View>
          <View className='stat-item' onClick={() => showToast({title: '查看完成课程详情'})}>
            <Text className='stat-icon'>📚</Text>
            <Text className='stat-value'>0</Text>
            <Text className='stat-label'>完成课程</Text>
          </View>
          <View className='stat-item' onClick={() => showToast({title: '查看学习积分详情'})}>
            <Text className='stat-icon'>⭐</Text>
            <Text className='stat-value'>0</Text>
            <Text className='stat-label'>学习积分</Text>
          </View>
        </View>
      )}

      {/* 学习目标区域 */}
      <View className='profile-content'>
        {isLogin ? (
          <View className='function-list'>
            <View className='function-item' onClick={() => showToast({ title: '功能开发中' })}>
              <View className='function-icon'>🎯</View>
              <View className='function-info'>
                <View className='goal-title-row'>
                  <Text className='function-name'>我的学习目标</Text>
                  <Text className='goal-path'>外语 {'>'} 英语 {'>'} 雅思</Text>
                </View>
                <View className='goal-progress-row'>
                  <View className='progress-bar-compact'>
                    <View className='progress-fill-compact' style={{ width: '35%' }}></View>
                  </View>
                  <Text className='progress-text-compact'>35% 完成</Text>
                </View>
              </View>
              <View className='arrow-right'>→</View>
            </View>
            <View className='function-item' onClick={() => showToast({ title: '功能开发中' })}>
              <View className='function-icon'>📚</View>
              <View className='function-info'>
                <Text className='function-name'>我的课程</Text>
                <Text className='function-desc'>查看学习进度</Text>
              </View>
              <View className='arrow-right'>→</View>
            </View>
            <View className='function-item' onClick={() => showToast({ title: '功能开发中' })}>
              <View className='function-icon'>⭐</View>
              <View className='function-info'>
                <Text className='function-name'>我的收藏</Text>
                <Text className='function-desc'>收藏的课程</Text>
              </View>
              <View className='arrow-right'>→</View>
            </View>
            <View className='function-item' onClick={() => showToast({ title: '功能开发中' })}>
              <View className='function-icon'>💬</View>
              <View className='function-info'>
                <Text className='function-name'>意见反馈</Text>
                <Text className='function-desc'>帮助我们改进</Text>
              </View>
              <View className='arrow-right'>→</View>
            </View>
            <View className='function-item' onClick={handleSettings}>
              <View className='function-icon'>⚙️</View>
              <View className='function-info'>
                <Text className='function-name'>设置</Text>
                <Text className='function-desc'>调整字体大小</Text>
              </View>
              <View className='arrow-right'>→</View>
            </View>
          </View>
        ) : (
          <View className='login-prompt'>
            <Text className='prompt-text'>登录后可查看更多内容</Text>
            <Button type='primary' onClick={handleLogin} loading={isLoading} disabled={isLoading} className='login-btn'>微信登录</Button>
          </View>
        )}
      </View>

      {/* 底部区域 - 放置退出登录按钮 */}
      <View className='profile-footer'>
        {isLogin && (
          <Button
            type='warn'
            onClick={handleLogout}
            className='logout-btn'
          >
            退出登录
          </Button>
        )}
      </View>
    </View>
  )
}

export default Profile
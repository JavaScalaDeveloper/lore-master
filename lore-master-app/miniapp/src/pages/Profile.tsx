import React, { useState, useEffect } from 'react';
import './Profile.css';
import { consumerApi } from '../utils/request';
import { API_PATHS } from '../config/api';
import AvatarUpload from '../components/AvatarUpload';
import LearningGoalSelector from '../components/LearningGoalSelector';

interface UserInfo {
  id: string;
  nickname: string;
  avatar: string;
  avatarFileId?: string; // 头像文件ID
  phone?: string;
  email?: string;
  studyDays: number;
  studyHours: number;
  completedCourses: number;
  points: number;
  learningGoal?: LearningGoal | null; // 学习目标
}

interface LearningGoal {
  id: string;
  name: string;
  category: string;
  subcategory: string;
}

const Profile: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [loginType, setLoginType] = useState<'wechat' | 'phone'>('wechat');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [verifyCode, setVerifyCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showGoalSelector, setShowGoalSelector] = useState(false);

  useEffect(() => {
    // 检查本地存储的登录状态
    const savedUserInfo = localStorage.getItem('userInfo');
    if (savedUserInfo) {
      setUserInfo(JSON.parse(savedUserInfo));
      setIsLoggedIn(true);
    }

    // 调试：检查API配置
    console.log('🔧 Profile组件API配置检查:', {
      'API_PATHS.CONSUMER.AUTH.LOGIN': API_PATHS.CONSUMER.AUTH.LOGIN,
      'consumerApi baseURL': 'http://localhost:8082', // 应该是这个
      '当前页面地址': window.location.href
    });

    // 测试API配置
    const testApiConfig = async () => {
      try {
        console.log('🧪 测试API配置...');
        // 这里不实际发送请求，只是测试URL构建
        const testUrl = 'http://localhost:8082' + API_PATHS.CONSUMER.AUTH.LOGIN;
        console.log('🎯 预期API地址:', testUrl);
      } catch (error) {
        console.error('❌ API配置测试失败:', error);
      }
    };
    testApiConfig();
  }, []);

  // 微信登录
  const handleWechatLogin = async () => {
    setIsLoading(true);
    try {
      // 模拟微信授权获取用户信息
      const mockWechatUserInfo = {
        openid: 'wx_openid_' + Date.now(),
        unionid: 'wx_unionid_' + Date.now(),
        nickname: '微信用户' + Math.floor(Math.random() * 1000),
        avatarUrl: 'https://via.placeholder.com/80x80/4A90E2/FFFFFF?text=头像',
        gender: Math.floor(Math.random() * 3), // 0未知 1男 2女
        country: '中国',
        province: '广东省',
        city: '深圳市',
        language: 'zh_CN'
      };

      // 调用后端登录接口
      const loginRequest = {
        loginType: 'wechat',
        wechatUserInfo: mockWechatUserInfo
      };

      console.log('🚀 准备发送登录请求:', {
        url: API_PATHS.CONSUMER.AUTH.LOGIN,
        data: loginRequest,
        '预期完整URL': 'http://localhost:8082' + API_PATHS.CONSUMER.AUTH.LOGIN
      });

      const result = await consumerApi.post(API_PATHS.CONSUMER.AUTH.LOGIN, loginRequest);

      if (result.success || result.code === 200) {
        const responseData = result.data;

        // 保存token
        localStorage.setItem('userToken', responseData.token);
        localStorage.setItem('tokenType', responseData.tokenType);

        // 构建用户信息
        const userInfo: UserInfo = {
          id: responseData.userInfo.userId,
          nickname: responseData.userInfo.nickname,
          avatar: responseData.userInfo.avatarUrl,
          phone: undefined,
          email: undefined,
          studyDays: responseData.userInfo.studyDays || 0,
          studyHours: Math.floor(responseData.userInfo.studyDays * 2.5), // 模拟学习时长
          completedCourses: Math.floor(responseData.userInfo.studyDays / 5), // 模拟完成课程数
          points: responseData.userInfo.totalScore || 0
        };

        setUserInfo(userInfo);
        setIsLoggedIn(true);
        setShowLoginModal(false);

        // 保存到本地存储
        localStorage.setItem('userInfo', JSON.stringify(userInfo));

        alert(responseData.isFirstLogin ? '欢迎加入通学万卷！' : '微信登录成功！');
      } else {
        alert(result.message || '登录失败，请重试');
      }
    } catch (error) {
      console.error('微信登录失败:', error);
      alert('登录失败，请检查网络连接');
    } finally {
      setIsLoading(false);
    }
  };

  // 手机号登录
  const handlePhoneLogin = async () => {
    if (!phoneNumber || !verifyCode) {
      alert('请输入手机号和验证码');
      return;
    }

    setIsLoading(true);
    try {
      // 调用后端登录接口
      const loginRequest = {
        loginType: 'phone',
        loginKey: phoneNumber,
        password: verifyCode // 这里用验证码作为密码
      };

      const result = await consumerApi.post(API_PATHS.CONSUMER.AUTH.LOGIN, loginRequest);

      if (result.success || result.code === 200) {
        const responseData = result.data;

        // 保存token
        localStorage.setItem('userToken', responseData.token);
        localStorage.setItem('tokenType', responseData.tokenType);

        // 构建用户信息
        const userInfo: UserInfo = {
          id: responseData.userInfo.userId,
          nickname: responseData.userInfo.nickname,
          avatar: responseData.userInfo.avatarUrl,
          phone: phoneNumber,
          email: undefined,
          studyDays: responseData.userInfo.studyDays || 0,
          studyHours: Math.floor(responseData.userInfo.studyDays * 2.5),
          completedCourses: Math.floor(responseData.userInfo.studyDays / 5),
          points: responseData.userInfo.totalScore || 0
        };

        setUserInfo(userInfo);
        setIsLoggedIn(true);
        setShowLoginModal(false);

        localStorage.setItem('userInfo', JSON.stringify(userInfo));

        alert('登录成功！');
      } else {
        alert(result.message || '登录失败，请重试');
      }
    } catch (error) {
      console.error('手机号登录失败:', error);
      alert('登录失败，请检查网络连接');
    } finally {
      setIsLoading(false);
    }
  };

  // 发送验证码
  const sendVerifyCode = () => {
    if (!phoneNumber) {
      alert('请输入手机号');
      return;
    }
    alert('验证码已发送到 ' + phoneNumber);
  };

  // 保存学习目标
  const handleSaveLearningGoal = async (goal: LearningGoal) => {
    try {
      // 更新本地用户信息
      if (userInfo) {
        const updatedUserInfo = {
          ...userInfo,
          learningGoal: goal
        };
        setUserInfo(updatedUserInfo);
        localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo));

        // 这里可以调用API保存到后端
        // await consumerApi.post(API_PATHS.CONSUMER.USER.UPDATE_GOAL, goal);

        alert(`学习目标已设置为：${goal.name}`);
      }
    } catch (error) {
      console.error('保存学习目标失败:', error);
      alert('保存失败，请重试');
    }
  };

  // 退出登录
  const handleLogout = async () => {
    try {
      // 调用后端登出接口
      const token = localStorage.getItem('userToken');
      if (token) {
        await consumerApi.post(API_PATHS.CONSUMER.AUTH.LOGOUT);
      }
    } catch (error) {
      console.error('登出接口调用失败:', error);
    } finally {
      // 无论接口是否成功，都清除本地数据
      setIsLoggedIn(false);
      setUserInfo(null);
      localStorage.removeItem('userInfo');
      localStorage.removeItem('userToken');
      localStorage.removeItem('tokenType');
    }
  };

  /**
   * 处理头像上传成功
   */
  const handleAvatarUploadSuccess = (avatarInfo: any) => {
    console.log('🎉 头像上传成功:', avatarInfo);

    if (userInfo) {
      // 更新用户信息中的头像
      const updatedUserInfo = {
        ...userInfo,
        avatar: avatarInfo.accessUrl,
        avatarFileId: avatarInfo.fileId
      };

      setUserInfo(updatedUserInfo);

      // 更新本地存储
      localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo));

      console.log('✅ 用户头像信息已更新');
    }
  };

  /**
   * 处理头像上传失败
   */
  const handleAvatarUploadError = (error: string) => {
    console.error('❌ 头像上传失败:', error);
    // 这里可以添加更多的错误处理逻辑
  };

  const menuItems = [
    { icon: '📚', title: '我的课程', desc: '查看学习进度' },
    { icon: '📊', title: '学习统计', desc: '详细数据分析' },
    { icon: '🏆', title: '我的成就', desc: '查看获得徽章' },
    { icon: '⭐', title: '我的收藏', desc: '收藏的课程' },
    { icon: '📝', title: '学习笔记', desc: '记录学习心得' },
    { icon: '🎯', title: '学习计划', desc: '制定学习目标' },
    { icon: '💬', title: '意见反馈', desc: '帮助我们改进' },
    { icon: '⚙️', title: '设置', desc: '个人设置' }
  ];

  return (
    <div className="profile-container">
      {isLoggedIn && userInfo ? (
        // 已登录状态
        <>
          {/* 用户信息卡片 */}
          <div className="user-card">
            <div className="user-info">
              <div className="avatar-section">
                <AvatarUpload
                  currentAvatar={userInfo.avatar}
                  userId={userInfo.id}
                  onUploadSuccess={handleAvatarUploadSuccess}
                  onUploadError={handleAvatarUploadError}
                  size={80}
                  className="user-avatar-upload"
                />
              </div>
              <div className="user-details">
                <h3 className="user-name">{userInfo.nickname}</h3>
                <p className="user-id">ID: {userInfo.id}</p>
                {userInfo.phone && <p className="user-phone">{userInfo.phone}</p>}
              </div>
            </div>
            <button className="edit-profile-btn">编辑资料</button>
          </div>

          {/* 学习统计 */}
          <div className="stats-section">
            <div className="stats-grid">
              <div className="stat-item">
                <div className="stat-number">{userInfo.studyDays}</div>
                <div className="stat-label">学习天数</div>
              </div>
              <div className="stat-item">
                <div className="stat-number">{userInfo.studyHours}</div>
                <div className="stat-label">学习时长(h)</div>
              </div>
              <div className="stat-item">
                <div className="stat-number">{userInfo.completedCourses}</div>
                <div className="stat-label">完成课程</div>
              </div>
              <div className="stat-item">
                <div className="stat-number">{userInfo.points}</div>
                <div className="stat-label">学习积分</div>
              </div>
            </div>
          </div>

          {/* 学习目标 */}
          <div className="goal-section">
            <div className="goal-header">
              <h3>🎯 我的学习目标</h3>
              <button
                className="goal-edit-btn"
                onClick={() => setShowGoalSelector(true)}
              >
                {userInfo.learningGoal ? '修改' : '设置'}
              </button>
            </div>
            {userInfo.learningGoal ? (
              <div className="goal-card">
                <div className="goal-info">
                  <div className="goal-name">{userInfo.learningGoal.name}</div>
                  <div className="goal-path">
                    {userInfo.learningGoal.category} › {userInfo.learningGoal.subcategory}
                  </div>
                </div>
                <div className="goal-status">
                  <div className="goal-progress">
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '35%'}}></div>
                    </div>
                    <span className="progress-text">35% 完成</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="goal-empty">
                <div className="empty-icon">🎯</div>
                <div className="empty-text">还未设置学习目标</div>
                <div className="empty-desc">设置目标，让学习更有方向</div>
                <button
                  className="set-goal-btn"
                  onClick={() => setShowGoalSelector(true)}
                >
                  立即设置
                </button>
              </div>
            )}
          </div>

          {/* 功能菜单 */}
          <div className="menu-section">
            {menuItems.map((item, index) => (
              <div
                key={index}
                className="menu-item"
                onClick={() => {
                  if (item.title === '学习计划') {
                    setShowGoalSelector(true);
                  }
                }}
                style={{ cursor: item.title === '学习计划' ? 'pointer' : 'default' }}
              >
                <div className="menu-icon">{item.icon}</div>
                <div className="menu-content">
                  <div className="menu-title">{item.title}</div>
                  <div className="menu-desc">{item.desc}</div>
                </div>
                <div className="menu-arrow">→</div>
              </div>
            ))}
          </div>

          {/* 退出登录 */}
          <div className="logout-section">
            <button className="logout-btn" onClick={handleLogout}>
              退出登录
            </button>
          </div>
        </>
      ) : (
        // 未登录状态
        <div className="login-prompt">
          <div className="login-icon">👤</div>
          <h3>欢迎来到通学万卷</h3>
          <p>登录后可享受更多学习功能</p>
          <button 
            className="login-btn"
            onClick={() => setShowLoginModal(true)}
          >
            立即登录
          </button>
        </div>
      )}

      {/* 登录弹窗 */}
      {showLoginModal && (
        <div className="modal-overlay" onClick={() => setShowLoginModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>登录</h3>
              <button 
                className="close-btn"
                onClick={() => setShowLoginModal(false)}
              >
                ×
              </button>
            </div>

            <div className="login-tabs">
              <button 
                className={`tab-btn ${loginType === 'wechat' ? 'active' : ''}`}
                onClick={() => setLoginType('wechat')}
              >
                微信登录
              </button>
              <button 
                className={`tab-btn ${loginType === 'phone' ? 'active' : ''}`}
                onClick={() => setLoginType('phone')}
              >
                手机登录
              </button>
            </div>

            <div className="login-content">
              {loginType === 'wechat' ? (
                <div className="wechat-login">
                  <div className="wechat-icon">💬</div>
                  <p>使用微信账号快速登录</p>
                  <button 
                    className="wechat-login-btn"
                    onClick={handleWechatLogin}
                    disabled={isLoading}
                  >
                    {isLoading ? '登录中...' : '微信登录'}
                  </button>
                </div>
              ) : (
                <div className="phone-login">
                  <div className="input-group">
                    <input
                      type="tel"
                      placeholder="请输入手机号"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                      className="phone-input"
                    />
                  </div>
                  <div className="input-group">
                    <input
                      type="text"
                      placeholder="请输入验证码"
                      value={verifyCode}
                      onChange={(e) => setVerifyCode(e.target.value)}
                      className="code-input"
                    />
                    <button 
                      className="send-code-btn"
                      onClick={sendVerifyCode}
                    >
                      发送验证码
                    </button>
                  </div>
                  <button 
                    className="phone-login-btn"
                    onClick={handlePhoneLogin}
                    disabled={isLoading}
                  >
                    {isLoading ? '登录中...' : '登录'}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 学习目标选择器 */}
      <LearningGoalSelector
        isVisible={showGoalSelector}
        onClose={() => setShowGoalSelector(false)}
        onSave={handleSaveLearningGoal}
        currentGoal={userInfo?.learningGoal}
      />
    </div>
  );
};

export default Profile;

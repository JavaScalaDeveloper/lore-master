import React, { useState } from 'react';
import { Layout, Button, Avatar, Dropdown, Space, Typography, Card, Row, Col, Statistic } from 'antd';
import { UserOutlined, LoginOutlined, UserAddOutlined, BookOutlined, TrophyOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import UserRegisterModal from './components/UserRegisterModal';
import UserLoginModal from './components/UserLoginModal';

const { Header, Content, Footer } = Layout;
const { Title, Paragraph } = Typography;

/**
 * 业务端主页面
 */
const BusinessHomePage: React.FC = () => {
  const [isRegisterModalVisible, setIsRegisterModalVisible] = useState(false);
  const [isLoginModalVisible, setIsLoginModalVisible] = useState(false);
  const [userInfo, setUserInfo] = useState<any>(null);

  // 用户菜单项
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      label: '个人中心',
      icon: <UserOutlined />,
    },
    {
      key: 'logout',
      label: '退出登录',
      icon: <LoginOutlined />,
    },
  ];

  // 未登录用户菜单项
  const guestMenuItems: MenuProps['items'] = [
    {
      key: 'login',
      label: '登录',
      icon: <LoginOutlined />,
      onClick: () => setIsLoginModalVisible(true),
    },
    {
      key: 'register',
      label: '注册',
      icon: <UserAddOutlined />,
      onClick: () => setIsRegisterModalVisible(true),
    },
  ];

  // 处理用户菜单点击
  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'logout') {
      setUserInfo(null);
      localStorage.removeItem('userInfo');
      localStorage.removeItem('accessToken');
    }
  };

  // 处理注册成功
  const handleRegisterSuccess = (userData: any) => {
    setUserInfo(userData);
    localStorage.setItem('userInfo', JSON.stringify(userData));
    localStorage.setItem('accessToken', userData.accessToken);
    setIsRegisterModalVisible(false);
  };

  // 处理登录成功
  const handleLoginSuccess = (userData: any) => {
    setUserInfo(userData);
    localStorage.setItem('userInfo', JSON.stringify(userData));
    localStorage.setItem('accessToken', userData.accessToken);
    setIsLoginModalVisible(false);
  };

  // 页面加载时检查本地存储的用户信息
  React.useEffect(() => {
    const storedUserInfo = localStorage.getItem('userInfo');
    if (storedUserInfo) {
      try {
        setUserInfo(JSON.parse(storedUserInfo));
      } catch (error) {
        console.error('解析用户信息失败:', error);
        localStorage.removeItem('userInfo');
      }
    }
  }, []);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 头部导航 */}
      <Header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: '#fff',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        padding: '0 24px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <BookOutlined style={{ fontSize: '24px', color: '#1890ff', marginRight: '12px' }} />
          <Title level={3} style={{ margin: 0, color: '#1890ff' }}>
            通学万卷
          </Title>
        </div>

        {/* 用户中心 */}
        <div>
          {userInfo ? (
            <Dropdown
              menu={{
                items: userMenuItems,
                onClick: handleUserMenuClick
              }}
              placement="bottomRight"
            >
              <Space style={{ cursor: 'pointer' }}>
                <Avatar
                  src={userInfo.avatarUrl}
                  icon={<UserOutlined />}
                  size="default"
                />
                <span>{userInfo.nickname}</span>
              </Space>
            </Dropdown>
          ) : (
            <Dropdown
              menu={{ items: guestMenuItems }}
              placement="bottomRight"
            >
              <Button type="primary" icon={<UserOutlined />}>
                用户中心
              </Button>
            </Dropdown>
          )}
        </div>
      </Header>

      {/* 主要内容区域 */}
      <Content style={{ padding: '24px' }}>
        {userInfo ? (
          // 已登录用户的主页内容
          <div>
            <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
              <Col span={24}>
                <Card>
                  <Title level={2}>
                    欢迎回来，{userInfo.nickname}！
                  </Title>
                  <Paragraph>
                    用户ID: {userInfo.userId} | 当前等级: Lv.{userInfo.currentLevel} | 总积分: {userInfo.totalScore}
                  </Paragraph>
                </Card>
              </Col>
            </Row>

            <Row gutter={[16, 16]}>
              <Col xs={24} sm={12} md={8}>
                <Card>
                  <Statistic
                    title="当前等级"
                    value={userInfo.currentLevel}
                    prefix={<TrophyOutlined />}
                    suffix="级"
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Card>
                  <Statistic
                    title="总积分"
                    value={userInfo.totalScore}
                    prefix={<BookOutlined />}
                    suffix="分"
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Card>
                  <Statistic
                    title="学习天数"
                    value={userInfo.studyDays || 0}
                    prefix={<ClockCircleOutlined />}
                    suffix="天"
                  />
                </Card>
              </Col>
            </Row>
          </div>
        ) : (
          // 未登录用户的欢迎页面
          <div style={{ textAlign: 'center', padding: '60px 0' }}>
            <BookOutlined style={{ fontSize: '72px', color: '#1890ff', marginBottom: '24px' }} />
            <Title level={1}>欢迎来到通学万卷</Title>
            <Paragraph style={{ fontSize: '18px', marginBottom: '32px' }}>
              一个专业的在线学习平台，助您掌握各领域知识
            </Paragraph>
            <Space size="large">
              <Button
                type="primary"
                size="large"
                icon={<UserAddOutlined />}
                onClick={() => setIsRegisterModalVisible(true)}
              >
                立即注册
              </Button>
              <Button
                size="large"
                icon={<LoginOutlined />}
                onClick={() => setIsLoginModalVisible(true)}
              >
                用户登录
              </Button>
            </Space>
          </div>
        )}
      </Content>

      {/* 页脚 */}
      <Footer style={{ textAlign: 'center', background: '#f0f2f5' }}>
        通学万卷 ©2025 Created by Lore Master Team
      </Footer>

      {/* 注册弹窗 */}
      <UserRegisterModal
        visible={isRegisterModalVisible}
        onCancel={() => setIsRegisterModalVisible(false)}
        onSuccess={handleRegisterSuccess}
      />

      {/* 登录弹窗 */}
      <UserLoginModal
        visible={isLoginModalVisible}
        onCancel={() => setIsLoginModalVisible(false)}
        onSuccess={handleLoginSuccess}
      />
    </Layout>
  );
};

export default BusinessHomePage;
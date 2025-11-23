import React, { useState } from 'react';
import { Layout, Button, Card, Row, Col, Typography, Space, Divider, Tag } from 'antd';
import {
  BookOutlined,
  TrophyOutlined,
  UserOutlined,
  StarOutlined,
  PlayCircleOutlined,
  ReadOutlined,
  TeamOutlined,
  RocketOutlined,
  MenuOutlined
} from '@ant-design/icons';
import UserLoginModal from './components/UserLoginModal';
import UserRegisterModal from './components/UserRegisterModal';

const { Header, Content, Footer } = Layout;
const { Title, Paragraph, Text } = Typography;

const ConsumerPage: React.FC = () => {
  const [loginModalVisible, setLoginModalVisible] = useState(false);
  const [registerModalVisible, setRegisterModalVisible] = useState(false);

  // 模拟用户登录状态
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState<any>(null);

  const handleLogin = (userData: any) => {
    setIsLoggedIn(true);
    setUserInfo(userData);
    setLoginModalVisible(false);
  };

  const handleRegister = (userData: any) => {
    setIsLoggedIn(true);
    setUserInfo(userData);
    setRegisterModalVisible(false);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setUserInfo(null);
  };

  return (
    <Layout className="min-h-screen">
      {/* 顶部导航 */}
      <Header
        className="bg-white shadow-sm border-b fixed top-0 left-0 right-0 z-50"
        style={{
          padding: 0,
          backgroundColor: '#ffffff',
          borderBottom: '1px solid #e5e7eb',
          boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)'
        }}
      >
        <div
          className="max-w-7xl mx-auto px-6 h-16"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            height: '64px'
          }}
        >
          {/* Logo - 左侧 */}
          <div className="flex items-center flex-shrink-0">
            <BookOutlined className="text-2xl text-blue-600 mr-3" />
            <Title level={3} className="mb-0 text-blue-600" style={{ margin: 0, fontSize: '20px', fontWeight: 600 }}>
              通学万卷
            </Title>
          </div>

          {/* 中间导航菜单 - 居中 */}
          <div className="hidden md:flex items-center space-x-8 flex-1 justify-center">
            <a href="#" className="text-gray-700 hover:text-blue-600 transition-colors font-medium px-3 py-2 rounded-md">
              首页
            </a>
            <a href="#" className="text-gray-700 hover:text-blue-600 transition-colors font-medium px-3 py-2 rounded-md">
              课程
            </a>
            <a href="#" className="text-gray-700 hover:text-blue-600 transition-colors font-medium px-3 py-2 rounded-md">
              练习
            </a>
            <a href="#" className="text-gray-700 hover:text-blue-600 transition-colors font-medium px-3 py-2 rounded-md">
              排行榜
            </a>
          </div>

          {/* 右上角用户操作区 - 右侧 */}
          <div className="flex items-center space-x-3 flex-shrink-0">
            {/* 移动端菜单按钮 */}
            <Button
              type="text"
              icon={<MenuOutlined />}
              className="md:hidden text-gray-700 hover:text-blue-600"
            />

            {/* 用户操作按钮 */}
            {isLoggedIn ? (
              <Space size="middle" className="hidden sm:flex">
                <div className="flex items-center space-x-2">
                  <UserOutlined className="text-gray-600" />
                  <Text className="text-gray-700 font-medium">
                    欢迎，{userInfo?.nickname || '用户'}
                  </Text>
                </div>
                <Button
                  type="text"
                  onClick={handleLogout}
                  className="text-gray-600 hover:text-red-500 hover:bg-red-50"
                  style={{ height: '36px', padding: '0 12px' }}
                >
                  退出登录
                </Button>
              </Space>
            ) : (
              <Space size="small">
                <Button
                  type="text"
                  onClick={() => setLoginModalVisible(true)}
                  className="text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium hidden sm:inline-flex"
                  style={{ height: '36px', padding: '0 12px' }}
                >
                  登录
                </Button>
                <Button
                  type="primary"
                  onClick={() => setRegisterModalVisible(true)}
                  className="bg-blue-600 hover:bg-blue-700 border-blue-600 hover:border-blue-700 font-medium"
                  style={{ height: '36px', padding: '0 16px' }}
                >
                  注册
                </Button>
              </Space>
            )}

            {/* 移动端已登录用户头像 */}
            {isLoggedIn && (
              <Button
                type="text"
                icon={<UserOutlined />}
                className="sm:hidden text-gray-700 hover:text-blue-600"
                onClick={handleLogout}
              />
            )}
          </div>
        </div>
      </Header>

      {/* 主要内容 */}
      <Content style={{ marginTop: '64px' }}>
        {/* Hero Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white py-20" style={{ marginTop: 0 }}>
          <div className="max-w-7xl mx-auto px-4 text-center">
            <Title level={1} className="text-white mb-6">
              开启你的学习之旅
            </Title>
            <Paragraph className="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
              通过科学的学习方法和丰富的课程内容，让学习变得更加高效有趣。
              加入我们，与千万学习者一起成长！
            </Paragraph>
            <Space size="large">
              <Button
                type="primary"
                size="large"
                icon={<PlayCircleOutlined />}
                onClick={() => setRegisterModalVisible(true)}
              >
                立即开始学习
              </Button>
              <Button
                size="large"
                className="bg-white text-blue-600 border-white hover:bg-blue-50"
              >
                了解更多
              </Button>
            </Space>
          </div>
        </div>

        {/* 特色功能 */}
        <div className="py-16 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4">
            <div className="text-center mb-12">
              <Title level={2}>为什么选择通学万卷？</Title>
              <Paragraph className="text-lg text-gray-600">
                我们提供最优质的学习体验和最完善的学习工具
              </Paragraph>
            </div>

            <Row gutter={[32, 32]}>
              <Col xs={24} sm={12} lg={6}>
                <Card className="text-center h-full hover:shadow-lg transition-shadow">
                  <ReadOutlined className="text-4xl text-blue-600 mb-4" />
                  <Title level={4}>丰富课程</Title>
                  <Paragraph className="text-gray-600">
                    涵盖各个学科的精品课程，满足不同学习需求
                  </Paragraph>
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card className="text-center h-full hover:shadow-lg transition-shadow">
                  <TrophyOutlined className="text-4xl text-green-600 mb-4" />
                  <Title level={4}>智能练习</Title>
                  <Paragraph className="text-gray-600">
                    AI驱动的个性化练习，针对性提升学习效果
                  </Paragraph>
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card className="text-center h-full hover:shadow-lg transition-shadow">
                  <TeamOutlined className="text-4xl text-purple-600 mb-4" />
                  <Title level={4}>学习社区</Title>
                  <Paragraph className="text-gray-600">
                    与同学交流讨论，分享学习心得和经验
                  </Paragraph>
                </Card>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Card className="text-center h-full hover:shadow-lg transition-shadow">
                  <RocketOutlined className="text-4xl text-red-600 mb-4" />
                  <Title level={4}>进步追踪</Title>
                  <Paragraph className="text-gray-600">
                    详细的学习数据分析，让进步看得见
                  </Paragraph>
                </Card>
              </Col>
            </Row>
          </div>
        </div>

        {/* 学习数据展示 */}
        <div className="py-16">
          <div className="max-w-7xl mx-auto px-4">
            <div className="text-center mb-12">
              <Title level={2}>学习成果一目了然</Title>
              <Paragraph className="text-lg text-gray-600">
                实时追踪你的学习进度，见证每一次进步
              </Paragraph>
            </div>

            <Row gutter={[32, 32]} className="text-center">
              <Col xs={24} sm={8}>
                <div className="p-8">
                  <div className="text-4xl font-bold text-blue-600 mb-2">1,000,000+</div>
                  <div className="text-lg text-gray-600">注册用户</div>
                </div>
              </Col>
              <Col xs={24} sm={8}>
                <div className="p-8">
                  <div className="text-4xl font-bold text-green-600 mb-2">50,000+</div>
                  <div className="text-lg text-gray-600">精品课程</div>
                </div>
              </Col>
              <Col xs={24} sm={8}>
                <div className="p-8">
                  <div className="text-4xl font-bold text-purple-600 mb-2">99.8%</div>
                  <div className="text-lg text-gray-600">用户满意度</div>
                </div>
              </Col>
            </Row>
          </div>
        </div>

        {/* 热门课程 */}
        <div className="py-16 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4">
            <div className="text-center mb-12">
              <Title level={2}>热门课程推荐</Title>
              <Paragraph className="text-lg text-gray-600">
                精选优质课程，助你快速提升
              </Paragraph>
            </div>

            <Row gutter={[24, 24]}>
              {[
                {
                  title: '高等数学基础',
                  description: '从零开始学习高等数学，掌握微积分基本概念',
                  students: '12,345',
                  rating: '4.9',
                  price: '免费',
                  tag: '热门'
                },
                {
                  title: '英语口语提升',
                  description: '实用英语口语训练，提升日常交流能力',
                  students: '8,976',
                  rating: '4.8',
                  price: '¥199',
                  tag: '推荐'
                },
                {
                  title: '编程入门指南',
                  description: 'Python编程从入门到实践，适合零基础学员',
                  students: '15,678',
                  rating: '4.9',
                  price: '¥299',
                  tag: '新课'
                }
              ].map((course, index) => (
                <Col xs={24} sm={12} lg={8} key={index}>
                  <Card
                    hoverable
                    cover={
                      <div className="h-48 bg-gradient-to-r from-blue-400 to-purple-500 flex items-center justify-center">
                        <BookOutlined className="text-6xl text-white" />
                      </div>
                    }
                    actions={[
                      <Button type="primary" block>立即学习</Button>
                    ]}
                  >
                    <div className="mb-2">
                      <Tag color={course.tag === '热门' ? 'red' : course.tag === '推荐' ? 'blue' : 'green'}>
                        {course.tag}
                      </Tag>
                    </div>
                    <Card.Meta
                      title={course.title}
                      description={course.description}
                    />
                    <div className="mt-4 flex justify-between items-center text-sm text-gray-500">
                      <span>{course.students} 人学习</span>
                      <span>
                        <StarOutlined className="text-yellow-400 mr-1" />
                        {course.rating}
                      </span>
                    </div>
                    <div className="mt-2 text-right">
                      <span className="text-lg font-bold text-red-500">{course.price}</span>
                    </div>
                  </Card>
                </Col>
              ))}
            </Row>
          </div>
        </div>
      </Content>

      {/* 底部 */}
      <Footer className="bg-gray-800 text-white">
        <div className="max-w-7xl mx-auto px-4 py-12">
          <Row gutter={[32, 32]}>
            <Col xs={24} sm={12} lg={6}>
              <div className="mb-4">
                <BookOutlined className="text-2xl text-blue-400 mr-2" />
                <span className="text-xl font-bold">通学万卷</span>
              </div>
              <Paragraph className="text-gray-300">
                专注于提供优质的在线学习体验，让每个人都能享受学习的乐趣。
              </Paragraph>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Title level={5} className="text-white mb-4">产品</Title>
              <div className="space-y-2">
                <div><a href="#" className="text-gray-300 hover:text-white">在线课程</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">智能练习</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">学习社区</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">移动应用</a></div>
              </div>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Title level={5} className="text-white mb-4">支持</Title>
              <div className="space-y-2">
                <div><a href="#" className="text-gray-300 hover:text-white">帮助中心</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">联系我们</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">用户反馈</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">服务条款</a></div>
              </div>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Title level={5} className="text-white mb-4">关注我们</Title>
              <div className="space-y-2">
                <div><a href="#" className="text-gray-300 hover:text-white">微信公众号</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">新浪微博</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">QQ群</a></div>
                <div><a href="#" className="text-gray-300 hover:text-white">官方邮箱</a></div>
              </div>
            </Col>
          </Row>
          <Divider className="border-gray-600" />
          <div className="text-center text-gray-400">
            <p>&copy; 2025 通学万卷. All rights reserved.</p>
          </div>
        </div>
      </Footer>

      {/* 登录弹窗 */}
      <UserLoginModal
        visible={loginModalVisible}
        onCancel={() => setLoginModalVisible(false)}
        onSuccess={handleLogin}
      />

      {/* 注册弹窗 */}
      <UserRegisterModal
        visible={registerModalVisible}
        onCancel={() => setRegisterModalVisible(false)}
        onSuccess={handleRegister}
      />
    </Layout>
  );
};

export default ConsumerPage;
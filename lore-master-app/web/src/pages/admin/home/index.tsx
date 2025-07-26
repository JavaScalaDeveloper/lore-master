import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Button, Space, Typography } from 'antd';
import {
  UserOutlined,
  BookOutlined,
  BarChartOutlined,
  LogoutOutlined,
  BellOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DatabaseOutlined,
  SettingOutlined,
  QuestionCircleOutlined,
  TrophyOutlined,
  FileTextOutlined,
  ExperimentOutlined
} from '@ant-design/icons';
import Overview from './Overview/index';
import UserManage from './UserManage';
import CareerTargetManage from './CareerTargetManage/index';
import KnowledgeManage from './KnowledgeManage/index';
import QuestionManage from './QuestionManage/index';
import SubjectManage from './SubjectManage/index';
import LevelManage from './LevelManage/index';
import CryptoTest from './CryptoTest/index';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

const menuItems = [
  {
    key: 'overview',
    icon: <BarChartOutlined />,
    label: '数据概览',
  },
  {
    key: 'user',
    icon: <UserOutlined />,
    label: '用户管理',
  },
  {
    key: 'career',
    icon: <BookOutlined />,
    label: '职业目标管理',
  },
  {
    key: 'knowledge',
    icon: <DatabaseOutlined />,
    label: '知识点管理',
  },
  {
    key: 'question',
    icon: <QuestionCircleOutlined />,
    label: '题库管理',
  },
  {
    key: 'subject',
    icon: <FileTextOutlined />,
    label: '学科管理',
  },
  {
    key: 'level',
    icon: <TrophyOutlined />,
    label: '等级管理',
  },
  {
    key: 'crypto-test',
    icon: <ExperimentOutlined />,
    label: '加密测试',
  },
  {
    key: 'settings',
    icon: <SettingOutlined />,
    label: '系统设置',
  },
];

const Home: React.FC = () => {
  const [selectedKey, setSelectedKey] = useState('overview');
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUser');
    window.location.href = '/login';
  };

  const userMenuItems = [
    {
      key: 'profile',
      label: '个人资料',
      icon: <UserOutlined />,
    },
    {
      key: 'logout',
      label: '退出登录',
      icon: <LogoutOutlined />,
      onClick: handleLogout,
    },
  ];

  const renderContent = () => {
    switch (selectedKey) {
      case 'overview':
        return <Overview />;
      case 'user':
        return <UserManage />;
      case 'career':
        return <CareerTargetManage />;
      case 'knowledge':
        return <KnowledgeManage />;
      case 'question':
        return <QuestionManage />;
      case 'subject':
        return <SubjectManage />;
      case 'level':
        return <LevelManage />;
      case 'crypto-test':
        return <CryptoTest />;
      case 'settings':
        return (
          <div style={{ padding: 24 }}>
            <h2>⚙️ 系统设置</h2>
            <p>系统配置和参数设置功能开发中...</p>
          </div>
        );
      default:
        return <Overview />;
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={240}
        style={{
          background: '#001529',
          boxShadow: '2px 0 6px rgba(0,21,41,.35)'
        }}
      >
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: '1px solid #303030'
        }}>
          <Title level={4} style={{
            color: '#fff',
            margin: 0,
            fontSize: collapsed ? '16px' : '18px'
          }}>
            {collapsed ? '通学' : '通学万卷'}
          </Title>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          style={{ borderRight: 0 }}
          items={menuItems}
          onClick={({ key }) => setSelectedKey(key as string)}
        />
      </Sider>
      <Layout>
        <Header style={{
          background: '#fff',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 1px 4px rgba(0,21,41,.08)'
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: '16px', width: 64, height: 64 }}
          />
          <Space size="middle">
            <Button type="text" icon={<BellOutlined />} />
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} />
                <span>管理员</span>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content style={{
          margin: '24px',
          padding: '24px',
          background: '#fff',
          borderRadius: '8px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)'
        }}>
          {renderContent()}
        </Content>
      </Layout>
    </Layout>
  );
};

export default Home; 
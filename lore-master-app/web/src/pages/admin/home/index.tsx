import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Button, Space, Typography } from 'antd';
import {
  UserOutlined,
  BookOutlined,
  DatabaseOutlined,
  BarChartOutlined,
  SettingOutlined,
  LogoutOutlined,
  BellOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons';
import UserManage from './UserManage';
import CareerTargetManage from './CareerTargetManage/index';

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
        return <div style={{ padding: 24 }}>数据概览功能开发中...</div>;
      case 'user':
        return <UserManage />;
      case 'career':
        return <CareerTargetManage />;
      default:
        return <div style={{ padding: 24 }}>数据概览功能开发中...</div>;
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
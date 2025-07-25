import React, { useState } from 'react';
import { Layout, Menu } from 'antd';
import UserManage from './UserManage';

const { Sider, Content } = Layout;

const menuItems = [
  { key: 'user', label: '用户管理' },
  // 可扩展更多菜单
];

const Home: React.FC = () => {
  const [selectedKey, setSelectedKey] = useState('user');

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={200} style={{ background: '#fff' }}>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          style={{ height: '100%', borderRight: 0 }}
          items={menuItems}
          onClick={({ key }) => setSelectedKey(key as string)}
        />
      </Sider>
      <Layout style={{ padding: '24px' }}>
        <Content style={{ background: '#fff', padding: 24, minHeight: 280 }}>
          {selectedKey === 'user' && <UserManage />}
        </Content>
      </Layout>
    </Layout>
  );
};

export default Home; 
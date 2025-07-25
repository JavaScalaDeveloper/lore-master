import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import axios from 'axios';
import { sha256 } from 'js-sha256';
import { useNavigate } from 'react-router-dom';

// @ts-ignore
const sha1 = require('js-sha1');
// @ts-ignore
const md5 = require('blueimp-md5');

// 多重 hash
const multiHash = (pwd: string) => md5(sha1(sha256(pwd)));

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      // 多重 hash
      const hashedPassword = multiHash(values.password);
      const response = await axios.post('/api/admin/login', {
        username: values.username,
        password: hashedPassword,
      }, { withCredentials: true });
      if (response.data && response.data.success) {
        // 后端返回的token在data.data.token中
        if (response.data.data && response.data.data.token) {
          localStorage.setItem('adminToken', response.data.data.token);
        }
        localStorage.setItem('adminUser', JSON.stringify({ username: values.username }));
        message.success('登录成功！');
        navigate('/home', { replace: true });
      } else {
        message.error(response.data.message || '登录失败');
      }
    } catch (error: any) {
      message.error(error?.response?.data?.message || '网络错误');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ width: 360, margin: '120px auto', padding: 32, boxShadow: '0 2px 8px #f0f1f2', borderRadius: 8, background: '#fff' }}>
      <h2 style={{ textAlign: 'center', marginBottom: 24 }}>管理端登录</h2>
      <Form name="login" onFinish={onFinish} autoComplete="off">
        <Form.Item name="username" rules={[{ required: true, message: '请输入账号' }]}> 
          <Input placeholder="账号" />
        </Form.Item>
        <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}> 
          <Input.Password placeholder="密码" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            登录
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default Login; 
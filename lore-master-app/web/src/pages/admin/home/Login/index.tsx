import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { encryptPasswordForTransmission } from '../../../../utils/crypto';

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      // 加密密码后传输到后端
      const encryptedPassword = encryptPasswordForTransmission(values.password);
      console.log('原始密码:', values.password);
      console.log('加密后密码:', encryptedPassword);

      const response = await axios.post('/api/admin/auth/login', {
        username: values.username,
        password: encryptedPassword, // 传输加密后的密码
      });

      // 检查响应格式：{ code: 200, message: "登录成功", data: {...}, timestamp: ... }
      if (response.data && response.data.code === 200) {
        const { data } = response.data;

        // 保存token和用户信息
        if (data && data.token) {
          localStorage.setItem('adminToken', data.token);
          localStorage.setItem('tokenType', data.tokenType || 'Bearer');
        }

        // 保存用户信息
        if (data && data.userInfo) {
          localStorage.setItem('adminUser', JSON.stringify(data.userInfo));
        }

        message.success(response.data.message || '登录成功！');

        // 延迟跳转，确保localStorage已保存
        setTimeout(() => {
          navigate('/home', { replace: true });
        }, 300);
      } else {
        message.error(response.data.message || '登录失败');
      }
    } catch (error: any) {
      console.error('登录错误:', error);
      if (error.response && error.response.data) {
        message.error(error.response.data.message || '登录失败');
      } else {
        message.error('网络错误，请检查网络连接');
      }
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
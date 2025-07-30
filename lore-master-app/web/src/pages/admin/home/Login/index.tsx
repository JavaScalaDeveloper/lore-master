import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { encryptPasswordForTransmission } from '../../../../utils/crypto';
import { adminApi } from '../../../../utils/request';
import { API_PATHS } from '../../../../config/api';

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

      const response = await adminApi.post(API_PATHS.ADMIN.AUTH.LOGIN, {
        username: values.username,
        password: encryptedPassword, // 传输加密后的密码
      });

      console.log('🔍 登录响应数据:', response);

      // 检查响应格式：{ code: 200, message: "登录成功", data: {...}, timestamp: ... }
      if (response && response.code === 200) {
        const { data } = response;

        // 保存token和用户信息
        if (data && data.token) {
          localStorage.setItem('adminToken', data.token);
          localStorage.setItem('tokenType', data.tokenType || 'Bearer');
          console.log('✅ Token已保存:', data.token);
        }

        // 保存用户信息
        if (data && data.userInfo) {
          localStorage.setItem('adminUser', JSON.stringify(data.userInfo));
          console.log('✅ 用户信息已保存:', data.userInfo);
        } else {
          // 如果没有userInfo，创建一个默认的
          const defaultUser = { username: values.username, id: 1 };
          localStorage.setItem('adminUser', JSON.stringify(defaultUser));
          console.log('✅ 默认用户信息已保存:', defaultUser);
        }

        message.success(response.message || '登录成功！');

        // 触发登录状态变化事件
        window.dispatchEvent(new Event('loginStateChange'));

        // 延迟跳转，确保localStorage已保存和状态更新
        setTimeout(() => {
          console.log('🔄 准备跳转到管理端首页');
          navigate('/admin/home', { replace: true });
        }, 500);
      } else {
        console.log('❌ 登录失败，响应数据:', response);
        message.error(response?.message || '登录失败');
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
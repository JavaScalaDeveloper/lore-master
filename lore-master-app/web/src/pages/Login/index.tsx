import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import axios from 'axios';
import JSEncrypt from 'jsencrypt';

// 示例公钥，实际请替换为后端提供的公钥
const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn1Qw...你的公钥...IDAQAB\n-----END PUBLIC KEY-----`;

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      // 用RSA公钥加密密码
      const encrypt = new JSEncrypt();
      encrypt.setPublicKey(PUBLIC_KEY);
      const encryptedPassword = encrypt.encrypt(values.password);
      if (!encryptedPassword) {
        message.error('密码加密失败，请重试');
        setLoading(false);
        return;
      }
      // 调用后端登录接口
      const response = await axios.post('/api/admin/login', {
        username: values.username,
        password: encryptedPassword,
      });
      if (response.data && response.data.success) {
        message.success('登录成功！');
        // TODO: 跳转到管理后台首页
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
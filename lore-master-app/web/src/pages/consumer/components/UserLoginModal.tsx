import React, { useState } from 'react';
import { Modal, Form, Input, Button, Checkbox, message, Divider, Space } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, QqOutlined, WechatOutlined } from '@ant-design/icons';
import { encryptPasswordForTransmission } from '../../../utils/crypto';

interface UserLoginModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: (userData: any) => void;
}

const UserLoginModal: React.FC<UserLoginModalProps> = ({ visible, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [loginType, setLoginType] = useState<'username' | 'email'>('username');

  const handleLogin = async (values: any) => {
    setLoading(true);
    
    try {
      const requestData = {
        loginType: loginType,
        loginKey: values.loginKey,
        password: encryptPasswordForTransmission(values.password),
        rememberMe: values.rememberMe || false,
      };

      console.log('登录请求数据:', requestData);

      // 调用C端用户登录API
      const response = await fetch('http://localhost:8082/api/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      });

      const result = await response.json();
      console.log('登录响应:', result);

      if (result.success) {
        message.success('登录成功！');
        onSuccess(result.data);
        form.resetFields();
      } else {
        message.error(result.message || '登录失败');
      }
    } catch (error) {
      console.error('登录请求失败:', error);
      message.error('网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleThirdPartyLogin = (type: 'qq' | 'wechat') => {
    message.info(`${type === 'qq' ? 'QQ' : '微信'}登录功能开发中...`);
  };

  return (
    <Modal
      title="用户登录"
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={400}
      destroyOnClose
    >
      <div className="py-4">
        {/* 登录方式切换 */}
        <div className="flex justify-center mb-6">
          <Space>
            <Button
              type={loginType === 'username' ? 'primary' : 'default'}
              onClick={() => setLoginType('username')}
            >
              用户名登录
            </Button>
            <Button
              type={loginType === 'email' ? 'primary' : 'default'}
              onClick={() => setLoginType('email')}
            >
              邮箱登录
            </Button>
          </Space>
        </div>

        <Form
          form={form}
          name="login"
          onFinish={handleLogin}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            name="loginKey"
            rules={[
              { required: true, message: `请输入${loginType === 'username' ? '用户名' : '邮箱'}` },
              loginType === 'email' ? { type: 'email', message: '请输入有效的邮箱地址' } : {},
            ]}
          >
            <Input
              prefix={loginType === 'username' ? <UserOutlined /> : <MailOutlined />}
              placeholder={loginType === 'username' ? '请输入用户名' : '请输入邮箱'}
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入密码"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <div className="flex justify-between items-center">
              <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                <Checkbox>记住我</Checkbox>
              </Form.Item>
              <a href="#" className="text-blue-600 hover:text-blue-800">
                忘记密码？
              </a>
            </div>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              size="large"
              block
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        <Divider>或</Divider>

        {/* 第三方登录 */}
        <div className="text-center">
          <Space size="large">
            <Button
              icon={<QqOutlined />}
              shape="circle"
              size="large"
              className="text-blue-600 border-blue-600 hover:bg-blue-50"
              onClick={() => handleThirdPartyLogin('qq')}
            />
            <Button
              icon={<WechatOutlined />}
              shape="circle"
              size="large"
              className="text-green-600 border-green-600 hover:bg-green-50"
              onClick={() => handleThirdPartyLogin('wechat')}
            />
          </Space>
        </div>

        <div className="text-center mt-4">
          <span className="text-gray-500">还没有账号？</span>
          <a href="#" className="text-blue-600 hover:text-blue-800 ml-1">
            立即注册
          </a>
        </div>
      </div>
    </Modal>
  );
};

export default UserLoginModal;

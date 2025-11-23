import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Button, message, Tabs, Space } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, SafetyOutlined, QqOutlined, WechatOutlined } from '@ant-design/icons';
import { encryptPasswordForTransmission } from '../../../utils/crypto';

interface UserRegisterModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: (userData: any) => void;
}

const UserRegisterModal: React.FC<UserRegisterModalProps> = ({ visible, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('username');
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // 倒计时效果
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  // 发送验证码
  const sendVerifyCode = async () => {
    const registerKey = form.getFieldValue('registerKey');
    if (!registerKey) {
      message.error('请先输入邮箱地址');
      return;
    }

    setSendingCode(true);
    try {
      const response = await fetch('http://localhost:8082/api/user/register/send-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `registerType=${activeTab}&registerKey=${encodeURIComponent(registerKey)}`,
      });

      const result = await response.json();
      console.log('发送验证码响应:', result);

      if (result.success) {
        message.success('验证码发送成功，请查收邮件');
        setCountdown(60);
      } else {
        message.error(result.message || '验证码发送失败');
      }
    } catch (error) {
      console.error('发送验证码失败:', error);
      message.error('网络错误，请稍后重试');
    } finally {
      setSendingCode(false);
    }
  };

  // 检查注册标识是否可用
  const checkRegisterKey = async (registerKey: string) => {
    if (!registerKey) return;

    try {
      const response = await fetch(
        `http://localhost:8082/api/user/register/check?registerType=${activeTab}&registerKey=${encodeURIComponent(registerKey)}`,
        { method: 'GET' }
      );

      const result = await response.json();
      console.log('检查注册标识响应:', result);

      if (!result.success || !result.data) {
        return Promise.reject(new Error('该账号已被注册'));
      }
    } catch (error) {
      console.error('检查注册标识失败:', error);
      return Promise.reject(new Error('检查失败，请稍后重试'));
    }
  };

  // 提交注册
  const handleRegister = async (values: any) => {
    setLoading(true);

    try {
      const requestData: any = {
        registerType: activeTab,
        registerKey: values.registerKey,
        nickname: values.nickname,
      };

      // 根据注册类型添加不同的字段
      if (activeTab === 'username') {
        requestData.password = encryptPasswordForTransmission(values.password);
      } else if (activeTab === 'email') {
        requestData.verifyCode = values.verifyCode;
      }

      console.log('注册请求数据:', requestData);

      const response = await fetch('http://localhost:8082/api/user/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      });

      const result = await response.json();
      console.log('注册响应:', result);

      if (result.success) {
        message.success('注册成功！');
        onSuccess(result.data);
        form.resetFields();
      } else {
        message.error(result.message || '注册失败');
      }
    } catch (error) {
      console.error('注册请求失败:', error);
      message.error('网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleThirdPartyRegister = (type: 'qq' | 'wechat') => {
    message.info(`${type === 'qq' ? 'QQ' : '微信'}注册功能开发中...`);
  };

  // 用户名密码注册表单
  const UsernameRegisterForm = () => (
    <Form
      form={form}
      name="username-register"
      onFinish={handleRegister}
      autoComplete="off"
      layout="vertical"
    >
      <Form.Item
        name="registerKey"
        rules={[
          { required: true, message: '请输入用户名' },
          { min: 4, max: 20, message: '用户名长度必须在4-20个字符之间' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
          { validator: (_, value) => checkRegisterKey(value) },
        ]}
      >
        <Input
          prefix={<UserOutlined />}
          placeholder="请输入用户名（4-20位字母数字下划线）"
          size="large"
        />
      </Form.Item>

      <Form.Item
        name="password"
        rules={[
          { required: true, message: '请输入密码' },
          { min: 6, max: 20, message: '密码长度必须在6-20个字符之间' },
          { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字' },
        ]}
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="请输入密码（6-20位，包含字母和数字）"
          size="large"
        />
      </Form.Item>

      <Form.Item
        name="confirmPassword"
        dependencies={['password']}
        rules={[
          { required: true, message: '请确认密码' },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('password') === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error('两次输入的密码不一致'));
            },
          }),
        ]}
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="请再次输入密码"
          size="large"
        />
      </Form.Item>

      <Form.Item
        name="nickname"
        rules={[
          { required: true, message: '请输入昵称' },
          { max: 50, message: '昵称长度不能超过50个字符' },
        ]}
      >
        <Input
          prefix={<UserOutlined />}
          placeholder="请输入昵称"
          size="large"
        />
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={loading}
          size="large"
          block
        >
          注册
        </Button>
      </Form.Item>
    </Form>
  );

  // 邮箱验证码注册表单
  const EmailRegisterForm = () => (
    <Form
      form={form}
      name="email-register"
      onFinish={handleRegister}
      autoComplete="off"
      layout="vertical"
    >
      <Form.Item
        name="registerKey"
        rules={[
          { required: true, message: '请输入邮箱地址' },
          { type: 'email', message: '请输入有效的邮箱地址' },
          { validator: (_, value) => checkRegisterKey(value) },
        ]}
      >
        <Input
          prefix={<MailOutlined />}
          placeholder="请输入邮箱地址"
          size="large"
        />
      </Form.Item>

      <Form.Item
        name="verifyCode"
        rules={[
          { required: true, message: '请输入验证码' },
          { len: 6, message: '验证码为6位数字' },
        ]}
      >
        <div className="flex gap-2">
          <Input
            prefix={<SafetyOutlined />}
            placeholder="请输入6位验证码"
            size="large"
            maxLength={6}
            className="flex-1"
          />
          <Button
            onClick={sendVerifyCode}
            loading={sendingCode}
            disabled={countdown > 0}
            size="large"
          >
            {countdown > 0 ? `${countdown}s` : '发送验证码'}
          </Button>
        </div>
      </Form.Item>

      <Form.Item
        name="nickname"
        rules={[
          { required: true, message: '请输入昵称' },
          { max: 50, message: '昵称长度不能超过50个字符' },
        ]}
      >
        <Input
          prefix={<UserOutlined />}
          placeholder="请输入昵称"
          size="large"
        />
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={loading}
          size="large"
          block
        >
          注册
        </Button>
      </Form.Item>
    </Form>
  );

  const tabItems = [
    {
      key: 'username',
      label: '用户名注册',
      children: <UsernameRegisterForm />,
    },
    {
      key: 'email',
      label: '邮箱注册',
      children: <EmailRegisterForm />,
    },
  ];

  return (
    <Modal
      title="用户注册"
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={500}
      destroyOnClose
    >
      <div className="py-4">
        <Tabs
          activeKey={activeTab}
          onChange={(key) => {
            setActiveTab(key);
            form.resetFields();
          }}
          items={tabItems}
          centered
        />

        <div className="mt-6 text-center">
          <div className="mb-4">
            <span className="text-gray-500">或使用第三方账号注册</span>
          </div>
          <Space size="large">
            <Button
              icon={<QqOutlined />}
              shape="circle"
              size="large"
              className="text-blue-600 border-blue-600 hover:bg-blue-50"
              onClick={() => handleThirdPartyRegister('qq')}
            />
            <Button
              icon={<WechatOutlined />}
              shape="circle"
              size="large"
              className="text-green-600 border-green-600 hover:bg-green-50"
              onClick={() => handleThirdPartyRegister('wechat')}
            />
          </Space>
        </div>

        <div className="text-center mt-4">
          <span className="text-gray-500">已有账号？</span>
          <a href="#" className="text-blue-600 hover:text-blue-800 ml-1">
            立即登录
          </a>
        </div>
      </div>
    </Modal>
  );
};

export default UserRegisterModal;

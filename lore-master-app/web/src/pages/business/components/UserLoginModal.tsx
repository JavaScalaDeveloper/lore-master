import React, { useState } from 'react';
import { Modal, Form, Input, Button, Checkbox, message, Divider } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { encryptPasswordForTransmission } from '../../../utils/crypto';

interface UserLoginModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: (userData: any) => void;
}

/**
 * 用户登录弹窗组件
 */
const UserLoginModal: React.FC<UserLoginModalProps> = ({
  visible,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);



  // 处理登录提交
  const handleSubmit = async (values: any) => {
    try {
      setLoading(true);

      // 构建请求数据
      const requestData = {
        loginType: 'username', // 暂时只支持用户名登录
        loginKey: values.username,
        password: encryptPasswordForTransmission(values.password),
        rememberMe: values.rememberMe || false,
      };

      console.log('登录请求数据:', requestData);

      // TODO: 实现登录接口调用
      // const response = await fetch('http://localhost:8081/api/user/login', {
      //   method: 'POST',
      //   headers: {
      //     'Content-Type': 'application/json',
      //   },
      //   body: JSON.stringify(requestData),
      // });

      // const result = await response.json();

      // 模拟登录成功响应
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const mockResult = {
        code: 200,
        message: 'success',
        data: {
          userId: '25072610450001',
          nickname: values.username,
          avatarUrl: null,
          gender: 1,
          currentLevel: 1,
          totalScore: 0,
          studyDays: 0,
          status: 1,
          isVerified: 1,
          accessToken: 'mock_access_token_' + Date.now(),
          refreshToken: 'mock_refresh_token_' + Date.now(),
          tokenExpireTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
        }
      };

      console.log('登录响应:', mockResult);

      if (mockResult.code === 200) {
        message.success('登录成功！');
        form.resetFields();
        onSuccess(mockResult.data);
      } else {
        message.error(mockResult.message || '登录失败');
      }
    } catch (error) {
      console.error('登录失败:', error);
      message.error('登录失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 重置表单
  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  return (
    <Modal
      title="用户登录"
      open={visible}
      onCancel={handleCancel}
      footer={null}
      width={400}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        autoComplete="off"
      >
        <Form.Item
          name="username"
          label="用户名"
          rules={[
            { required: true, message: '请输入用户名' },
          ]}
        >
          <Input
            prefix={<UserOutlined />}
            placeholder="请输入用户名"
            size="large"
          />
        </Form.Item>

        <Form.Item
          name="password"
          label="密码"
          rules={[
            { required: true, message: '请输入密码' },
          ]}
        >
          <Input.Password
            prefix={<LockOutlined />}
            placeholder="请输入密码"
            size="large"
          />
        </Form.Item>

        <Form.Item>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Form.Item name="rememberMe" valuePropName="checked" noStyle>
              <Checkbox>记住我</Checkbox>
            </Form.Item>
            <Button type="link" style={{ padding: 0 }}>
              忘记密码？
            </Button>
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

        <Divider plain>其他登录方式</Divider>

        <div style={{ textAlign: 'center' }}>
          <Button type="text" disabled>
            微信登录（开发中）
          </Button>
          <Button type="text" disabled>
            QQ登录（开发中）
          </Button>
        </div>
      </Form>
    </Modal>
  );
};

export default UserLoginModal;

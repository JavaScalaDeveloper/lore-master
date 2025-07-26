import React, { useState } from 'react';
import { Modal, Form, Input, Button, Select, Radio, message, Tabs, Space } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, SafetyOutlined } from '@ant-design/icons';
import { encryptPasswordForTransmission } from '../../../utils/crypto';

const { Option } = Select;
const { TabPane } = Tabs;

interface UserRegisterModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: (userData: any) => void;
}

/**
 * 用户注册弹窗组件
 */
const UserRegisterModal: React.FC<UserRegisterModalProps> = ({
  visible,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('username');
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);



  // 发送验证码
  const handleSendVerifyCode = async () => {
    try {
      const email = form.getFieldValue('registerKey');
      if (!email) {
        message.error('请先输入邮箱地址');
        return;
      }

      setSendingCode(true);
      
      const response = await fetch('http://localhost:8082/api/user/register/send-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          registerType: 'email',
          registerKey: email,
        }),
      });

      const result = await response.json();
      
      if (result.code === 200) {
        message.success('验证码已发送，请查收邮箱');
        
        // 开始倒计时
        setCountdown(60);
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        message.error(result.message || '发送验证码失败');
      }
    } catch (error) {
      console.error('发送验证码失败:', error);
      message.error('发送验证码失败，请稍后重试');
    } finally {
      setSendingCode(false);
    }
  };

  // 处理注册提交
  const handleSubmit = async (values: any) => {
    try {
      setLoading(true);

      // 构建请求数据
      const requestData: any = {
        registerType: activeTab,
        registerKey: values.registerKey,
        nickname: values.nickname,
        gender: values.gender,
      };

      // 根据注册类型添加相应的验证信息
      if (activeTab === 'username') {
        // 用户名注册需要加密密码
        requestData.password = encryptPasswordForTransmission(values.password);
      } else if (activeTab === 'email') {
        // 邮箱注册需要验证码
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

      if (result.code === 200) {
        message.success('注册成功！');
        form.resetFields();
        onSuccess(result.data);
      } else {
        message.error(result.message || '注册失败');
      }
    } catch (error) {
      console.error('注册失败:', error);
      message.error('注册失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 检查注册标识是否可用
  const checkRegisterKeyAvailable = async (registerKey: string) => {
    if (!registerKey || registerKey.length < 2) {
      return Promise.resolve(); // 太短的输入不检查
    }

    try {
      const response = await fetch(
        `http://localhost:8082/api/user/register/check?registerType=${activeTab}&registerKey=${encodeURIComponent(registerKey)}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      console.log('检查可用性响应:', result);

      if (result.code === 200) {
        return result.data ? Promise.resolve() : Promise.reject(new Error('该账号已被注册'));
      } else {
        return Promise.reject(new Error(result.message || '检查失败'));
      }
    } catch (error) {
      console.error('检查注册标识可用性失败:', error);
      return Promise.reject(new Error('检查失败，请稍后重试'));
    }
  };

  // 重置表单
  const handleCancel = () => {
    form.resetFields();
    setActiveTab('username');
    setCountdown(0);
    onCancel();
  };

  return (
    <Modal
      title="用户注册"
      open={visible}
      onCancel={handleCancel}
      footer={null}
      width={480}
      destroyOnClose
    >
      <Tabs activeKey={activeTab} onChange={setActiveTab} centered>
        <TabPane tab="用户名注册" key="username">
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
            autoComplete="off"
          >
            <Form.Item
              name="registerKey"
              label="用户名"
              rules={[
                { required: true, message: '请输入用户名' },
                { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名只能包含字母、数字和下划线，长度4-20位' },
                {
                  validator: async (_, value) => {
                    if (!value) return Promise.resolve();
                    try {
                      await checkRegisterKeyAvailable(value);
                      return Promise.resolve();
                    } catch (error) {
                      return Promise.reject(error);
                    }
                  }
                },
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
                { min: 6, max: 20, message: '密码长度必须在6-20个字符之间' },
                { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请输入密码"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              label="确认密码"
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
              label="昵称"
              rules={[
                { max: 50, message: '昵称长度不能超过50个字符' },
              ]}
            >
              <Input
                placeholder="请输入昵称（可选）"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="gender"
              label="性别"
              initialValue="0"
            >
              <Radio.Group>
                <Radio value="0">保密</Radio>
                <Radio value="1">男</Radio>
                <Radio value="2">女</Radio>
              </Radio.Group>
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
        </TabPane>

        <TabPane tab="邮箱注册" key="email">
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
            autoComplete="off"
          >
            <Form.Item
              name="registerKey"
              label="邮箱地址"
              rules={[
                { required: true, message: '请输入邮箱地址' },
                { type: 'email', message: '请输入有效的邮箱地址' },
                {
                  validator: async (_, value) => {
                    if (!value) return Promise.resolve();
                    try {
                      await checkRegisterKeyAvailable(value);
                      return Promise.resolve();
                    } catch (error) {
                      return Promise.reject(error);
                    }
                  }
                },
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
              label="验证码"
              rules={[
                { required: true, message: '请输入验证码' },
                { len: 6, message: '验证码长度为6位' },
              ]}
            >
              <Space.Compact style={{ width: '100%' }}>
                <Input
                  prefix={<SafetyOutlined />}
                  placeholder="请输入验证码"
                  size="large"
                />
                <Button
                  size="large"
                  onClick={handleSendVerifyCode}
                  loading={sendingCode}
                  disabled={countdown > 0}
                >
                  {countdown > 0 ? `${countdown}s` : '发送验证码'}
                </Button>
              </Space.Compact>
            </Form.Item>

            <Form.Item
              name="nickname"
              label="昵称"
              rules={[
                { max: 50, message: '昵称长度不能超过50个字符' },
              ]}
            >
              <Input
                placeholder="请输入昵称（可选）"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="gender"
              label="性别"
              initialValue="0"
            >
              <Radio.Group>
                <Radio value="0">保密</Radio>
                <Radio value="1">男</Radio>
                <Radio value="2">女</Radio>
              </Radio.Group>
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
        </TabPane>
      </Tabs>
    </Modal>
  );
};

export default UserRegisterModal;

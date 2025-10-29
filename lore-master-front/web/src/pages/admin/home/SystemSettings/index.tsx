import React, { useState } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Switch,
  Button,
  message,
  Tabs,
  Select,
  Upload,
  Typography,
  Divider,
  Space,
  Row,
  Col,
  Alert
} from 'antd';
import {
  SaveOutlined,
  UploadOutlined,
  ReloadOutlined,
  SettingOutlined,
  SecurityScanOutlined,
  DatabaseOutlined,
  BellOutlined
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;
const { TabPane } = Tabs;

const SystemSettings: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [basicForm] = Form.useForm();
  const [securityForm] = Form.useForm();
  const [notificationForm] = Form.useForm();
  const [learningForm] = Form.useForm();

  // 模拟当前配置
  const currentConfig = {
    basic: {
      systemName: '通学万卷',
      systemLogo: '',
      systemDescription: '基于 Langchain4j 的智能教育学习能力提升平台',
      contactEmail: 'admin@loremaster.com',
      contactPhone: '400-123-4567',
      icp: '京ICP备12345678号',
      maxUploadSize: 10,
      sessionTimeout: 30
    },
    security: {
      passwordMinLength: 6,
      passwordComplexity: true,
      loginAttempts: 5,
      lockoutDuration: 30,
      enableTwoFactor: false,
      ipWhitelist: '',
      enableAuditLog: true
    },
    notification: {
      emailEnabled: true,
      smsEnabled: false,
      pushEnabled: true,
      emailServer: 'smtp.example.com',
      emailPort: 587,
      emailUsername: '',
      emailPassword: '',
      smsProvider: 'aliyun'
    },
    learning: {
      defaultDifficulty: 3,
      passScore: 60,
      maxRetries: 3,
      studyTimeLimit: 120,
      enableAI: true,
      aiModel: 'gpt-3.5-turbo',
      enableRecommendation: true,
      recommendationAlgorithm: 'collaborative'
    }
  };

  const handleSave = async (formType: string, values: any) => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success(`${formType}设置保存成功`);
    } catch (error) {
      message.error('保存失败');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    basicForm.resetFields();
    securityForm.resetFields();
    notificationForm.resetFields();
    learningForm.resetFields();
    message.info('已重置为默认配置');
  };

  const handleTestConnection = async (type: string) => {
    try {
      message.loading('正在测试连接...', 2);
      // 模拟测试
      await new Promise(resolve => setTimeout(resolve, 2000));
      message.success(`${type}连接测试成功`);
    } catch (error) {
      message.error(`${type}连接测试失败`);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>系统设置</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置配置
          </Button>
          <Button type="primary" icon={<SaveOutlined />} loading={loading}>
            保存所有设置
          </Button>
        </Space>
      </div>

      <Tabs defaultActiveKey="basic" type="card">
        {/* 基础设置 */}
        <TabPane tab={<span><SettingOutlined />基础设置</span>} key="basic">
          <Card>
            <Form
              form={basicForm}
              layout="vertical"
              initialValues={currentConfig.basic}
              onFinish={(values) => handleSave('基础', values)}
            >
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="systemName"
                    label="系统名称"
                    rules={[{ required: true, message: '请输入系统名称' }]}
                  >
                    <Input placeholder="请输入系统名称" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="systemLogo"
                    label="系统Logo"
                  >
                    <Upload>
                      <Button icon={<UploadOutlined />}>上传Logo</Button>
                    </Upload>
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="systemDescription"
                label="系统描述"
              >
                <TextArea rows={3} placeholder="请输入系统描述" />
              </Form.Item>

              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="contactEmail"
                    label="联系邮箱"
                    rules={[{ type: 'email', message: '请输入有效的邮箱地址' }]}
                  >
                    <Input placeholder="请输入联系邮箱" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="contactPhone"
                    label="联系电话"
                  >
                    <Input placeholder="请输入联系电话" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="icp"
                    label="ICP备案号"
                  >
                    <Input placeholder="请输入ICP备案号" />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name="maxUploadSize"
                    label="最大上传大小(MB)"
                  >
                    <InputNumber min={1} max={100} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name="sessionTimeout"
                    label="会话超时(分钟)"
                  >
                    <InputNumber min={5} max={480} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading}>
                  保存基础设置
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </TabPane>

        {/* 安全设置 */}
        <TabPane tab={<span><SecurityScanOutlined />安全设置</span>} key="security">
          <Card>
            <Alert
              message="安全提醒"
              description="修改安全设置可能影响用户登录，请谨慎操作。"
              type="warning"
              showIcon
              style={{ marginBottom: 24 }}
            />
            
            <Form
              form={securityForm}
              layout="vertical"
              initialValues={currentConfig.security}
              onFinish={(values) => handleSave('安全', values)}
            >
              <Row gutter={24}>
                <Col span={8}>
                  <Form.Item
                    name="passwordMinLength"
                    label="密码最小长度"
                  >
                    <InputNumber min={6} max={20} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="loginAttempts"
                    label="最大登录尝试次数"
                  >
                    <InputNumber min={3} max={10} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="lockoutDuration"
                    label="锁定时长(分钟)"
                  >
                    <InputNumber min={5} max={1440} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={24}>
                <Col span={8}>
                  <Form.Item
                    name="passwordComplexity"
                    label="启用密码复杂度"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="enableTwoFactor"
                    label="启用双因子认证"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="enableAuditLog"
                    label="启用审计日志"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="ipWhitelist"
                label="IP白名单"
                help="多个IP用换行分隔，留空表示不限制"
              >
                <TextArea rows={4} placeholder="请输入允许访问的IP地址，每行一个" />
              </Form.Item>

              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading}>
                  保存安全设置
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </TabPane>

        {/* 通知设置 */}
        <TabPane tab={<span><BellOutlined />通知设置</span>} key="notification">
          <Card>
            <Form
              form={notificationForm}
              layout="vertical"
              initialValues={currentConfig.notification}
              onFinish={(values) => handleSave('通知', values)}
            >
              <Title level={4}>通知方式</Title>
              <Row gutter={24}>
                <Col span={8}>
                  <Form.Item
                    name="emailEnabled"
                    label="邮件通知"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="smsEnabled"
                    label="短信通知"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="pushEnabled"
                    label="推送通知"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
              </Row>

              <Divider />

              <Title level={4}>邮件服务器配置</Title>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="emailServer"
                    label="SMTP服务器"
                  >
                    <Input placeholder="请输入SMTP服务器地址" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="emailPort"
                    label="端口"
                  >
                    <InputNumber min={1} max={65535} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="emailUsername"
                    label="用户名"
                  >
                    <Input placeholder="请输入邮箱用户名" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="emailPassword"
                    label="密码"
                  >
                    <Input.Password placeholder="请输入邮箱密码" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit" loading={loading}>
                    保存通知设置
                  </Button>
                  <Button onClick={() => handleTestConnection('邮件服务器')}>
                    测试邮件连接
                  </Button>
                </Space>
              </Form.Item>

              <Divider />

              <Title level={4}>短信服务配置</Title>
              <Form.Item
                name="smsProvider"
                label="短信服务商"
              >
                <Select placeholder="请选择短信服务商">
                  <Select.Option value="aliyun">阿里云</Select.Option>
                  <Select.Option value="tencent">腾讯云</Select.Option>
                  <Select.Option value="huawei">华为云</Select.Option>
                </Select>
              </Form.Item>
            </Form>
          </Card>
        </TabPane>

        {/* 学习设置 */}
        <TabPane tab={<span><DatabaseOutlined />学习设置</span>} key="learning">
          <Card>
            <Form
              form={learningForm}
              layout="vertical"
              initialValues={currentConfig.learning}
              onFinish={(values) => handleSave('学习', values)}
            >
              <Title level={4}>基础配置</Title>
              <Row gutter={24}>
                <Col span={6}>
                  <Form.Item
                    name="defaultDifficulty"
                    label="默认难度"
                  >
                    <Select>
                      {[1, 2, 3, 4, 5].map(level => (
                        <Select.Option key={level} value={level}>
                          {'★'.repeat(level)}{'☆'.repeat(5 - level)}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name="passScore"
                    label="及格分数"
                  >
                    <InputNumber min={0} max={100} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name="maxRetries"
                    label="最大重试次数"
                  >
                    <InputNumber min={1} max={10} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name="studyTimeLimit"
                    label="学习时长限制(分钟)"
                  >
                    <InputNumber min={30} max={480} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              </Row>

              <Divider />

              <Title level={4}>AI功能配置</Title>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="enableAI"
                    label="启用AI功能"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="aiModel"
                    label="AI模型"
                  >
                    <Select placeholder="请选择AI模型">
                      <Select.Option value="gpt-3.5-turbo">GPT-3.5 Turbo</Select.Option>
                      <Select.Option value="gpt-4">GPT-4</Select.Option>
                      <Select.Option value="claude-3">Claude-3</Select.Option>
                      <Select.Option value="local-model">本地模型</Select.Option>
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Divider />

              <Title level={4}>推荐系统配置</Title>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="enableRecommendation"
                    label="启用智能推荐"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="recommendationAlgorithm"
                    label="推荐算法"
                  >
                    <Select placeholder="请选择推荐算法">
                      <Select.Option value="collaborative">协同过滤</Select.Option>
                      <Select.Option value="content">基于内容</Select.Option>
                      <Select.Option value="hybrid">混合推荐</Select.Option>
                      <Select.Option value="deep-learning">深度学习</Select.Option>
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit" loading={loading}>
                    保存学习设置
                  </Button>
                  <Button onClick={() => handleTestConnection('AI服务')}>
                    测试AI连接
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default SystemSettings;

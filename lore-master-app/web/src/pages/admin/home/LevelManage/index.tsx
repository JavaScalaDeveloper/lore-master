import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  message,
  Popconfirm,
  Tag,
  Card,
  Row,
  Col,
  Progress,
  Typography,
  Descriptions,
  Badge
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TrophyOutlined,
  StarOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface LevelConfig {
  id: string;
  level: string;
  name: string;
  subject: string;
  description: string;
  minScore: number;
  maxScore: number;
  requiredQuestions: number;
  passRate: number;
  unlockConditions: string;
  rewards: string;
  color: string;
  icon: string;
  userCount: number;
  avgScore: number;
  createTime: string;
}

const LevelManage: React.FC = () => {
  const [levels, setLevels] = useState<LevelConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingLevel, setEditingLevel] = useState<LevelConfig | null>(null);
  const [form] = Form.useForm();
  const [selectedSubject, setSelectedSubject] = useState<string>('all');

  // 模拟数据
  const mockLevels: LevelConfig[] = [
    {
      id: '1',
      level: 'L1',
      name: '初学者',
      subject: 'Java编程',
      description: 'Java编程入门级别，掌握基本语法和概念',
      minScore: 0,
      maxScore: 59,
      requiredQuestions: 10,
      passRate: 60,
      unlockConditions: '无前置条件',
      rewards: '解锁L2等级',
      color: '#52c41a',
      icon: '🌱',
      userCount: 156,
      avgScore: 45,
      createTime: '2024-01-15'
    },
    {
      id: '2',
      level: 'L2',
      name: '基础掌握',
      subject: 'Java编程',
      description: '掌握Java基础语法，能编写简单程序',
      minScore: 60,
      maxScore: 69,
      requiredQuestions: 15,
      passRate: 65,
      unlockConditions: '完成L1等级',
      rewards: '解锁L3等级 + 基础徽章',
      color: '#1890ff',
      icon: '📚',
      userCount: 234,
      avgScore: 64,
      createTime: '2024-01-15'
    },
    {
      id: '3',
      level: 'L3',
      name: '进阶学习',
      subject: 'Java编程',
      description: '掌握面向对象编程，理解类和对象概念',
      minScore: 70,
      maxScore: 79,
      requiredQuestions: 20,
      passRate: 70,
      unlockConditions: '完成L2等级',
      rewards: '解锁L4等级 + 进阶徽章',
      color: '#faad14',
      icon: '🎯',
      userCount: 198,
      avgScore: 74,
      createTime: '2024-01-15'
    },
    {
      id: '4',
      level: 'L4',
      name: '熟练应用',
      subject: 'Java编程',
      description: '熟练使用Java API，能开发小型应用',
      minScore: 80,
      maxScore: 84,
      requiredQuestions: 25,
      passRate: 75,
      unlockConditions: '完成L3等级',
      rewards: '解锁L5等级 + 熟练徽章',
      color: '#f5222d',
      icon: '🚀',
      userCount: 143,
      avgScore: 82,
      createTime: '2024-01-15'
    },
    {
      id: '5',
      level: 'L5',
      name: '高级开发',
      subject: 'Java编程',
      description: '掌握高级特性，能进行复杂项目开发',
      minScore: 85,
      maxScore: 89,
      requiredQuestions: 30,
      passRate: 80,
      unlockConditions: '完成L4等级',
      rewards: '解锁L6等级 + 高级徽章',
      color: '#722ed1',
      icon: '👑',
      userCount: 89,
      avgScore: 87,
      createTime: '2024-01-15'
    }
  ];

  useEffect(() => {
    fetchLevels();
  }, [selectedSubject]);

  const fetchLevels = async () => {
    setLoading(true);
    try {
      setTimeout(() => {
        let filteredData = mockLevels;
        if (selectedSubject !== 'all') {
          filteredData = filteredData.filter(level => level.subject === selectedSubject);
        }
        setLevels(filteredData);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('获取等级配置失败');
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingLevel(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: LevelConfig) => {
    setEditingLevel(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      message.success('删除成功');
      fetchLevels();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingLevel) {
        message.success('更新成功');
      } else {
        message.success('添加成功');
      }
      setModalVisible(false);
      fetchLevels();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns = [
    {
      title: '等级',
      dataIndex: 'level',
      key: 'level',
      render: (level: string, record: LevelConfig) => (
        <Space>
          <span style={{ fontSize: '18px' }}>{record.icon}</span>
          <Tag color={record.color} style={{ fontWeight: 'bold' }}>
            {level}
          </Tag>
        </Space>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: '学科',
      dataIndex: 'subject',
      key: 'subject',
      render: (subject: string) => <Tag>{subject}</Tag>,
    },
    {
      title: '分数范围',
      key: 'scoreRange',
      render: (_, record: LevelConfig) => (
        <span>{record.minScore} - {record.maxScore}分</span>
      ),
    },
    {
      title: '通过率要求',
      dataIndex: 'passRate',
      key: 'passRate',
      render: (rate: number) => (
        <Progress
          percent={rate}
          size="small"
          format={(percent) => `${percent}%`}
          strokeColor={rate >= 80 ? '#52c41a' : rate >= 60 ? '#faad14' : '#f5222d'}
        />
      ),
    },
    {
      title: '题目数量',
      dataIndex: 'requiredQuestions',
      key: 'requiredQuestions',
      align: 'center' as const,
    },
    {
      title: '用户数量',
      dataIndex: 'userCount',
      key: 'userCount',
      align: 'center' as const,
      render: (count: number) => (
        <Badge count={count} showZero color="#1890ff" />
      ),
    },
    {
      title: '平均分',
      dataIndex: 'avgScore',
      key: 'avgScore',
      align: 'center' as const,
      render: (score: number) => (
        <span style={{ 
          color: score >= 80 ? '#52c41a' : score >= 60 ? '#faad14' : '#f5222d',
          fontWeight: 'bold'
        }}>
          {score}分
        </span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: LevelConfig) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个等级配置吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>等级管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加等级
        </Button>
      </div>

      {/* 筛选条件 */}
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <span>学科筛选：</span>
          <Select
            value={selectedSubject}
            onChange={setSelectedSubject}
            style={{ width: 200 }}
          >
            <Select.Option value="all">全部学科</Select.Option>
            <Select.Option value="Java编程">Java编程</Select.Option>
            <Select.Option value="英语学习">英语学习</Select.Option>
            <Select.Option value="数学基础">数学基础</Select.Option>
          </Select>
        </Space>
      </Card>

      {/* 等级概览 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        {levels.slice(0, 5).map((level) => (
          <Col key={level.id} span={4.8}>
            <Card 
              size="small" 
              style={{ 
                textAlign: 'center',
                borderColor: level.color,
                borderWidth: 2
              }}
            >
              <div style={{ fontSize: '24px', marginBottom: 8 }}>
                {level.icon}
              </div>
              <Tag color={level.color} style={{ marginBottom: 8 }}>
                {level.level}
              </Tag>
              <div style={{ fontSize: '12px', color: '#666' }}>
                {level.name}
              </div>
              <div style={{ fontSize: '14px', fontWeight: 'bold', marginTop: 4 }}>
                {level.userCount}人
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 等级配置表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={levels}
          rowKey="id"
          loading={loading}
          pagination={{
            total: levels.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingLevel ? '编辑等级配置' : '添加等级配置'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="level"
                label="等级"
                rules={[{ required: true, message: '请输入等级' }]}
              >
                <Select placeholder="请选择等级">
                  {Array.from({ length: 9 }, (_, i) => (
                    <Select.Option key={i + 1} value={`L${i + 1}`}>L{i + 1}</Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="name"
                label="等级名称"
                rules={[{ required: true, message: '请输入等级名称' }]}
              >
                <Input placeholder="请输入等级名称" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="subject"
                label="学科"
                rules={[{ required: true, message: '请选择学科' }]}
              >
                <Select placeholder="请选择学科">
                  <Select.Option value="Java编程">Java编程</Select.Option>
                  <Select.Option value="英语学习">英语学习</Select.Option>
                  <Select.Option value="数学基础">数学基础</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="等级描述"
            rules={[{ required: true, message: '请输入等级描述' }]}
          >
            <TextArea rows={3} placeholder="请输入等级描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="minScore"
                label="最低分数"
                rules={[{ required: true, message: '请输入最低分数' }]}
              >
                <InputNumber min={0} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="maxScore"
                label="最高分数"
                rules={[{ required: true, message: '请输入最高分数' }]}
              >
                <InputNumber min={0} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="passRate"
                label="通过率要求(%)"
                rules={[{ required: true, message: '请输入通过率要求' }]}
              >
                <InputNumber min={0} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="requiredQuestions"
                label="题目数量"
                rules={[{ required: true, message: '请输入题目数量' }]}
              >
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="color"
                label="等级颜色"
                rules={[{ required: true, message: '请选择等级颜色' }]}
              >
                <Select placeholder="请选择颜色">
                  <Select.Option value="#52c41a">绿色</Select.Option>
                  <Select.Option value="#1890ff">蓝色</Select.Option>
                  <Select.Option value="#faad14">橙色</Select.Option>
                  <Select.Option value="#f5222d">红色</Select.Option>
                  <Select.Option value="#722ed1">紫色</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="icon"
                label="等级图标"
                rules={[{ required: true, message: '请输入等级图标' }]}
              >
                <Input placeholder="请输入emoji图标" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="unlockConditions"
            label="解锁条件"
            rules={[{ required: true, message: '请输入解锁条件' }]}
          >
            <Input placeholder="请输入解锁条件" />
          </Form.Item>

          <Form.Item
            name="rewards"
            label="奖励内容"
            rules={[{ required: true, message: '请输入奖励内容' }]}
          >
            <Input placeholder="请输入奖励内容" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default LevelManage;

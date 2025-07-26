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
  Switch,
  message,
  Popconfirm,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  Typography,
  Progress,
  Tooltip
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  RobotOutlined,
  BranchesOutlined,
  UserOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface CareerTarget {
  id: string;
  name: string;
  code: string;
  description: string;
  category: string;
  difficultyLevel: number;
  estimatedMonths: number;
  status: boolean;
  learningPathCount: number;
  knowledgePointCount: number;
  enrolledUsers: number;
  completionRate: number;
  aiGenerated: boolean;
  createTime: string;
}

const CareerTargetManage: React.FC = () => {
  const [targets, setTargets] = useState<CareerTarget[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [pathModalVisible, setPathModalVisible] = useState(false);
  const [editingTarget, setEditingTarget] = useState<CareerTarget | null>(null);
  const [selectedTarget, setSelectedTarget] = useState<CareerTarget | null>(null);
  const [form] = Form.useForm();
  const [generatingPath, setGeneratingPath] = useState(false);

  // 模拟数据
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const mockTargets: CareerTarget[] = [
    {
      id: '1',
      name: 'Web工程师（业务系统）',
      code: 'WEB_ENGINEER',
      description: 'Web应用开发，包括前端、后端、数据库等全栈技术',
      category: 'Development',
      difficultyLevel: 3,
      estimatedMonths: 12,
      status: true,
      learningPathCount: 8,
      knowledgePointCount: 156,
      enrolledUsers: 1248,
      completionRate: 68.5,
      aiGenerated: true,
      createTime: '2024-01-15'
    },
    {
      id: '2',
      name: '大数据平台开发工程师',
      code: 'BIGDATA_ENGINEER',
      description: '大数据处理、分布式系统、数据仓库等技术',
      category: 'BigData',
      difficultyLevel: 4,
      estimatedMonths: 18,
      status: true,
      learningPathCount: 12,
      knowledgePointCount: 234,
      enrolledUsers: 892,
      completionRate: 45.2,
      aiGenerated: true,
      createTime: '2024-01-10'
    },
    {
      id: '3',
      name: '中间件开发工程师',
      code: 'MIDDLEWARE_ENGINEER',
      description: '中间件原理、源码分析、系统架构设计',
      category: 'Infrastructure',
      difficultyLevel: 5,
      estimatedMonths: 24,
      status: true,
      learningPathCount: 15,
      knowledgePointCount: 312,
      enrolledUsers: 567,
      completionRate: 32.8,
      aiGenerated: true,
      createTime: '2024-01-08'
    }
  ];

  useEffect(() => {
    fetchTargets();
  }, []);

  const fetchTargets = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('adminToken');
      const tokenType = localStorage.getItem('tokenType') || 'Bearer';

      const response = await fetch('/api/admin/career-targets/page', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `${tokenType} ${token}` : ''
        },
        body: JSON.stringify({
          current: 1,
          size: 100
        })
      });

      const result = await response.json();
      console.log('API响应数据:', result); // 调试日志

      // 检查新的响应格式：{ code: 200, message: "success", data: {...} }
      if (result.code === 200) {
        // Spring Data Page对象结构：{ content: [...], totalElements: ..., ... }
        const pageData = result.data;
        const targetList = pageData.content || pageData.records || pageData || [];
        console.log('解析的目标列表:', targetList); // 调试日志
        setTargets(Array.isArray(targetList) ? targetList : []);
      } else {
        message.error(result.message || '获取职业目标列表失败');
        setTargets([]); // 确保设置为空数组
      }
    } catch (error) {
      console.error('获取职业目标列表错误:', error);
      message.error('获取职业目标列表失败');
      setTargets([]); // 确保在错误时设置为空数组
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingTarget(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: CareerTarget) => {
    setEditingTarget(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      const response = await fetch(`/api/admin/career-targets/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
        }
      });

      const result = await response.json();
      if (result.success) {
        message.success('删除成功');
        fetchTargets();
      } else {
        message.error(result.message || '删除失败');
      }
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const url = editingTarget
        ? `/api/admin/career-targets/${editingTarget.id}`
        : '/api/admin/career-targets';

      const method = editingTarget ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
        },
        body: JSON.stringify(values)
      });

      const result = await response.json();
      if (result.success) {
        message.success(editingTarget ? '更新成功' : '添加成功');
        setModalVisible(false);
        fetchTargets();
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleGenerateLearningPath = async (target: CareerTarget) => {
    setSelectedTarget(target);
    setGeneratingPath(true);
    
    try {
      // 模拟AI生成学习路径
      await new Promise(resolve => setTimeout(resolve, 3000));
      message.success(`已为"${target.name}"生成AI学习路径`);
      setPathModalVisible(true);
    } catch (error) {
      message.error('生成学习路径失败');
    } finally {
      setGeneratingPath(false);
    }
  };

  const getDifficultyColor = (level: number) => {
    const colors = ['#52c41a', '#1890ff', '#faad14', '#f5222d', '#722ed1'];
    return colors[level - 1] || '#666';
  };

  const getDifficultyText = (level: number) => {
    const texts = ['入门', '初级', '中级', '高级', '专家'];
    return texts[level - 1] || '未知';
  };

  const columns = [
    {
      title: '职业目标',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: CareerTarget) => (
        <Space direction="vertical" size="small">
          <Space>
            <Text strong>{text}</Text>
            {record.aiGenerated && (
              <Tooltip title="AI生成的学习路径">
                <Tag color="blue" icon={<RobotOutlined />}>AI</Tag>
              </Tooltip>
            )}
          </Space>
          <Tag color="default">{record.code}</Tag>
        </Space>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag color="geekblue">{category}</Tag>,
    },
    {
      title: '难度等级',
      dataIndex: 'difficultyLevel',
      key: 'difficultyLevel',
      render: (level: number) => (
        <Space>
          <Tag color={getDifficultyColor(level)}>
            {'★'.repeat(level)}{'☆'.repeat(5 - level)}
          </Tag>
          <Text type="secondary">{getDifficultyText(level)}</Text>
        </Space>
      ),
    },
    {
      title: '预估时长',
      dataIndex: 'estimatedMonths',
      key: 'estimatedMonths',
      render: (months: number) => `${months}个月`,
      align: 'center' as const,
    },
    {
      title: '学习路径',
      dataIndex: 'learningPathCount',
      key: 'learningPathCount',
      align: 'center' as const,
    },
    {
      title: '知识点数',
      dataIndex: 'knowledgePointCount',
      key: 'knowledgePointCount',
      align: 'center' as const,
    },
    {
      title: '学习人数',
      dataIndex: 'enrolledUsers',
      key: 'enrolledUsers',
      align: 'center' as const,
      render: (count: number) => (
        <Space>
          <UserOutlined />
          <Text>{count}</Text>
        </Space>
      ),
    },
    {
      title: '完成率',
      dataIndex: 'completionRate',
      key: 'completionRate',
      render: (rate: number) => (
        <Progress
          percent={rate}
          size="small"
          format={(percent) => `${percent}%`}
          strokeColor={rate >= 60 ? '#52c41a' : rate >= 40 ? '#faad14' : '#f5222d'}
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: boolean) => (
        <Tag color={status ? 'success' : 'default'}>
          {status ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: CareerTarget) => (
        <Space size="middle">
          <Tooltip title="查看学习路径">
            <Button
              type="link"
              icon={<BranchesOutlined />}
              onClick={() => {/* 查看学习路径 */}}
            >
              路径
            </Button>
          </Tooltip>
          <Tooltip title="AI生成学习路径">
            <Button
              type="link"
              icon={<RobotOutlined />}
              loading={generatingPath && selectedTarget?.id === record.id}
              onClick={() => handleGenerateLearningPath(record)}
            >
              AI生成
            </Button>
          </Tooltip>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个职业目标吗？"
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

  // 统计数据 - 添加安全检查
  const safeTargets = Array.isArray(targets) ? targets : [];
  const totalTargets = safeTargets.length;
  const activeTargets = safeTargets.filter(t => t && t.status).length;
  const totalUsers = safeTargets.reduce((sum, t) => sum + (t?.enrolledUsers || 0), 0);
  const avgCompletionRate = totalTargets > 0
    ? safeTargets.reduce((sum, t) => sum + (t?.completionRate || 0), 0) / totalTargets
    : 0;

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>职业目标管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加职业目标
        </Button>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="职业目标总数" value={totalTargets} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="启用目标" value={activeTargets} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="学习总人数" value={totalUsers} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="平均完成率" 
              value={avgCompletionRate} 
              precision={1}
              suffix="%" 
            />
          </Card>
        </Col>
      </Row>

      {/* 职业目标列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={targets}
          rowKey="id"
          loading={loading}
          pagination={{
            total: targets.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingTarget ? '编辑职业目标' : '添加职业目标'}
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
            <Col span={12}>
              <Form.Item
                name="name"
                label="职业目标名称"
                rules={[{ required: true, message: '请输入职业目标名称' }]}
              >
                <Input placeholder="请输入职业目标名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="code"
                label="职业代码"
                rules={[{ required: true, message: '请输入职业代码' }]}
              >
                <Input placeholder="请输入职业代码" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="职业描述"
            rules={[{ required: true, message: '请输入职业描述' }]}
          >
            <TextArea rows={4} placeholder="请输入职业描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="category"
                label="职业分类"
                rules={[{ required: true, message: '请选择职业分类' }]}
              >
                <Select placeholder="请选择职业分类">
                  <Select.Option value="Development">开发类</Select.Option>
                  <Select.Option value="BigData">大数据类</Select.Option>
                  <Select.Option value="Infrastructure">基础设施类</Select.Option>
                  <Select.Option value="AI">人工智能类</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="difficultyLevel"
                label="难度等级"
                rules={[{ required: true, message: '请选择难度等级' }]}
              >
                <Select placeholder="请选择难度等级">
                  {[1, 2, 3, 4, 5].map(level => (
                    <Select.Option key={level} value={level}>
                      {'★'.repeat(level)}{'☆'.repeat(5 - level)} {getDifficultyText(level)}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="estimatedMonths"
                label="预估学习月数"
                rules={[{ required: true, message: '请输入预估学习月数' }]}
              >
                <InputNumber min={1} max={60} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="status"
            label="状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* AI生成学习路径结果模态框 */}
      <Modal
        title={`${selectedTarget?.name} - AI生成的学习路径`}
        open={pathModalVisible}
        onCancel={() => setPathModalVisible(false)}
        width={1000}
        footer={[
          <Button key="close" onClick={() => setPathModalVisible(false)}>
            关闭
          </Button>,
          <Button key="save" type="primary">
            保存学习路径
          </Button>
        ]}
      >
        <div style={{ maxHeight: 600, overflowY: 'auto' }}>
          <Text type="secondary">AI已为该职业目标生成完整的学习路径，包含基础到专家的所有阶段...</Text>
          {/* 这里可以展示AI生成的学习路径树形结构 */}
        </div>
      </Modal>
    </div>
  );
};

export default CareerTargetManage;

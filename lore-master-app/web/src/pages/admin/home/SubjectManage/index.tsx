import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  message,
  Popconfirm,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  Typography
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  BookOutlined
} from '@ant-design/icons';

const { Title } = Typography;
const { TextArea } = Input;

interface Subject {
  id: string;
  name: string;
  code: string;
  description: string;
  category: string;
  status: boolean;
  totalLevels: number;
  totalQuestions: number;
  totalUsers: number;
  createTime: string;
}

const SubjectManage: React.FC = () => {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingSubject, setEditingSubject] = useState<Subject | null>(null);
  const [form] = Form.useForm();

  // 模拟数据
  const mockSubjects: Subject[] = [
    {
      id: '1',
      name: 'Java编程',
      code: 'JAVA',
      description: 'Java基础语法、面向对象编程、常用API等',
      category: '编程语言',
      status: true,
      totalLevels: 9,
      totalQuestions: 456,
      totalUsers: 234,
      createTime: '2024-01-15'
    },
    {
      id: '2',
      name: '英语学习',
      code: 'ENGLISH',
      description: '单词记忆、语法练习、听力口语等',
      category: '语言学习',
      status: true,
      totalLevels: 9,
      totalQuestions: 678,
      totalUsers: 567,
      createTime: '2024-01-10'
    },
    {
      id: '3',
      name: '数学基础',
      code: 'MATH',
      description: '从小学到高等数学，涵盖各类知识点',
      category: '基础学科',
      status: true,
      totalLevels: 9,
      totalQuestions: 890,
      totalUsers: 445,
      createTime: '2024-01-08'
    },
    {
      id: '4',
      name: 'Python编程',
      code: 'PYTHON',
      description: 'Python基础语法、数据分析、机器学习等',
      category: '编程语言',
      status: false,
      totalLevels: 7,
      totalQuestions: 234,
      totalUsers: 123,
      createTime: '2024-01-20'
    }
  ];

  useEffect(() => {
    fetchSubjects();
  }, []);

  const fetchSubjects = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      setTimeout(() => {
        setSubjects(mockSubjects);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('获取学科列表失败');
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingSubject(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Subject) => {
    setEditingSubject(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      // 模拟API调用
      message.success('删除成功');
      fetchSubjects();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingSubject) {
        // 更新
        message.success('更新成功');
      } else {
        // 新增
        message.success('添加成功');
      }
      setModalVisible(false);
      fetchSubjects();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleStatusChange = async (id: string, status: boolean) => {
    try {
      // 模拟API调用
      message.success(`${status ? '启用' : '禁用'}成功`);
      fetchSubjects();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const columns = [
    {
      title: '学科名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: Subject) => (
        <Space>
          <BookOutlined />
          <span>{text}</span>
          <Tag color="blue">{record.code}</Tag>
        </Space>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag>{category}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '等级数',
      dataIndex: 'totalLevels',
      key: 'totalLevels',
      align: 'center' as const,
    },
    {
      title: '题目数',
      dataIndex: 'totalQuestions',
      key: 'totalQuestions',
      align: 'center' as const,
    },
    {
      title: '学习人数',
      dataIndex: 'totalUsers',
      key: 'totalUsers',
      align: 'center' as const,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: boolean, record: Subject) => (
        <Switch
          checked={status}
          onChange={(checked) => handleStatusChange(record.id, checked)}
          checkedChildren="启用"
          unCheckedChildren="禁用"
        />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Subject) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {/* 查看详情 */}}
          >
            详情
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个学科吗？"
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

  // 统计数据
  const totalSubjects = subjects.length;
  const activeSubjects = subjects.filter(s => s.status).length;
  const totalQuestions = subjects.reduce((sum, s) => sum + s.totalQuestions, 0);
  const totalUsers = subjects.reduce((sum, s) => sum + s.totalUsers, 0);

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>学科管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加学科
        </Button>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="总学科数" value={totalSubjects} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="启用学科" value={activeSubjects} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="总题目数" value={totalQuestions} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="学习人数" value={totalUsers} />
          </Card>
        </Col>
      </Row>

      {/* 学科列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={subjects}
          rowKey="id"
          loading={loading}
          pagination={{
            total: subjects.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingSubject ? '编辑学科' : '添加学科'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
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
                label="学科名称"
                rules={[{ required: true, message: '请输入学科名称' }]}
              >
                <Input placeholder="请输入学科名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="code"
                label="学科代码"
                rules={[{ required: true, message: '请输入学科代码' }]}
              >
                <Input placeholder="请输入学科代码" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="category"
            label="学科分类"
            rules={[{ required: true, message: '请选择学科分类' }]}
          >
            <Select placeholder="请选择学科分类">
              <Select.Option value="编程语言">编程语言</Select.Option>
              <Select.Option value="语言学习">语言学习</Select.Option>
              <Select.Option value="基础学科">基础学科</Select.Option>
              <Select.Option value="专业技能">专业技能</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="description"
            label="学科描述"
            rules={[{ required: true, message: '请输入学科描述' }]}
          >
            <TextArea rows={4} placeholder="请输入学科描述" />
          </Form.Item>
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
    </div>
  );
};

export default SubjectManage;

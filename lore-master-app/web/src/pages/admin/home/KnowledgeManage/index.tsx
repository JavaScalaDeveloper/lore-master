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
  Upload,
  Tree,
  Tabs,
  Typography,
  Divider
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  FolderOutlined,
  FileTextOutlined,
  DownloadOutlined
} from '@ant-design/icons';

const { Title, Paragraph } = Typography;
const { TextArea } = Input;
const { TabPane } = Tabs;

interface KnowledgeItem {
  id: string;
  title: string;
  subject: string;
  level: string;
  type: 'text' | 'video' | 'audio' | 'document';
  content: string;
  tags: string[];
  status: boolean;
  difficulty: number;
  createTime: string;
  updateTime: string;
}

const KnowledgeManage: React.FC = () => {
  const [knowledgeList, setKnowledgeList] = useState<KnowledgeItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<KnowledgeItem | null>(null);
  const [form] = Form.useForm();
  const [selectedSubject, setSelectedSubject] = useState<string>('all');
  const [selectedLevel, setSelectedLevel] = useState<string>('all');

  // 模拟数据
  const mockKnowledgeList: KnowledgeItem[] = [
    {
      id: '1',
      title: 'Java基础语法 - 变量与数据类型',
      subject: 'Java编程',
      level: 'L1',
      type: 'text',
      content: 'Java中的基本数据类型包括byte、short、int、long、float、double、boolean、char...',
      tags: ['基础语法', '数据类型', '变量'],
      status: true,
      difficulty: 1,
      createTime: '2024-01-15',
      updateTime: '2024-01-20'
    },
    {
      id: '2',
      title: '面向对象编程 - 类与对象',
      subject: 'Java编程',
      level: 'L3',
      type: 'video',
      content: '面向对象编程的核心概念，类的定义、对象的创建和使用...',
      tags: ['面向对象', '类', '对象'],
      status: true,
      difficulty: 3,
      createTime: '2024-01-16',
      updateTime: '2024-01-21'
    },
    {
      id: '3',
      title: '英语语法 - 时态详解',
      subject: '英语学习',
      level: 'L2',
      type: 'document',
      content: '英语中的12种时态详细讲解，包括一般现在时、现在进行时...',
      tags: ['语法', '时态', '基础'],
      status: true,
      difficulty: 2,
      createTime: '2024-01-17',
      updateTime: '2024-01-22'
    },
    {
      id: '4',
      title: '数学基础 - 函数与方程',
      subject: '数学基础',
      level: 'L4',
      type: 'text',
      content: '函数的概念、性质和图像，一元二次方程的解法...',
      tags: ['函数', '方程', '代数'],
      status: false,
      difficulty: 4,
      createTime: '2024-01-18',
      updateTime: '2024-01-23'
    }
  ];

  useEffect(() => {
    fetchKnowledgeList();
  }, [selectedSubject, selectedLevel]);

  const fetchKnowledgeList = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/admin/knowledge-points/page', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
        },
        body: JSON.stringify({
          page: 1,
          pageSize: 100,
          careerTargetId: selectedSubject !== 'all' ? selectedSubject : null,
          levelMin: selectedLevel !== 'all' ? parseInt(selectedLevel.replace('L', '')) : null,
          levelMax: selectedLevel !== 'all' ? parseInt(selectedLevel.replace('L', '')) : null
        })
      });

      const result = await response.json();
      if (result.success) {
        setKnowledgeList(result.data.records || []);
      } else {
        message.error(result.message || '获取知识库列表失败');
      }
    } catch (error) {
      message.error('获取知识库列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingItem(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: KnowledgeItem) => {
    setEditingItem(record);
    form.setFieldsValue({
      ...record,
      tags: record.tags.join(',')
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      message.success('删除成功');
      fetchKnowledgeList();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const submitData = {
        ...values,
        tags: values.tags ? values.tags.split(',').map((tag: string) => tag.trim()) : []
      };
      
      if (editingItem) {
        message.success('更新成功');
      } else {
        message.success('添加成功');
      }
      setModalVisible(false);
      fetchKnowledgeList();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleStatusChange = async (id: string, status: boolean) => {
    try {
      message.success(`${status ? '启用' : '禁用'}成功`);
      fetchKnowledgeList();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const getTypeIcon = (type: string) => {
    const iconMap = {
      text: <FileTextOutlined style={{ color: '#1890ff' }} />,
      video: <FileTextOutlined style={{ color: '#f5222d' }} />,
      audio: <FileTextOutlined style={{ color: '#52c41a' }} />,
      document: <FileTextOutlined style={{ color: '#faad14' }} />
    };
    return iconMap[type as keyof typeof iconMap] || <FileTextOutlined />;
  };

  const getTypeTag = (type: string) => {
    const typeMap = {
      text: { color: 'blue', text: '文本' },
      video: { color: 'red', text: '视频' },
      audio: { color: 'green', text: '音频' },
      document: { color: 'orange', text: '文档' }
    };
    const config = typeMap[type as keyof typeof typeMap];
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const getLevelColor = (level: string) => {
    const levelNum = parseInt(level.replace('L', ''));
    if (levelNum <= 3) return 'green';
    if (levelNum <= 6) return 'orange';
    return 'red';
  };

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text: string, record: KnowledgeItem) => (
        <Space>
          {getTypeIcon(record.type)}
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '学科',
      dataIndex: 'subject',
      key: 'subject',
      render: (subject: string) => <Tag>{subject}</Tag>,
    },
    {
      title: '等级',
      dataIndex: 'level',
      key: 'level',
      render: (level: string) => (
        <Tag color={getLevelColor(level)}>{level}</Tag>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => getTypeTag(type),
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      render: (difficulty: number) => (
        <span>{'★'.repeat(difficulty)}{'☆'.repeat(5 - difficulty)}</span>
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      render: (tags: string[]) => (
        <Space wrap>
          {tags.map(tag => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: boolean, record: KnowledgeItem) => (
        <Switch
          checked={status}
          onChange={(checked) => handleStatusChange(record.id, checked)}
          size="small"
        />
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: KnowledgeItem) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<DownloadOutlined />}
            onClick={() => {/* 导出 */}}
          >
            导出
          </Button>
          <Popconfirm
            title="确定要删除这个知识点吗？"
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
        <Title level={2} style={{ margin: 0 }}>知识库管理</Title>
        <Space>
          <Upload>
            <Button icon={<UploadOutlined />}>批量导入</Button>
          </Upload>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加知识点
          </Button>
        </Space>
      </div>

      {/* 筛选条件 */}
      <Card style={{ marginBottom: 16 }}>
        <Space size="large">
          <div>
            <span style={{ marginRight: 8 }}>学科：</span>
            <Select
              value={selectedSubject}
              onChange={setSelectedSubject}
              style={{ width: 150 }}
            >
              <Select.Option value="all">全部学科</Select.Option>
              <Select.Option value="Java编程">Java编程</Select.Option>
              <Select.Option value="英语学习">英语学习</Select.Option>
              <Select.Option value="数学基础">数学基础</Select.Option>
            </Select>
          </div>
          <div>
            <span style={{ marginRight: 8 }}>等级：</span>
            <Select
              value={selectedLevel}
              onChange={setSelectedLevel}
              style={{ width: 120 }}
            >
              <Select.Option value="all">全部等级</Select.Option>
              {Array.from({ length: 9 }, (_, i) => (
                <Select.Option key={i + 1} value={`L${i + 1}`}>L{i + 1}</Select.Option>
              ))}
            </Select>
          </div>
        </Space>
      </Card>

      {/* 知识点列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={knowledgeList}
          rowKey="id"
          loading={loading}
          pagination={{
            total: knowledgeList.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingItem ? '编辑知识点' : '添加知识点'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入知识点标题" />
          </Form.Item>
          
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="subject"
              label="学科"
              rules={[{ required: true, message: '请选择学科' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择学科">
                <Select.Option value="Java编程">Java编程</Select.Option>
                <Select.Option value="英语学习">英语学习</Select.Option>
                <Select.Option value="数学基础">数学基础</Select.Option>
              </Select>
            </Form.Item>
            
            <Form.Item
              name="level"
              label="等级"
              rules={[{ required: true, message: '请选择等级' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择等级">
                {Array.from({ length: 9 }, (_, i) => (
                  <Select.Option key={i + 1} value={`L${i + 1}`}>L{i + 1}</Select.Option>
                ))}
              </Select>
            </Form.Item>
            
            <Form.Item
              name="type"
              label="类型"
              rules={[{ required: true, message: '请选择类型' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择类型">
                <Select.Option value="text">文本</Select.Option>
                <Select.Option value="video">视频</Select.Option>
                <Select.Option value="audio">音频</Select.Option>
                <Select.Option value="document">文档</Select.Option>
              </Select>
            </Form.Item>
            
            <Form.Item
              name="difficulty"
              label="难度"
              rules={[{ required: true, message: '请选择难度' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择难度">
                {[1, 2, 3, 4, 5].map(level => (
                  <Select.Option key={level} value={level}>
                    {'★'.repeat(level)}{'☆'.repeat(5 - level)}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </div>
          
          <Form.Item
            name="content"
            label="内容"
            rules={[{ required: true, message: '请输入内容' }]}
          >
            <TextArea rows={6} placeholder="请输入知识点内容" />
          </Form.Item>
          
          <Form.Item
            name="tags"
            label="标签"
            help="多个标签用逗号分隔"
          >
            <Input placeholder="请输入标签，用逗号分隔" />
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

export default KnowledgeManage;

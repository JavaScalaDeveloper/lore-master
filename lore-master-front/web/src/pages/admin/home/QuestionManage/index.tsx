import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Radio,
  Switch,
  message,
  Popconfirm,
  Tag,
  Card,
  Upload,
  Typography,
  Divider,
  Checkbox
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  DownloadOutlined,
  EyeOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Question {
  id: string;
  title: string;
  type: 'single' | 'multiple' | 'judge' | 'fill' | 'essay';
  subject: string;
  level: string;
  difficulty: number;
  content: string;
  options?: string[];
  correctAnswer: string | string[];
  explanation: string;
  tags: string[];
  status: boolean;
  usageCount: number;
  correctRate: number;
  createTime: string;
  updateTime: string;
}

const QuestionManage: React.FC = () => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<Question | null>(null);
  const [previewQuestion, setPreviewQuestion] = useState<Question | null>(null);
  const [form] = Form.useForm();
  const [selectedSubject, setSelectedSubject] = useState<string>('all');
  const [selectedLevel, setSelectedLevel] = useState<string>('all');
  const [selectedType, setSelectedType] = useState<string>('all');

  // 模拟数据
  const mockQuestions: Question[] = [
    {
      id: '1',
      title: 'Java中的基本数据类型',
      type: 'single',
      subject: 'Java编程',
      level: 'L1',
      difficulty: 1,
      content: '以下哪个不是Java的基本数据类型？',
      options: ['int', 'float', 'String', 'boolean'],
      correctAnswer: 'String',
      explanation: 'String是引用数据类型，不是基本数据类型',
      tags: ['基础语法', '数据类型'],
      status: true,
      usageCount: 156,
      correctRate: 78.5,
      createTime: '2024-01-15',
      updateTime: '2024-01-20'
    },
    {
      id: '2',
      title: '面向对象的特征',
      type: 'multiple',
      subject: 'Java编程',
      level: 'L3',
      difficulty: 3,
      content: '面向对象编程的主要特征包括哪些？',
      options: ['封装', '继承', '多态', '抽象'],
      correctAnswer: ['封装', '继承', '多态', '抽象'],
      explanation: '面向对象编程的四大特征：封装、继承、多态、抽象',
      tags: ['面向对象', '特征'],
      status: true,
      usageCount: 234,
      correctRate: 65.2,
      createTime: '2024-01-16',
      updateTime: '2024-01-21'
    },
    {
      id: '3',
      title: '英语时态判断',
      type: 'judge',
      subject: '英语学习',
      level: 'L2',
      difficulty: 2,
      content: '"I have been studying English for 3 years." 这句话使用的是现在完成进行时。',
      correctAnswer: 'true',
      explanation: '现在完成进行时的结构是have/has been + 动词ing形式',
      tags: ['时态', '语法'],
      status: true,
      usageCount: 189,
      correctRate: 72.1,
      createTime: '2024-01-17',
      updateTime: '2024-01-22'
    }
  ];

  useEffect(() => {
    fetchQuestions();
  }, [selectedSubject, selectedLevel, selectedType]);

  const fetchQuestions = async () => {
    setLoading(true);
    try {
      setTimeout(() => {
        let filteredData = mockQuestions;
        if (selectedSubject !== 'all') {
          filteredData = filteredData.filter(q => q.subject === selectedSubject);
        }
        if (selectedLevel !== 'all') {
          filteredData = filteredData.filter(q => q.level === selectedLevel);
        }
        if (selectedType !== 'all') {
          filteredData = filteredData.filter(q => q.type === selectedType);
        }
        setQuestions(filteredData);
        setLoading(false);
      }, 500);
    } catch (error) {
      message.error('获取题目列表失败');
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingQuestion(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Question) => {
    setEditingQuestion(record);
    form.setFieldsValue({
      ...record,
      tags: record.tags.join(','),
      options: record.options?.join('\n')
    });
    setModalVisible(true);
  };

  const handlePreview = (record: Question) => {
    setPreviewQuestion(record);
    setPreviewVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      message.success('删除成功');
      fetchQuestions();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const submitData = {
        ...values,
        tags: values.tags ? values.tags.split(',').map((tag: string) => tag.trim()) : [],
        options: values.options ? values.options.split('\n').filter((opt: string) => opt.trim()) : []
      };
      
      if (editingQuestion) {
        message.success('更新成功');
      } else {
        message.success('添加成功');
      }
      setModalVisible(false);
      fetchQuestions();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const getTypeTag = (type: string) => {
    const typeMap = {
      single: { color: 'blue', text: '单选题' },
      multiple: { color: 'green', text: '多选题' },
      judge: { color: 'orange', text: '判断题' },
      fill: { color: 'purple', text: '填空题' },
      essay: { color: 'red', text: '问答题' }
    };
    const config = typeMap[type as keyof typeof typeMap];
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const getDifficultyStars = (difficulty: number) => {
    return '★'.repeat(difficulty) + '☆'.repeat(5 - difficulty);
  };

  const columns = [
    {
      title: '题目标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => getTypeTag(type),
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
        <Tag color={level <= 'L3' ? 'green' : level <= 'L6' ? 'orange' : 'red'}>
          {level}
        </Tag>
      ),
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      render: (difficulty: number) => (
        <span title={`难度: ${difficulty}/5`}>
          {getDifficultyStars(difficulty)}
        </span>
      ),
    },
    {
      title: '使用次数',
      dataIndex: 'usageCount',
      key: 'usageCount',
      align: 'center' as const,
    },
    {
      title: '正确率',
      dataIndex: 'correctRate',
      key: 'correctRate',
      render: (rate: number) => (
        <span style={{ 
          color: rate >= 80 ? '#52c41a' : rate >= 60 ? '#faad14' : '#f5222d',
          fontWeight: 'bold'
        }}>
          {rate}%
        </span>
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
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Question) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record)}
          >
            预览
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这道题目吗？"
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
        <Title level={2} style={{ margin: 0 }}>题库管理</Title>
        <Space>
          <Upload>
            <Button icon={<UploadOutlined />}>批量导入</Button>
          </Upload>
          <Button icon={<DownloadOutlined />}>导出题库</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加题目
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
          <div>
            <span style={{ marginRight: 8 }}>类型：</span>
            <Select
              value={selectedType}
              onChange={setSelectedType}
              style={{ width: 120 }}
            >
              <Select.Option value="all">全部类型</Select.Option>
              <Select.Option value="single">单选题</Select.Option>
              <Select.Option value="multiple">多选题</Select.Option>
              <Select.Option value="judge">判断题</Select.Option>
              <Select.Option value="fill">填空题</Select.Option>
              <Select.Option value="essay">问答题</Select.Option>
            </Select>
          </div>
        </Space>
      </Card>

      {/* 题目列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={questions}
          rowKey="id"
          loading={loading}
          pagination={{
            total: questions.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingQuestion ? '编辑题目' : '添加题目'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
        style={{ top: 20 }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="title"
            label="题目标题"
            rules={[{ required: true, message: '请输入题目标题' }]}
          >
            <Input placeholder="请输入题目标题" />
          </Form.Item>
          
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item
              name="type"
              label="题目类型"
              rules={[{ required: true, message: '请选择题目类型' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择题目类型">
                <Select.Option value="single">单选题</Select.Option>
                <Select.Option value="multiple">多选题</Select.Option>
                <Select.Option value="judge">判断题</Select.Option>
                <Select.Option value="fill">填空题</Select.Option>
                <Select.Option value="essay">问答题</Select.Option>
              </Select>
            </Form.Item>
            
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
              name="difficulty"
              label="难度"
              rules={[{ required: true, message: '请选择难度' }]}
              style={{ flex: 1 }}
            >
              <Select placeholder="请选择难度">
                {[1, 2, 3, 4, 5].map(level => (
                  <Select.Option key={level} value={level}>
                    {getDifficultyStars(level)}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </div>
          
          <Form.Item
            name="content"
            label="题目内容"
            rules={[{ required: true, message: '请输入题目内容' }]}
          >
            <TextArea rows={4} placeholder="请输入题目内容" />
          </Form.Item>
          
          <Form.Item
            name="options"
            label="选项"
            help="每行一个选项（单选题、多选题必填）"
          >
            <TextArea rows={4} placeholder="请输入选项，每行一个" />
          </Form.Item>
          
          <Form.Item
            name="correctAnswer"
            label="正确答案"
            rules={[{ required: true, message: '请输入正确答案' }]}
          >
            <Input placeholder="请输入正确答案" />
          </Form.Item>
          
          <Form.Item
            name="explanation"
            label="答案解析"
            rules={[{ required: true, message: '请输入答案解析' }]}
          >
            <TextArea rows={3} placeholder="请输入答案解析" />
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

      {/* 题目预览模态框 */}
      <Modal
        title="题目预览"
        open={previewVisible}
        onCancel={() => setPreviewVisible(false)}
        footer={[
          <Button key="close" onClick={() => setPreviewVisible(false)}>
            关闭
          </Button>
        ]}
        width={600}
      >
        {previewQuestion && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <Space>
                {getTypeTag(previewQuestion.type)}
                <Tag>{previewQuestion.subject}</Tag>
                <Tag color={previewQuestion.level <= 'L3' ? 'green' : previewQuestion.level <= 'L6' ? 'orange' : 'red'}>
                  {previewQuestion.level}
                </Tag>
                <span>{getDifficultyStars(previewQuestion.difficulty)}</span>
              </Space>
            </div>
            
            <Title level={4}>{previewQuestion.title}</Title>
            <Text>{previewQuestion.content}</Text>
            
            {previewQuestion.options && previewQuestion.options.length > 0 && (
              <div style={{ margin: '16px 0' }}>
                <Text strong>选项：</Text>
                <div style={{ marginTop: 8 }}>
                  {previewQuestion.options.map((option, index) => (
                    <div key={index} style={{ marginBottom: 4 }}>
                      {String.fromCharCode(65 + index)}. {option}
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            <Divider />
            
            <div style={{ marginBottom: 8 }}>
              <Text strong>正确答案：</Text>
              <Text type="success">{Array.isArray(previewQuestion.correctAnswer) 
                ? previewQuestion.correctAnswer.join(', ') 
                : previewQuestion.correctAnswer}
              </Text>
            </div>
            
            <div>
              <Text strong>答案解析：</Text>
              <div style={{ marginTop: 4 }}>
                <Text>{previewQuestion.explanation}</Text>
              </div>
            </div>
            
            {previewQuestion.tags.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <Text strong>标签：</Text>
                <Space wrap style={{ marginTop: 4 }}>
                  {previewQuestion.tags.map(tag => (
                    <Tag key={tag}>{tag}</Tag>
                  ))}
                </Space>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default QuestionManage;

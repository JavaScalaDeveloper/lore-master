import React, { useEffect, useState } from 'react';
import { marked } from 'marked';
import './markdown-preview.css';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  message,
  Tag,
  Typography,
  Row,
  Col,
  Statistic,
  Tooltip,
  Modal,
  Form,
  DatePicker,
  InputNumber,
  Switch,
  Tabs,
  Upload
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  BookOutlined,
  UserOutlined,
  ClockCircleOutlined,
  StarOutlined,
  UploadOutlined,
  FileMarkdownOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons';
import { adminApi } from '../../../../utils/request';

const { Title, Text } = Typography;
const { Search, TextArea } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;

// 课程接口定义
interface Course {
  id: number;
  courseCode: string;
  title: string;
  description?: string;
  author: string;
  courseType: string;
  contentType?: string;
  difficultyLevel: string;
  difficultyLevels?: string;
  difficultyLevelList?: string[];
  parentCourseId?: number;
  parentCourseTitle?: string;
  sortOrder: number;
  status: string;
  knowledgeNodeCode?: string;
  knowledgeNodePath?: string;
  knowledgeNodeNamePath?: string;
  tags?: string;
  tagList?: string[];
  durationMinutes?: number;
  formattedDuration?: string;
  viewCount: number;
  likeCount: number;
  collectCount: number;
  contentUrl?: string;
  coverImageUrl?: string;
  thumbnailUrl?: string;
  contentMarkdown?: string;
  contentHtml?: string;
  contentUpdatedTime?: string;
  contentFileIds?: string;
  publishTime?: string;
  createdTime: string;
  subCourseCount?: number;
  subCourses?: Course[];
  isCollected?: boolean;
  isLiked?: boolean;
  progressPercent?: number;
}

// 统计信息接口
interface CourseStatistics {
  totalCourses: number;
  publishedCourses: number;
  draftCourses: number;
  totalEnrollments: number;
  averageRating: number;
  totalRevenue: number;
}

// 查询参数接口
interface QueryParams {
  page: number;
  size: number;
  keyword?: string;
  courseType?: string;
  difficultyLevel?: string;
  status?: string;
  publishedOnly?: boolean;
}

const CourseManage: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [statistics, setStatistics] = useState<CourseStatistics>({
    totalCourses: 0,
    publishedCourses: 0,
    draftCourses: 0,
    totalEnrollments: 0,
    averageRating: 0,
    totalRevenue: 0
  });
  
  // 查询参数
  const [queryParams, setQueryParams] = useState<QueryParams>({
    page: 1,
    size: 10
  });

  // 表单相关
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [form] = Form.useForm();

  // 知识点数据
  const [knowledgeNodes, setKnowledgeNodes] = useState<any[]>([]);
  const [knowledgeLoading, setKnowledgeLoading] = useState(false);

  // Markdown编辑器状态
  const [markdownContent, setMarkdownContent] = useState('');
  const [markdownPreview, setMarkdownPreview] = useState('');
  const [previewMode, setPreviewMode] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<any[]>([]);

  // 加载课程列表
  const loadCourses = async () => {
    setLoading(true);
    try {
      const requestParams = {
        ...queryParams,
        page: Math.max(0, queryParams.page - 1) // 转换为后端期望的从0开始的页码，确保不会小于0
      };
      const response = await adminApi.post('/api/admin/course/list', requestParams);
      if (response.success) {
        setCourses(response.data.courses || []);
        setTotal(response.data.totalElements || 0);
      } else {
        message.error('加载课程列表失败: ' + response.message);
      }
    } catch (error) {
      console.error('加载课程列表失败:', error);
      message.error('加载课程列表失败，请检查网络连接');
    } finally {
      setLoading(false);
    }
  };

  // 加载统计信息
  const loadStatistics = async () => {
    try {
      const response = await adminApi.get('/api/admin/course/statistics');
      if (response.success) {
        setStatistics(response.data);
      }
    } catch (error) {
      console.error('加载统计信息失败:', error);
    }
  };

  // 加载知识点数据
  const loadKnowledgeNodes = async (currentKnowledgeNodePath?: string) => {
    setKnowledgeLoading(true);
    try {
      let rootCodes: string[] = [];

      if (currentKnowledgeNodePath) {
        // 从当前知识点路径提取根路径
        const rootCode = currentKnowledgeNodePath.split('/')[0];
        rootCodes = [rootCode];
      } else {
        // 如果没有当前路径，加载所有常用的根节点
        rootCodes = ['frontend', 'backend', 'database', 'devops', 'mobile', 'ai', 'tools'];
      }

      const allNodes: any[] = [];

      for (const rootCode of rootCodes) {
        try {
          const response = await adminApi.post(`/api/admin/knowledge-map/getSkillTree?rootCode=${rootCode}`);
          if (response.success && response.data.children) {
            const flatNodes = flattenTreeNodes(response.data.children, response.data.rootName);
            allNodes.push(...flatNodes);
          }
        } catch (error) {
          console.warn(`加载${rootCode}技能树失败:`, error);
        }
      }

      setKnowledgeNodes(allNodes);
    } catch (error) {
      console.error('加载知识点数据失败:', error);
      message.error('加载知识点数据失败');
    } finally {
      setKnowledgeLoading(false);
    }
  };

  // 扁平化树形节点数据
  const flattenTreeNodes = (nodes: any[], parentPath = ''): any[] => {
    const result: any[] = [];

    nodes.forEach(node => {
      const currentPath = parentPath ? `${parentPath}/${node.nodeNameStr || node.nodeName}` : (node.nodeNameStr || node.nodeName);

      result.push({
        nodeCode: node.nodeCode,
        nodeName: node.nodeNameStr || node.nodeName,
        nodeType: node.nodeType,
        fullPath: currentPath,
        levelDepth: node.levelDepth
      });

      if (node.children && node.children.length > 0) {
        result.push(...flattenTreeNodes(node.children, currentPath));
      }
    });

    return result;
  };

  // 初始化加载
  useEffect(() => {
    loadCourses();
    loadStatistics();
  }, [queryParams]);

  // 搜索处理
  const handleSearch = (value: string) => {
    setQueryParams(prev => ({
      ...prev,
      page: 1,
      keyword: value
    }));
  };

  // 筛选处理
  const handleFilter = (key: string, value: any) => {
    setQueryParams(prev => ({
      ...prev,
      page: 1,
      [key]: value
    }));
  };

  // 重置筛选
  const handleReset = () => {
    setQueryParams({
      page: 1,
      size: 10
    });
  };

  // 查看详情
  const handleViewDetail = async (course: Course) => {
    try {
      const response = await adminApi.get(`/api/admin/course/detail/${course.id}`);
      if (response.success) {
        setSelectedCourse(response.data);
        setDetailModalVisible(true);
      } else {
        message.error('获取课程详情失败');
      }
    } catch (error) {
      console.error('获取课程详情失败:', error);
      message.error('获取课程详情失败');
    }
  };

  // 文件上传处理
  const handleFileUpload = async (file: any) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bucketName', 'course-content');
    formData.append('isPublic', 'true');
    formData.append('remark', '课程内容文件');

    try {
      const response = await adminApi.post('/api/admin/file/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.success) {
        const fileInfo = response.data;
        setUploadedFiles(prev => [...prev, fileInfo]);

        // 根据文件类型生成Markdown语法
        let markdownSyntax = '';
        if (fileInfo.fileType.startsWith('image/')) {
          markdownSyntax = `![${fileInfo.originalName}](/api/file/view?fileId=${fileInfo.fileId})`;
        } else {
          markdownSyntax = `[${fileInfo.originalName}](/api/file/view?fileId=${fileInfo.fileId})`;
        }

        // 插入到当前光标位置或末尾
        const currentContent = form.getFieldValue('contentMarkdown') || '';
        const newContent = currentContent + '\n' + markdownSyntax + '\n';
        form.setFieldValue('contentMarkdown', newContent);
        handleMarkdownChange(newContent);

        message.success('文件上传成功');
        return false; // 阻止默认上传行为
      } else {
        message.error('文件上传失败：' + response.message);
        return false;
      }
    } catch (error) {
      console.error('文件上传失败:', error);
      message.error('文件上传失败');
      return false;
    }
  };

  // 图片代理处理函数
  const processImageUrls = (html: string): string => {
    // 匹配所有img标签中的src属性
    return html.replace(/<img([^>]*?)src=["']([^"']*)["']([^>]*?)>/gi, (match, before, src, after) => {
      console.log('Processing image:', src); // 调试日志
      // 检查是否是外部链接（http/https开头且不是本站链接）
      if (src.startsWith('http') && !src.includes(window.location.hostname)) {
        // 使用免费的图片代理服务
        const proxySrc = `https://images.weserv.nl/?url=${encodeURIComponent(src)}`;
        console.log('Converting to proxy URL:', proxySrc); // 调试日志
        return `<img${before}src="${proxySrc}"${after} onerror="this.onerror=null;this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuWbvueJh+WKoOi9veWksei0pTwvdGV4dD48L3N2Zz4=';this.title='图片加载失败: ${src}'">`;
      }
      console.log('Keeping original URL:', src); // 调试日志
      return match;
    });
  };

  // Markdown内容变化处理
  const handleMarkdownChange = (value: string) => {
    console.log('Markdown content changed:', value); // 调试日志
    setMarkdownContent(value);
    // 同步更新表单字段值
    form.setFieldValue('contentMarkdown', value);

    // 使用专业的 marked 库解析 Markdown
    try {
      // 配置 marked 选项
      marked.setOptions({
        breaks: true, // 支持 GFM 换行
        gfm: true,   // 启用 GitHub Flavored Markdown
      });
      
      let htmlContent = marked(value) as string;
      console.log('Original HTML content:', htmlContent); // 调试日志
      
      // 处理外部图片链接
      htmlContent = processImageUrls(htmlContent);
      console.log('Processed HTML content:', htmlContent); // 调试日志
      
      setMarkdownPreview(htmlContent);
    } catch (error) {
      console.error('Markdown 解析失败:', error);
      // 如果解析失败，回退到简单的文本显示
      setMarkdownPreview(value.replace(/\n/g, '<br>'));
    }
  };

  // 新增课程
  const handleCreate = () => {
    setIsCreating(true);
    setEditingCourse(null);
    form.resetFields();
    setMarkdownContent('');
    setMarkdownPreview('');
    setUploadedFiles([]);
    setEditModalVisible(true);
    // 新增时加载所有根节点的知识点
    loadKnowledgeNodes();
  };

  // 编辑课程
  const handleEdit = (course: Course) => {
    setIsCreating(false);
    setEditingCourse(course);

    // 设置表单初始值
    form.setFieldsValue({
      courseCode: course.courseCode,
      title: course.title,
      description: course.description,
      author: course.author,
      courseType: course.courseType,
      contentType: course.contentType,
      difficultyLevel: course.difficultyLevel,
      status: course.status,
      knowledgeNodeCode: course.knowledgeNodeCode,
      tags: course.tags,
      durationMinutes: course.durationMinutes,
      sortOrder: course.sortOrder,
      contentUrl: course.contentUrl,
      coverImageUrl: course.coverImageUrl,
      thumbnailUrl: course.thumbnailUrl,
      contentMarkdown: course.contentMarkdown
    });

    // 设置Markdown内容
    const markdownValue = course.contentMarkdown || '';
    setMarkdownContent(markdownValue);
    handleMarkdownChange(markdownValue);

    setUploadedFiles([]);

    setEditModalVisible(true);

    // 根据当前课程的知识点路径加载相关技能树
    if (course.knowledgeNodePath) {
      loadKnowledgeNodes(course.knowledgeNodePath);
    } else {
      // 如果没有知识点路径，加载所有根节点
      loadKnowledgeNodes();
    }
  };

  // 保存课程
  const handleSave = async () => {
    try {
      const values = await form.validateFields();

      // 根据知识点编码设置路径信息
      const selectedNode = knowledgeNodes.find(node => node.nodeCode === values.knowledgeNodeCode);
      if (selectedNode) {
        values.knowledgeNodePath = selectedNode.nodeCode;
        values.knowledgeNodeNamePath = selectedNode.fullPath;
      }

      let response;
      if (isCreating) {
        response = await adminApi.post('/api/admin/course/create', values);
      } else {
        values.id = editingCourse?.id;
        response = await adminApi.post('/api/admin/course/update', values);
      }

      if (response.success) {
        message.success(isCreating ? '创建课程成功' : '更新课程成功');
        setEditModalVisible(false);
        loadCourses();
        loadStatistics();
      } else {
        message.error(response.message || '操作失败');
      }
    } catch (error) {
      console.error('保存课程失败:', error);
      message.error('保存课程失败');
    }
  };

  // 删除课程
  const handleDelete = (course: Course) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除课程"${course.title}"吗？此操作不可恢复。`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          const response = await adminApi.delete(`/api/admin/course/delete/${course.id}`);
          if (response.success) {
            message.success('删除课程成功');
            loadCourses();
            loadStatistics();
          } else {
            message.error(response.message || '删除失败');
          }
        } catch (error) {
          console.error('删除课程失败:', error);
          message.error('删除课程失败');
        }
      }
    });
  };

  // 获取课程类型标签
  const getCourseTypeTag = (type: string) => {
    const typeMap: { [key: string]: { color: string; text: string } } = {
      'VIDEO': { color: 'blue', text: '视频课程' },
      'LIVE': { color: 'green', text: '直播课程' },
      'TEXT': { color: 'orange', text: '图文课程' },
      'MIXED': { color: 'purple', text: '混合课程' }
    };
    const config = typeMap[type] || { color: 'default', text: type };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 获取难度标签
  const getDifficultyTag = (difficulty: string) => {
    const difficultyMap: { [key: string]: { color: string; text: string } } = {
      'L1': { color: 'green', text: 'L1-入门' },
      'L2': { color: 'blue', text: 'L2-初级' },
      'L3': { color: 'orange', text: 'L3-中级' },
      'L4': { color: 'red', text: 'L4-高级' },
      'L5': { color: 'purple', text: 'L5-专家' },
      'BEGINNER': { color: 'green', text: '初级' },
      'INTERMEDIATE': { color: 'orange', text: '中级' },
      'ADVANCED': { color: 'red', text: '高级' },
      'EXPERT': { color: 'purple', text: '专家' }
    };
    const config = difficultyMap[difficulty] || { color: 'default', text: difficulty };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const statusMap: { [key: string]: { color: string; text: string } } = {
      'PUBLISHED': { color: 'green', text: '已发布' },
      'DRAFT': { color: 'orange', text: '草稿' },
      'ARCHIVED': { color: 'red', text: '已归档' },
      'ACTIVE': { color: 'green', text: '启用' },
      'INACTIVE': { color: 'red', text: '禁用' }
    };
    const config = statusMap[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };



  // 表格列定义
  const columns = [
    {
      title: '课程编码',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 120,
      fixed: 'left' as const,
    },
    {
      title: '课程名称',
      dataIndex: 'title',
      key: 'title',
      width: 200,
      render: (text: string) => (
        <Space>
          <BookOutlined style={{ color: '#1890ff' }} />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '课程类型',
      dataIndex: 'courseType',
      key: 'courseType',
      width: 100,
      render: (type: string) => getCourseTypeTag(type),
    },
    {
      title: '难度等级',
      dataIndex: 'difficultyLevel',
      key: 'difficultyLevel',
      width: 100,
      render: (difficulty: string) => getDifficultyTag(difficulty),
    },
    {
      title: '时长(分钟)',
      dataIndex: 'durationMinutes',
      key: 'durationMinutes',
      width: 100,
      render: (duration: number) => duration ? (
        <Space>
          <ClockCircleOutlined />
          <span>{duration}分钟</span>
        </Space>
      ) : '-',
    },
    {
      title: '内容类型',
      dataIndex: 'contentType',
      key: 'contentType',
      width: 100,
      render: (type: string) => type ? (
        <Tag color={type === 'ARTICLE' ? 'blue' : 'green'}>
          {type === 'ARTICLE' ? '图文' : type === 'VIDEO' ? '视频' : type}
        </Tag>
      ) : '-',
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
      width: 120,
      render: (name: string) => name ? (
        <Space>
          <UserOutlined />
          <span>{name}</span>
        </Space>
      ) : '-',
    },
    {
      title: '观看次数',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 100,
      render: (count: number) => (
        <Text type={count > 0 ? 'success' : 'secondary'}>{count}</Text>
      ),
    },
    {
      title: '点赞数',
      dataIndex: 'likeCount',
      key: 'likeCount',
      width: 80,
      render: (count: number) => count > 0 ? (
        <Space>
          <StarOutlined style={{ color: '#faad14' }} />
          <span>{count}</span>
        </Space>
      ) : '-',
    },
    {
      title: '收藏数',
      dataIndex: 'collectCount',
      key: 'collectCount',
      width: 80,
      render: (count: number) => (
        <Text type={count > 0 ? 'success' : 'secondary'}>{count}</Text>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: Course) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="link"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="删除">
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>📚 课程管理</Title>
      
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card>
            <Statistic
              title="总课程数"
              value={statistics.totalCourses}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="已发布"
              value={statistics.publishedCourses}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="草稿"
              value={statistics.draftCourses}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="总报名数"
              value={statistics.totalEnrollments}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="平均评分"
              value={statistics.averageRating}
              precision={1}
              prefix={<StarOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="总收入"
              value={statistics.totalRevenue}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索和筛选 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={6}>
            <Search
              placeholder="搜索课程名称、编码、讲师..."
              allowClear
              onSearch={handleSearch}
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={3}>
            <Select
              placeholder="课程类型"
              allowClear
              style={{ width: '100%' }}
              onChange={(value) => handleFilter('courseType', value)}
              value={queryParams.courseType}
            >
              <Option value="VIDEO">视频课程</Option>
              <Option value="LIVE">直播课程</Option>
              <Option value="TEXT">图文课程</Option>
              <Option value="MIXED">混合课程</Option>
            </Select>
          </Col>
          <Col span={3}>
            <Select
              placeholder="难度等级"
              allowClear
              style={{ width: '100%' }}
              onChange={(value) => handleFilter('difficultyLevel', value)}
              value={queryParams.difficultyLevel}
            >
              <Option value="L1">L1-入门</Option>
              <Option value="L2">L2-初级</Option>
              <Option value="L3">L3-中级</Option>
              <Option value="L4">L4-高级</Option>
              <Option value="L5">L5-专家</Option>
            </Select>
          </Col>
          <Col span={3}>
            <Select
              placeholder="状态"
              allowClear
              style={{ width: '100%' }}
              onChange={(value) => handleFilter('status', value)}
              value={queryParams.status}
            >
              <Option value="PUBLISHED">已发布</Option>
              <Option value="DRAFT">草稿</Option>
              <Option value="ARCHIVED">已归档</Option>
            </Select>
          </Col>

          <Col span={9}>
            <Space>
              <Button icon={<ReloadOutlined />} onClick={loadCourses}>
                刷新
              </Button>
              <Button onClick={handleReset}>
                重置
              </Button>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleCreate}
              >
                添加课程
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 课程列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={courses}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1500 }}
          pagination={{
            current: queryParams.page,
            pageSize: queryParams.size,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条 (共 ${Math.ceil(total / queryParams.size)} 页)`,
            pageSizeOptions: [10, 20, 50, 100, 200],
            onChange: (page, size) => {
              // 确保页码不会小于1
              const newPage = Math.max(1, page);
              setQueryParams(prev => ({ 
                ...prev, 
                page: newPage, 
                size: size || prev.size || 10 
              }));
            },
            onShowSizeChange: (current, size) => {
              // 分页大小变化时，重置到第1页
              setQueryParams(prev => ({ 
                ...prev, 
                page: 1, 
                size: size || 10 
              }));
            },
          }}
        />
      </Card>

      {/* 课程详情模态框 */}
      <Modal
        title="课程详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedCourse && (
          <div>
            <Row gutter={16}>
              <Col span={12}>
                <p><strong>课程编码：</strong>{selectedCourse.courseCode}</p>
                <p><strong>课程名称：</strong>{selectedCourse.title}</p>
                <p><strong>课程类型：</strong>{getCourseTypeTag(selectedCourse.courseType)}</p>
                <p><strong>内容类型：</strong>{selectedCourse.contentType || '-'}</p>
                <p><strong>难度等级：</strong>{getDifficultyTag(selectedCourse.difficultyLevel)}</p>
                <p><strong>时长：</strong>{selectedCourse.durationMinutes ? `${selectedCourse.durationMinutes}分钟` : '-'}</p>
                <p><strong>排序：</strong>{selectedCourse.sortOrder}</p>
              </Col>
              <Col span={12}>
                <p><strong>作者：</strong>{selectedCourse.author || '-'}</p>
                <p><strong>知识节点：</strong>{selectedCourse.knowledgeNodeNamePath || '-'}</p>
                <p><strong>观看次数：</strong>{selectedCourse.viewCount}</p>
                <p><strong>点赞数：</strong>{selectedCourse.likeCount}</p>
                <p><strong>收藏数：</strong>{selectedCourse.collectCount}</p>
                <p><strong>状态：</strong>{getStatusTag(selectedCourse.status)}</p>
                <p><strong>发布时间：</strong>{selectedCourse.publishTime ? new Date(selectedCourse.publishTime).toLocaleString() : '-'}</p>
              </Col>
            </Row>
            {selectedCourse.description && (
              <div>
                <p><strong>课程描述：</strong></p>
                <p style={{ background: '#f5f5f5', padding: 12, borderRadius: 4 }}>
                  {selectedCourse.description}
                </p>
              </div>
            )}
            {selectedCourse.tagList && selectedCourse.tagList.length > 0 && (
              <div>
                <p><strong>标签：</strong></p>
                <Space wrap>
                  {selectedCourse.tagList.map((tag, index) => (
                    <Tag key={index}>{tag}</Tag>
                  ))}
                </Space>
              </div>
            )}
            {selectedCourse.contentMarkdown && (
              <div>
                <p><strong>课程内容：</strong></p>
                <div
                  style={{
                    maxHeight: '400px',
                    padding: '16px',
                    border: '1px solid #d9d9d9',
                    borderRadius: '6px',
                    backgroundColor: '#fafafa',
                    overflow: 'auto'
                  }}
                  className="markdown-preview"
                  dangerouslySetInnerHTML={{ 
                    __html: processImageUrls(marked(selectedCourse.contentMarkdown) as string) 
                  }}
                />
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* 编辑/新增课程模态框 */}
      <Modal
        title={isCreating ? '新增课程' : '编辑课程'}
        open={editModalVisible}
        onCancel={() => setEditModalVisible(false)}
        onOk={handleSave}
        width={800}
        okText="保存"
        cancelText="取消"
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            courseType: 'NORMAL',
            contentType: 'ARTICLE',
            status: 'DRAFT',
            sortOrder: 0
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="courseCode"
                label="课程编码"
                rules={[{ required: true, message: '请输入课程编码' }]}
              >
                <Input placeholder="请输入课程编码" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="title"
                label="课程标题"
                rules={[{ required: true, message: '请输入课程标题' }]}
              >
                <Input placeholder="请输入课程标题" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="课程描述"
          >
            <TextArea rows={3} placeholder="请输入课程描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="author"
                label="作者"
                rules={[{ required: true, message: '请输入作者' }]}
              >
                <Input placeholder="请输入作者" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="courseType"
                label="课程类型"
                rules={[{ required: true, message: '请选择课程类型' }]}
              >
                <Select placeholder="请选择课程类型">
                  <Option value="NORMAL">普通课程</Option>
                  <Option value="COLLECTION">合集</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="contentType"
                label="内容类型"
              >
                <Select placeholder="请选择内容类型">
                  <Option value="ARTICLE">图文</Option>
                  <Option value="VIDEO">视频</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="difficultyLevel"
                label="难度等级"
              >
                <Select placeholder="请选择难度等级">
                  <Option value="L1">L1-入门</Option>
                  <Option value="L2">L2-初级</Option>
                  <Option value="L3">L3-中级</Option>
                  <Option value="L4">L4-高级</Option>
                  <Option value="L5">L5-专家</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="status"
                label="状态"
                rules={[{ required: true, message: '请选择状态' }]}
              >
                <Select placeholder="请选择状态">
                  <Option value="DRAFT">草稿</Option>
                  <Option value="PUBLISHED">已发布</Option>
                  <Option value="ARCHIVED">已归档</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="knowledgeNodeCode"
                label="知识点"
              >
                <Select
                  placeholder="请选择知识点"
                  loading={knowledgeLoading}
                  showSearch
                  filterOption={(input, option) =>
                    option?.children?.toString().toLowerCase().includes(input.toLowerCase()) || false
                  }
                  onFocus={() => {
                    // 当聚焦且没有数据时，加载知识点数据
                    if (knowledgeNodes.length === 0) {
                      loadKnowledgeNodes();
                    }
                  }}
                  notFoundContent={
                    knowledgeLoading ? (
                      <div style={{ textAlign: 'center', padding: '20px' }}>
                        <span>加载中...</span>
                      </div>
                    ) : knowledgeNodes.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: '20px' }}>
                        <div>暂无数据</div>
                        <Button
                          type="link"
                          size="small"
                          onClick={() => loadKnowledgeNodes()}
                          style={{ padding: 0, marginTop: '8px' }}
                        >
                          🔄 点击加载知识点
                        </Button>
                      </div>
                    ) : null
                  }
                >
                  {knowledgeNodes.map(node => (
                    <Option key={node.nodeCode} value={node.nodeCode}>
                      {node.fullPath}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="durationMinutes"
                label="时长(分钟)"
              >
                <InputNumber min={0} placeholder="请输入时长" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="sortOrder"
                label="排序"
              >
                <InputNumber min={0} placeholder="请输入排序" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="tags"
            label="标签"
          >
            <Input placeholder="请输入标签，用逗号分隔" />
          </Form.Item>

          <Form.Item
            name="contentUrl"
            label="内容链接"
          >
            <Input placeholder="请输入内容链接" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="coverImageUrl"
                label="封面图片"
              >
                <Input placeholder="请输入封面图片链接" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="thumbnailUrl"
                label="缩略图"
              >
                <Input placeholder="请输入缩略图链接" />
              </Form.Item>
            </Col>
          </Row>

          {/* Markdown内容编辑器 */}
          <Form.Item
            name="contentMarkdown"
            label="课程详情内容"
          >
            <Tabs
              activeKey={previewMode ? 'preview' : 'edit'}
              onChange={(key) => setPreviewMode(key === 'preview')}
              tabBarExtraContent={
                <Space>
                  <Upload
                    beforeUpload={handleFileUpload}
                    showUploadList={false}
                    accept="image/*,video/*,.pdf,.doc,.docx"
                  >
                    <Button icon={<UploadOutlined />} size="small">
                      上传文件
                    </Button>
                  </Upload>
                </Space>
              }
              items={[
                {
                  key: 'edit',
                  label: (
                    <span>
                      <FileMarkdownOutlined />
                      编辑
                    </span>
                  ),
                  children: (
                    <TextArea
                      placeholder="请输入Markdown格式的课程详情内容..."
                      rows={15}
                      style={{ fontFamily: 'Monaco, Consolas, monospace' }}
                      value={markdownContent}
                      onChange={(e) => handleMarkdownChange(e.target.value)}
                    />
                  )
                },
                {
                  key: 'preview',
                  label: (
                    <span>
                      <EyeOutlined />
                      预览
                    </span>
                  ),
                  children: (
                    <div
                      style={{
                        minHeight: '400px',
                        padding: '16px',
                        border: '1px solid #d9d9d9',
                        borderRadius: '6px',
                        backgroundColor: '#fafafa',
                        overflow: 'auto'
                      }}
                      className="markdown-preview"
                      dangerouslySetInnerHTML={{ __html: markdownPreview || '暂无内容' }}
                    />
                  )
                }
              ]}
            />
          </Form.Item>

          {/* 上传文件列表 */}
          {uploadedFiles.length > 0 && (
            <Form.Item label="已上传文件">
              <div style={{ maxHeight: '150px', overflowY: 'auto' }}>
                {uploadedFiles.map((file, index) => (
                  <Tag key={index} style={{ margin: '2px' }}>
                    {file.originalName}
                  </Tag>
                ))}
              </div>
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default CourseManage;

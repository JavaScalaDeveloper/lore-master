import React, { useState, useEffect } from 'react';
import { 
  Table, 
  Button, 
  Space, 
  Modal, 
  Form, 
  Input, 
  Select, 
  Upload, 
  message, 
  Popconfirm,
  Switch,
  InputNumber,
  Card,
  Row,
  Col,
  Tag,
  Image,
  Typography,
  Tabs
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  EyeOutlined,
  UploadOutlined,
  DragOutlined
} from '@ant-design/icons';
import { marked } from 'marked';
import { adminApi } from '../../../../utils/request';
import './CarouselManage.css';

const { TextArea } = Input;
const { Option } = Select;
const { Text } = Typography;
const { TabPane } = Tabs;

interface CarouselBanner {
  bannerId: string;
  title: string;
  subtitle?: string;
  coverImageUrl: string;
  contentMarkdown?: string;
  contentHtml?: string;
  jumpUrl?: string;
  sortOrder: number;
  status: string;
  viewCount: number;
  createdTime: string;
  updatedTime?: string;
}

const CarouselManage: React.FC = () => {
  const [banners, setBanners] = useState<CarouselBanner[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [previewModalVisible, setPreviewModalVisible] = useState(false);
  const [editingBanner, setEditingBanner] = useState<CarouselBanner | null>(null);
  const [previewBanner, setPreviewBanner] = useState<CarouselBanner | null>(null);
  const [form] = Form.useForm();
  const [markdownContent, setMarkdownContent] = useState('');
  const [activeTab, setActiveTab] = useState('split');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  });

  useEffect(() => {
    loadBanners();
  }, [pagination.current, pagination.pageSize]);

  const loadBanners = async () => {
    try {
      setLoading(true);
      const response = await adminApi.post('/api/admin/carousel/list', {
        page: pagination.current - 1,
        size: pagination.pageSize
      });

      if (response.success) {
        setBanners(response.data.content);
        setPagination(prev => ({
          ...prev,
          total: response.data.totalElements
        }));
      } else {
        message.error('获取轮播图列表失败: ' + response.message);
      }
    } catch (error) {
      console.error('获取轮播图列表失败:', error);
      message.error('获取轮播图列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingBanner(null);
    form.resetFields();
    setMarkdownContent('');
    setActiveTab('split');
    setModalVisible(true);
  };

  const handleEdit = (banner: CarouselBanner) => {
    setEditingBanner(banner);
    form.setFieldsValue({
      ...banner,
      status: banner.status === 'ACTIVE'
    });
    setMarkdownContent(banner.contentMarkdown || '');
    setActiveTab('split');
    setModalVisible(true);
  };

  const handleDelete = async (bannerId: string) => {
    try {
      const response = await adminApi.post('/api/admin/carousel/delete', { bannerId });
      if (response.success) {
        message.success('删除成功');
        loadBanners();
      } else {
        message.error('删除失败: ' + response.message);
      }
    } catch (error) {
      console.error('删除轮播图失败:', error);
      message.error('删除失败，请稍后重试');
    }
  };

  const handleStatusChange = async (bannerId: string, status: string) => {
    try {
      const response = await adminApi.post('/api/admin/carousel/updateStatus', { bannerId, status });
      if (response.success) {
        message.success('状态更新成功');
        loadBanners();
      } else {
        message.error('状态更新失败: ' + response.message);
      }
    } catch (error) {
      console.error('更新状态失败:', error);
      message.error('状态更新失败，请稍后重试');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const requestData = {
        ...values,
        contentMarkdown: markdownContent,
        status: values.status ? 'ACTIVE' : 'INACTIVE'
      };

      let response;
      if (editingBanner) {
        response = await adminApi.post('/api/admin/carousel/update', {
          ...requestData,
          bannerId: editingBanner.bannerId
        });
      } else {
        response = await adminApi.post('/api/admin/carousel/create', requestData);
      }

      if (response.success) {
        message.success(editingBanner ? '更新成功' : '创建成功');
        setModalVisible(false);
        loadBanners();
      } else {
        message.error((editingBanner ? '更新' : '创建') + '失败: ' + response.message);
      }
    } catch (error) {
      console.error('提交失败:', error);
      message.error('提交失败，请稍后重试');
    }
  };

  const handleViewDetail = (banner: CarouselBanner) => {
    setPreviewBanner(banner);
    setPreviewModalVisible(true);
  };

  const renderMarkdown = (markdown: string) => {
    const html = marked(markdown);
    return { __html: html };
  };

  const columns = [
    {
      title: '封面图',
      dataIndex: 'coverImageUrl',
      key: 'coverImageUrl',
      width: 120,
      render: (url: string) => (
        <Image
          width={80}
          height={50}
          src={url}
          style={{ objectFit: 'cover', borderRadius: 4 }}
          fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMIAAADDCAYAAADQvc6UAAABRWlDQ1BJQ0MgUHJvZmlsZQAAKJFjYGASSSwoyGFhYGDIzSspCnJ3UoiIjFJgf8LAwSDCIMogwMCcmFxc4BgQ4ANUwgCjUcG3awyMIPqyLsis7PPOq3QdDFcvjV3jOD1boQVTPQrgSkktTgbSf4A4LbmgqISBgTEFyFYuLykAsTuAbJEioKOA7DkgdjqEvQHEToKwj4DVhAQ5A9k3gGyB5IxEoBmML4BsnSQk8XQkNtReEOBxcfXxUQg1Mjc0dyHgXNJBSWpFCYh2zi+oLMpMzyhRcASGUqqCZ16yno6CkYGRAQMDKMwhqj/fAIcloxgHQqxAjIHBEugw5sUIsSQpBobtQPdLciLEVJYzMPBHMDBsayhILEqEO4DxG0txmrERhM29nYGBddr//5/DGRjYNRkY/l7////39v///y4Dmn+LgeHANwDrkl1AuO+pmgAAADhlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAAqACAAQAAAABAAAAwqADAAQAAAABAAAAwwAAAAD9b/HnAAAHlklEQVR4Ae3dP3Ik1RnG4W+FgYxN"
        />
      )
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true
    },
    {
      title: '副标题',
      dataIndex: 'subtitle',
      key: 'subtitle',
      ellipsis: true,
      render: (text: string) => text || '-'
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 80,
      sorter: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string, record: CarouselBanner) => (
        <Switch
          checked={status === 'ACTIVE'}
          onChange={(checked) => handleStatusChange(record.bannerId, checked ? 'ACTIVE' : 'INACTIVE')}
          checkedChildren="启用"
          unCheckedChildren="禁用"
        />
      )
    },
    {
      title: '查看次数',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: CarouselBanner) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
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
            title="确定要删除这个轮播图吗？"
            onConfirm={() => handleDelete(record.bannerId)}
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
      )
    }
  ];

  return (
    <div className="carousel-manage">
      <Card>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <Col>
            <h2>轮播图管理</h2>
          </Col>
          <Col>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreate}
            >
              新建轮播图
            </Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={banners}
          rowKey="bannerId"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
          onChange={(paginationInfo) => {
            setPagination(prev => ({
              ...prev,
              current: paginationInfo.current || 1,
              pageSize: paginationInfo.pageSize || 10
            }));
          }}
        />
      </Card>

      <Modal
        title={editingBanner ? '编辑轮播图' : '新建轮播图'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={1200}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            status: true,
            sortOrder: 0
          }}
        >
          <Form.Item
            label="标题"
            name="title"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入轮播图标题" />
          </Form.Item>

          <Form.Item
            label="副标题"
            name="subtitle"
          >
            <Input placeholder="请输入副标题（可选）" />
          </Form.Item>

          <Form.Item
            label="封面图片URL"
            name="coverImageUrl"
            rules={[{ required: true, message: '请输入封面图片URL' }]}
          >
            <Input placeholder="请输入封面图片URL" />
          </Form.Item>

          <Form.Item
            label="跳转链接"
            name="jumpUrl"
          >
            <Input placeholder="请输入跳转链接（可选）" />
          </Form.Item>

          <Form.Item
            label="排序顺序"
            name="sortOrder"
            rules={[{ required: true, message: '请输入排序顺序' }]}
          >
            <InputNumber
              min={0}
              placeholder="数字越小越靠前"
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item label="详情内容 (Markdown)">
            <Tabs 
              activeKey={activeTab} 
              onChange={setActiveTab}
              items={[
                {
                  key: 'edit',
                  label: '编辑',
                  children: (
                    <TextArea
                      value={markdownContent}
                      onChange={(e) => setMarkdownContent(e.target.value)}
                      rows={12}
                      placeholder="请输入详情内容，支持Markdown格式"
                      style={{ fontFamily: 'Monaco, Consolas, monospace', fontSize: '13px' }}
                    />
                  )
                },
                {
                  key: 'preview',
                  label: '预览',
                  children: (
                    <div 
                      style={{ 
                        minHeight: '300px',
                        maxHeight: '400px',
                        overflow: 'auto',
                        padding: '16px',
                        border: '1px solid #d9d9d9',
                        borderRadius: '6px',
                        backgroundColor: '#fafafa'
                      }}
                    >
                      {markdownContent ? (
                        <div 
                          dangerouslySetInnerHTML={renderMarkdown(markdownContent)}
                          className="markdown-content"
                          style={{
                            fontSize: '14px',
                            lineHeight: '1.6'
                          }}
                        />
                      ) : (
                        <Text type="secondary" style={{ fontStyle: 'italic' }}>
                          请在编辑区域输入Markdown内容以查看预览
                        </Text>
                      )}
                    </div>
                  )
                },
                {
                  key: 'split',
                  label: '分屏',
                  children: (
                    <div style={{ display: 'flex', gap: '16px', height: '350px' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ marginBottom: '8px', fontWeight: 'bold' }}>编辑</div>
                        <TextArea
                          value={markdownContent}
                          onChange={(e) => setMarkdownContent(e.target.value)}
                          style={{ 
                            height: '320px',
                            fontFamily: 'Monaco, Consolas, monospace', 
                            fontSize: '13px',
                            resize: 'none'
                          }}
                          placeholder="请输入详情内容，支持Markdown格式"
                        />
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ marginBottom: '8px', fontWeight: 'bold' }}>预览</div>
                        <div 
                          style={{ 
                            height: '320px',
                            overflow: 'auto',
                            padding: '12px',
                            border: '1px solid #d9d9d9',
                            borderRadius: '6px',
                            backgroundColor: '#fafafa'
                          }}
                        >
                          {markdownContent ? (
                            <div 
                              dangerouslySetInnerHTML={renderMarkdown(markdownContent)}
                              className="markdown-content"
                              style={{
                                fontSize: '14px',
                                lineHeight: '1.6'
                              }}
                            />
                          ) : (
                            <Text type="secondary" style={{ fontStyle: 'italic' }}>
                              预览区域
                            </Text>
                          )}
                        </div>
                      </div>
                    </div>
                  )
                }
              ]}
            />
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            valuePropName="checked"
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit">
                {editingBanner ? '更新' : '创建'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 预览模态框 */}
      <Modal
        title="轮播图预览"
        open={previewModalVisible}
        onCancel={() => setPreviewModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setPreviewModalVisible(false)}>
            关闭
          </Button>,
          <Button 
            key="edit" 
            type="primary" 
            onClick={() => {
              setPreviewModalVisible(false);
              if (previewBanner) {
                handleEdit(previewBanner);
              }
            }}
          >
            编辑
          </Button>
        ]}
        width={1200}
        destroyOnClose
      >
        {previewBanner && (
          <div>
            {/* 标题区域 */}
            <div style={{ marginBottom: '20px' }}>
              <h2 style={{ marginBottom: '8px', fontSize: '24px' }}>
                {previewBanner.title}
              </h2>
              {previewBanner.subtitle && (
                <p style={{ fontSize: '16px', color: '#666', marginBottom: '12px' }}>
                  {previewBanner.subtitle}
                </p>
              )}
              <div style={{ color: '#999', fontSize: '14px' }}>
                <span style={{ marginRight: '16px' }}>
                  查看次数: {previewBanner.viewCount}
                </span>
                <span>
                  创建时间: {new Date(previewBanner.createdTime).toLocaleString()}
                </span>
              </div>
            </div>

            {/* 封面图片 */}
            {previewBanner.coverImageUrl && (
              <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <Image
                  src={previewBanner.coverImageUrl}
                  alt={previewBanner.title}
                  style={{ 
                    maxWidth: '100%', 
                    maxHeight: '300px',
                    borderRadius: '8px'
                  }}
                />
              </div>
            )}

            {/* 内容预览 */}
            <Tabs defaultActiveKey="1">
              <TabPane tab="Markdown原始内容" key="1">
                <div style={{ 
                  maxHeight: '400px', 
                  overflow: 'auto',
                  padding: '16px',
                  border: '1px solid #f0f0f0',
                  borderRadius: '6px',
                  backgroundColor: '#f8f8f8'
                }}>
                  <pre style={{ 
                    margin: 0, 
                    whiteSpace: 'pre-wrap',
                    fontFamily: 'Monaco, Consolas, monospace',
                    fontSize: '13px',
                    lineHeight: '1.5',
                    color: '#333'
                  }}>
                    {previewBanner.contentMarkdown || '暂无Markdown内容'}
                  </pre>
                </div>
              </TabPane>
              <TabPane tab="渲染预览" key="2">
                <div 
                  style={{ 
                    maxHeight: '400px', 
                    overflow: 'auto',
                    padding: '16px',
                    border: '1px solid #f0f0f0',
                    borderRadius: '6px',
                    backgroundColor: '#fafafa'
                  }}
                >
                  {previewBanner.contentMarkdown ? (
                    <div 
                      dangerouslySetInnerHTML={renderMarkdown(previewBanner.contentMarkdown)}
                      className="markdown-content"
                      style={{
                        fontSize: '14px',
                        lineHeight: '1.6'
                      }}
                    />
                  ) : previewBanner.contentHtml ? (
                    <div 
                      dangerouslySetInnerHTML={{ __html: previewBanner.contentHtml }}
                      className="content-html"
                    />
                  ) : (
                    <Text type="secondary">暂无内容</Text>
                  )}
                </div>
              </TabPane>
            </Tabs>

            {/* 跳转链接 */}
            {previewBanner.jumpUrl && (
              <div style={{ marginTop: '16px' }}>
                <Text strong>跳转链接: </Text>
                <a href={previewBanner.jumpUrl} target="_blank" rel="noopener noreferrer">
                  {previewBanner.jumpUrl}
                </a>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default CarouselManage;

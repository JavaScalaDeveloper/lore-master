import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Typography, Image, message, Space, Modal } from 'antd';
import { ArrowLeftOutlined, EyeOutlined, CalendarOutlined, EditOutlined } from '@ant-design/icons';
import { marked } from 'marked';
import { adminApi } from '../../../../utils/request';
import './CarouselDetail.css';

const { Title, Paragraph, Text } = Typography;

interface CarouselBannerDetail {
  bannerId: string;
  title: string;
  subtitle?: string;
  coverImageUrl: string;
  contentMarkdown?: string;
  contentHtml?: string;
  jumpUrl?: string;
  viewCount: number;
  createdTime: string;
  updatedTime: string;
}

const CarouselDetail: React.FC = () => {
  const { bannerId } = useParams<{ bannerId: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState<CarouselBannerDetail | null>(null);
  const [editModalVisible, setEditModalVisible] = useState(false);

  useEffect(() => {
    if (bannerId) {
      loadCarouselDetail(bannerId);
      // 增加查看次数
      incrementViewCount(bannerId);
    }
  }, [bannerId]);

  const loadCarouselDetail = async (id: string) => {
    try {
      setLoading(true);
      const response = await adminApi.post('/api/admin/carousel/detail', { bannerId: id });
      if (response.success) {
        setDetail(response.data);
      } else {
        message.error('获取详情失败: ' + response.message);
      }
    } catch (error) {
      console.error('获取轮播图详情失败:', error);
      message.error('获取详情失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const incrementViewCount = async (id: string) => {
    try {
      await adminApi.post(`/api/admin/carousel/view?bannerId=${id}`);
    } catch (error) {
      console.error('更新查看次数失败:', error);
    }
  };

  const handleBack = () => {
    navigate(-1);
  };

  const handleEdit = () => {
    setEditModalVisible(true);
  };

  const renderMarkdown = (markdown: string) => {
    const html = marked(markdown);
    return { __html: html };
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '400px' 
      }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!detail) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Title level={3}>内容不存在</Title>
        <Button type="primary" onClick={handleBack}>
          返回
        </Button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '20px' }}>
      {/* 操作按钮 */}
      <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between' }}>
        <Button 
          icon={<ArrowLeftOutlined />} 
          onClick={handleBack}
          type="text"
        >
          返回
        </Button>
        <Button 
          type="primary"
          icon={<EditOutlined />} 
          onClick={handleEdit}
        >
          编辑
        </Button>
      </div>

      {/* 主要内容 */}
      <Card>
        {/* 标题区域 */}
        <div style={{ marginBottom: '24px' }}>
          <Title level={1} style={{ marginBottom: '8px' }}>
            {detail.title}
          </Title>
          {detail.subtitle && (
            <Paragraph style={{ fontSize: '16px', color: '#666', marginBottom: '16px' }}>
              {detail.subtitle}
            </Paragraph>
          )}
          
          {/* 元信息 */}
          <Space size="large" style={{ color: '#999', fontSize: '14px' }}>
            <span>
              <CalendarOutlined style={{ marginRight: '4px' }} />
              {formatDate(detail.createdTime)}
            </span>
            <span>
              <EyeOutlined style={{ marginRight: '4px' }} />
              {detail.viewCount} 次查看
            </span>
          </Space>
        </div>

        {/* 封面图片 */}
        {detail.coverImageUrl && (
          <div style={{ marginBottom: '24px', textAlign: 'center' }}>
            <Image
              src={detail.coverImageUrl}
              alt={detail.title}
              style={{ 
                maxWidth: '100%', 
                maxHeight: '400px',
                borderRadius: '8px'
              }}
              fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMIAAADDCAYAAADQvc6UAAABRWlDQ1BJQ0MgUHJvZmlsZQAAKJFjYGASSSwoyGFhYGDIzSspCnJ3UoiIjFJgf8LAwSDCIMogwMCcmFxc4BgQ4ANUwgCjUcG3awyMIPqyLsis7PPOq3QdDFcvjV3jOD1boQVTPQrgSkktTgbSf4A4LbmgqISBgTEFyFYuLykAsTuAbJEioKOA7DkgdjqEvQHEToKwj4DVhAQ5A9k3gGyB5IxEoBmML4BsnSQk8XQkNtReEOBxcfXxUQg1Mjc0dyHgXNJBSWpFCYh2zi+oLMpMzyhRcASGUqqCZ16yno6CkYGRAQMDKMwhqj/fAIcloxgHQqxAjIHBEugw5sUIsSQpBobtQPdLciLEVJYzMPBHMDBsayhILEqEO4DxG0txmrERhM29nYGBddr//5/DGRjYNRkY/l7////39v///y4Dmn+LgeHANwDrkl1AuO+pmgAAADhlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAAqACAAQAAAABAAAAwqADAAQAAAABAAAAwwAAAAD9b/HnAAAHlklEQVR4Ae3dP3Ik1RnG4W+FgYxN"
            />
          </div>
        )}

        {/* 内容区域 */}
        <div 
          className="carousel-detail-content"
          style={{ lineHeight: '1.8' }}
        >
          {detail.contentMarkdown ? (
            <div>
              <h3 style={{ marginBottom: '16px', color: '#333' }}>Markdown 原始内容</h3>
              <div style={{
                padding: '16px',
                backgroundColor: '#f8f8f8',
                border: '1px solid #e8e8e8',
                borderRadius: '6px',
                marginBottom: '24px'
              }}>
                <pre style={{ 
                  margin: 0, 
                  whiteSpace: 'pre-wrap',
                  fontFamily: 'Monaco, Consolas, monospace',
                  fontSize: '13px',
                  lineHeight: '1.5',
                  color: '#333'
                }}>
                  {detail.contentMarkdown}
                </pre>
              </div>
              
              <h3 style={{ marginBottom: '16px', color: '#333' }}>渲染预览</h3>
              <div 
                dangerouslySetInnerHTML={renderMarkdown(detail.contentMarkdown)}
                className="markdown-content"
                style={{
                  fontSize: '16px',
                  lineHeight: '1.8',
                  color: '#333',
                  padding: '16px',
                  backgroundColor: '#fafafa',
                  border: '1px solid #f0f0f0',
                  borderRadius: '6px'
                }}
              />
            </div>
          ) : detail.contentHtml ? (
            <div 
              dangerouslySetInnerHTML={{ __html: detail.contentHtml }}
              className="content-html"
            />
          ) : (
            <Text type="secondary">暂无详细内容</Text>
          )}
        </div>

        {/* 跳转链接 */}
        {detail.jumpUrl && (
          <div style={{ marginTop: '24px', textAlign: 'center' }}>
            <Button 
              type="primary" 
              size="large"
              onClick={() => window.open(detail.jumpUrl, '_blank')}
            >
              了解更多
            </Button>
          </div>
        )}
      </Card>

      {/* 编辑模态框 */}
      <Modal
        title="编辑轮播图"
        open={editModalVisible}
        onCancel={() => setEditModalVisible(false)}
        footer={null}
        width={800}
        destroyOnClose
      >
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Text type="secondary">编辑功能将在管理页面中打开</Text>
          <div style={{ marginTop: '20px' }}>
            <Button 
              type="primary"
              onClick={() => {
                setEditModalVisible(false);
                navigate('/admin/carousel');
              }}
            >
              前往管理页面
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CarouselDetail;

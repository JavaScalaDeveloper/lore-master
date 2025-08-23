import React from 'react';
import { Card, Typography, Space, Divider } from 'antd';
import CarouselBanner from '../../components/CarouselBanner';

const { Title, Paragraph, Text } = Typography;

const CarouselTest: React.FC = () => {
  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <Title level={2}>轮播图功能测试</Title>
      
      {/* 轮播图展示 */}
      <Card style={{ marginBottom: '24px' }}>
        <CarouselBanner height={400} />
      </Card>

      {/* 功能说明 */}
      <Card title="功能说明">
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Title level={4}>🎯 核心功能</Title>
            <ul>
              <li><strong>轮播展示</strong>：自动轮播，支持手动切换</li>
              <li><strong>点击跳转</strong>：点击轮播图可跳转到详情页面</li>
              <li><strong>查看统计</strong>：自动统计查看次数</li>
              <li><strong>Markdown支持</strong>：详情内容支持Markdown格式</li>
            </ul>
          </div>

          <Divider />

          <div>
            <Title level={4}>📊 数据库表结构</Title>
            <Text code>consumer_carousel_banner</Text>
            <Paragraph style={{ marginTop: '8px' }}>
              包含以下核心字段：
            </Paragraph>
            <ul>
              <li><Text code>banner_id</Text> - 轮播图唯一标识</li>
              <li><Text code>title</Text> - 标题</li>
              <li><Text code>subtitle</Text> - 副标题</li>
              <li><Text code>cover_image_url</Text> - 封面图片</li>
              <li><Text code>content_markdown</Text> - Markdown内容</li>
              <li><Text code>content_html</Text> - HTML内容（自动转换）</li>
              <li><Text code>view_count</Text> - 查看次数</li>
              <li><Text code>sort_order</Text> - 排序顺序</li>
              <li><Text code>status</Text> - 状态（ACTIVE/INACTIVE）</li>
            </ul>
          </div>

          <Divider />

          <div>
            <Title level={4}>🔧 API接口</Title>
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>
                <Text strong>获取轮播图列表：</Text>
                <br />
                <Text code>GET /api/carousel/list</Text>
              </div>
              <div>
                <Text strong>获取轮播图详情：</Text>
                <br />
                <Text code>GET /api/carousel/detail?bannerId=xxx</Text>
              </div>
              <div>
                <Text strong>增加查看次数：</Text>
                <br />
                <Text code>POST /api/carousel/view?bannerId=xxx</Text>
              </div>
            </Space>
          </div>

          <Divider />

          <div>
            <Title level={4}>🎨 使用方式</Title>
            <Paragraph>
              <strong>1. 在主页中使用轮播图组件：</strong>
            </Paragraph>
            <Text code style={{ display: 'block', padding: '12px', backgroundColor: '#f5f5f5' }}>
{`import CarouselBanner from '../components/CarouselBanner';

// 在组件中使用
<CarouselBanner 
  height={400} 
  autoplay={true} 
  dots={true} 
/>`}
            </Text>

            <Paragraph style={{ marginTop: '16px' }}>
              <strong>2. 添加路由配置：</strong>
            </Paragraph>
            <Text code style={{ display: 'block', padding: '12px', backgroundColor: '#f5f5f5' }}>
{`// 在路由配置中添加详情页路由
{
  path: '/carousel/:bannerId',
  element: <CarouselDetail />
}`}
            </Text>
          </div>

          <Divider />

          <div>
            <Title level={4}>📝 建表SQL</Title>
            <Paragraph>
              请执行以下SQL文件来创建数据库表：
            </Paragraph>
            <Text code>lore-master-commands/sql/mysql/carousel-banner.sql</Text>
            <Paragraph style={{ marginTop: '8px', color: '#666' }}>
              该文件包含了完整的表结构定义和示例数据。
            </Paragraph>
          </div>

          <Divider />

          <div>
            <Title level={4">✨ 特色功能</Title>
            <ul>
              <li><strong>自动Markdown转HTML</strong>：保存时自动将Markdown转换为HTML</li>
              <li><strong>响应式设计</strong>：适配不同屏幕尺寸</li>
              <li><strong>优雅降级</strong>：图片加载失败时显示占位符</li>
              <li><strong>SEO友好</strong>：支持搜索引擎优化</li>
              <li><strong>性能优化</strong>：懒加载和缓存机制</li>
            </ul>
          </div>
        </Space>
      </Card>
    </div>
  );
};

export default CarouselTest;

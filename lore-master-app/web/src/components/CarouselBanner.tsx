import React, { useEffect, useState } from 'react';
import { Carousel, Card, Typography, Space, Button } from 'antd';
import { EyeOutlined, ArrowRightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { consumerApi } from '../utils/request';

const { Title, Paragraph } = Typography;

interface CarouselBannerItem {
  bannerId: string;
  title: string;
  subtitle?: string;
  coverImageUrl: string;
  jumpUrl?: string;
  viewCount: number;
  createdTime: string;
}

interface CarouselBannerProps {
  height?: number;
  autoplay?: boolean;
  dots?: boolean;
}

const CarouselBanner: React.FC<CarouselBannerProps> = ({
  height = 400,
  autoplay = true,
  dots = true
}) => {
  const navigate = useNavigate();
  const [banners, setBanners] = useState<CarouselBannerItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCarouselBanners();
  }, []);

  const loadCarouselBanners = async () => {
    try {
      setLoading(true);
      const response = await consumerApi.get('/api/carousel/list');
      if (response.success) {
        setBanners(response.data || []);
      }
    } catch (error) {
      console.error('获取轮播图失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBannerClick = (banner: CarouselBannerItem) => {
    if (banner.jumpUrl) {
      // 如果有外部跳转链接，直接跳转
      window.open(banner.jumpUrl, '_blank');
    } else {
      // 否则跳转到详情页
      navigate(`/carousel/${banner.bannerId}`);
    }
  };

  if (loading) {
    return (
      <div style={{ height, backgroundColor: '#f5f5f5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div>加载中...</div>
      </div>
    );
  }

  if (banners.length === 0) {
    return (
      <div style={{ height, backgroundColor: '#f5f5f5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div>暂无轮播内容</div>
      </div>
    );
  }

  return (
    <div style={{ position: 'relative' }}>
      <Carousel
        autoplay={autoplay}
        dots={dots}
        effect="fade"
        style={{ height }}
      >
        {banners.map((banner) => (
          <div key={banner.bannerId}>
            <div
              style={{
                height,
                backgroundImage: `linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url(${banner.coverImageUrl})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                position: 'relative',
                cursor: 'pointer'
              }}
              onClick={() => handleBannerClick(banner)}
            >
              {/* 内容覆盖层 */}
              <div
                style={{
                  position: 'absolute',
                  bottom: 0,
                  left: 0,
                  right: 0,
                  background: 'linear-gradient(transparent, rgba(0,0,0,0.7))',
                  padding: '40px 20px 20px',
                  color: 'white'
                }}
              >
                <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
                  <Title 
                    level={2} 
                    style={{ 
                      color: 'white', 
                      marginBottom: '8px',
                      fontSize: '28px',
                      fontWeight: 'bold'
                    }}
                  >
                    {banner.title}
                  </Title>
                  
                  {banner.subtitle && (
                    <Paragraph 
                      style={{ 
                        color: 'rgba(255,255,255,0.9)', 
                        fontSize: '16px',
                        marginBottom: '16px',
                        maxWidth: '600px'
                      }}
                    >
                      {banner.subtitle}
                    </Paragraph>
                  )}
                  
                  <Space size="large" style={{ color: 'rgba(255,255,255,0.8)' }}>
                    <span>
                      <EyeOutlined style={{ marginRight: '4px' }} />
                      {banner.viewCount} 次查看
                    </span>
                    <Button 
                      type="primary" 
                      icon={<ArrowRightOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleBannerClick(banner);
                      }}
                    >
                      查看详情
                    </Button>
                  </Space>
                </div>
              </div>
            </div>
          </div>
        ))}
      </Carousel>
    </div>
  );
};

export default CarouselBanner;

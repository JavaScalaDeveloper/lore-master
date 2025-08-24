import { View, Text, Button } from '@tarojs/components';
import { useState } from 'react';
import { get, post } from '../../utils/request';
import './index.css';

// 轮播图数据类型
interface CarouselBanner {
  bannerId: string;
  title: string;
  subtitle?: string;
  coverImageUrl: string;
  jumpUrl?: string;
  viewCount: number;
  createdTime: string;
}

export default function CarouselTest() {
  const [banners, setBanners] = useState<CarouselBanner[]>([]);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<string>('');

  // 测试获取轮播图列表
  const testGetBanners = async () => {
    try {
      setLoading(true);
      setResult('正在获取轮播图列表...');
      
      const response = await get<CarouselBanner[]>('/api/carousel/list');
      
      if (response.success && response.data) {
        setBanners(response.data);
        setResult(`✅ 成功获取 ${response.data.length} 个轮播图`);
      } else {
        setResult(`❌ 获取失败: ${response.message}`);
      }
    } catch (error) {
      console.error('测试获取轮播图失败:', error);
      setResult(`❌ 网络错误: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  // 测试获取轮播图详情
  const testGetDetail = async (bannerId: string) => {
    try {
      setLoading(true);
      setResult(`正在获取轮播图详情: ${bannerId}`);
      
      const response = await get(`/api/carousel/detail?bannerId=${bannerId}`);
      
      if (response.success && response.data) {
        setResult(`✅ 成功获取详情: ${response.data.title}`);
      } else {
        setResult(`❌ 获取详情失败: ${response.message}`);
      }
    } catch (error) {
      console.error('测试获取详情失败:', error);
      setResult(`❌ 网络错误: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  // 测试增加查看次数
  const testIncrementView = async (bannerId: string) => {
    try {
      setLoading(true);
      setResult(`正在增加查看次数: ${bannerId}`);
      
      const response = await post(`/api/carousel/view?bannerId=${bannerId}`);
      
      if (response.success) {
        setResult(`✅ 成功增加查看次数`);
        // 重新获取列表以查看更新后的数据
        await testGetBanners();
      } else {
        setResult(`❌ 增加查看次数失败: ${response.message}`);
      }
    } catch (error) {
      console.error('测试增加查看次数失败:', error);
      setResult(`❌ 网络错误: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View className='carousel-test'>
      <View className='header'>
        <Text className='title'>轮播图API测试</Text>
      </View>

      <View className='test-section'>
        <Text className='section-title'>🧪 API测试</Text>
        
        <View className='test-buttons'>
          <Button 
            className='test-button'
            onClick={testGetBanners}
            disabled={loading}
          >
            {loading ? '测试中...' : '获取轮播图列表'}
          </Button>
        </View>

        {result && (
          <View className='result-section'>
            <Text className='result-text'>{result}</Text>
          </View>
        )}
      </View>

      {banners.length > 0 && (
        <View className='banners-section'>
          <Text className='section-title'>📋 轮播图数据</Text>
          
          {banners.map((banner, index) => (
            <View key={banner.bannerId} className='banner-item'>
              <View className='banner-header'>
                <Text className='banner-title'>{banner.title}</Text>
                <Text className='banner-id'>ID: {banner.bannerId}</Text>
              </View>
              
              {banner.subtitle && (
                <Text className='banner-subtitle'>{banner.subtitle}</Text>
              )}
              
              <View className='banner-meta'>
                <Text className='meta-item'>👁 {banner.viewCount} 次查看</Text>
                <Text className='meta-item'>📅 {new Date(banner.createdTime).toLocaleDateString()}</Text>
              </View>
              
              <View className='banner-actions'>
                <Button 
                  className='action-button detail-button'
                  size='mini'
                  onClick={() => testGetDetail(banner.bannerId)}
                  disabled={loading}
                >
                  获取详情
                </Button>
                <Button 
                  className='action-button view-button'
                  size='mini'
                  onClick={() => testIncrementView(banner.bannerId)}
                  disabled={loading}
                >
                  增加查看
                </Button>
              </View>
            </View>
          ))}
        </View>
      )}

      <View className='info-section'>
        <Text className='section-title'>ℹ️ 测试说明</Text>
        <View className='info-content'>
          <Text className='info-text'>• 点击"获取轮播图列表"测试API连接</Text>
          <Text className='info-text'>• 点击"获取详情"测试详情API</Text>
          <Text className='info-text'>• 点击"增加查看"测试查看次数更新</Text>
          <Text className='info-text'>• 确保后端服务已启动 (localhost:8082)</Text>
        </View>
      </View>
    </View>
  );
}

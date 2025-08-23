import { View, Text, Input, Swiper, SwiperItem, Image } from '@tarojs/components';
import { useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
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

// API响应类型
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export default function Index() {
  const [searchValue, setSearchValue] = useState('');
  const [recommendations, setRecommendations] = useState<CarouselBanner[]>([]);
  const [loading, setLoading] = useState(true);

  // 获取轮播图数据
  const loadCarouselBanners = async () => {
    try {
      setLoading(true);
      const response = await get<CarouselBanner[]>('/api/carousel/list');

      if (response.success && response.data) {
        setRecommendations(response.data);
      } else {
        console.error('获取轮播图失败:', response.message);
        // 使用默认数据作为降级方案
        setRecommendations([
          {
            bannerId: 'default_1',
            title: '🎯 个性化学习计划',
            subtitle: '根据你的水平定制专属学习路径',
            coverImageUrl: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=400&h=200&fit=crop&crop=center',
            viewCount: 0,
            createdTime: new Date().toISOString()
          },
          {
            bannerId: 'default_2',
            title: '🏆 挑战模式',
            subtitle: '与全球学习者一起竞技成长',
            coverImageUrl: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=200&fit=crop&crop=center',
            viewCount: 0,
            createdTime: new Date().toISOString()
          },
          {
            bannerId: 'default_3',
            title: '📚 知识宝库',
            subtitle: '海量优质学习资源等你探索',
            coverImageUrl: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop&crop=center',
            viewCount: 0,
            createdTime: new Date().toISOString()
          }
        ]);
      }
    } catch (error) {
      console.error('获取轮播图数据失败:', error);
      // 网络错误时使用默认数据
      setRecommendations([
        {
          bannerId: 'fallback_1',
          title: '🎯 个性化学习计划',
          subtitle: '根据你的水平定制专属学习路径',
          coverImageUrl: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=400&h=200&fit=crop&crop=center',
          viewCount: 0,
          createdTime: new Date().toISOString()
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  // 页面加载时获取数据
  useEffect(() => {
    loadCarouselBanners();
  }, []);

  // 功能图标数据
  const features = [
    { id: 1, title: '开始测评', subtitle: '了解你的水平', icon: '🎯' },
    { id: 2, title: '指定目标', subtitle: '制定学习计划', icon: '🎪' },
  ];

  // 推荐课程数据
  const courses = [
    {
      id: 1,
      title: '🇬🇧 英语基础课程',
      description: '从零开始，轻松掌握英语基础知识，包含发音、语法、词汇等核心内容',
      image: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=100&h=100&fit=crop&crop=center',
      level: '初级',
      duration: '30天',
      students: '1.2万'
    },
    {
      id: 2,
      title: '🔢 数学提高课程',
      description: '系统提升数学解题能力，涵盖代数、几何、概率等重要知识点',
      image: 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=100&h=100&fit=crop&crop=center',
      level: '中级',
      duration: '45天',
      students: '8.5千'
    },
    {
      id: 3,
      title: '💻 编程入门课程',
      description: '零基础学编程，掌握编程思维和基础语法，开启技术之路',
      image: 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=100&h=100&fit=crop&crop=center',
      level: '入门',
      duration: '60天',
      students: '2.1万'
    },
  ];

  // 处理轮播图点击
  const handleBannerClick = async (banner: CarouselBanner) => {
    try {
      // 增加查看次数
      await post(`/api/carousel/view?bannerId=${banner.bannerId}`, {}, {
        'Content-Type': 'application/x-www-form-urlencoded'
      });
    } catch (error) {
      console.error('更新查看次数失败:', error);
    }

    if (banner.jumpUrl) {
      // 如果有外部跳转链接，显示确认对话框
      Taro.showModal({
        title: '跳转确认',
        content: `即将跳转到外部链接：${banner.jumpUrl}`,
        success: (res) => {
          if (res.confirm) {
            // 小程序中无法直接打开外部链接，可以复制到剪贴板
            Taro.setClipboardData({
              data: banner.jumpUrl || '',
              success: () => {
                Taro.showToast({
                  title: '链接已复制到剪贴板',
                  icon: 'success'
                });
              }
            });
          }
        }
      });
    } else {
      // 跳转到详情页面
      Taro.navigateTo({
        url: `/pages/carousel-detail/index?bannerId=${banner.bannerId}`
      });
    }
  };

  // 处理搜索
  const handleSearch = (e: any) => {
    setSearchValue(e.detail.value);
  };

  // 处理功能点击
  const handleFeatureClick = (feature) => {
    if (feature.id === 1) {
      // 开始测评 - 导航到聊天页面
      Taro.navigateTo({
        url: '/pages/chat/chat'
      });
    } else {
      Taro.showToast({
        title: `点击了${feature.title}`,
        icon: 'success',
        duration: 1500
      });
    }
  };

  // 处理课程点击
  const handleCourseClick = (course) => {
    Taro.showModal({
      title: course.title,
      content: `${course.description}\n\n难度：${course.level}\n时长：${course.duration}\n学员：${course.students}人`,
      showCancel: true,
      cancelText: '稍后再看',
      confirmText: '立即学习',
      success: (res) => {
        if (res.confirm) {
          Taro.showToast({
            title: '即将跳转到课程页面',
            icon: 'success'
          });
        }
      }
    });
  };

  return (
    <View className='index'>
      {/* 顶部搜索框 */}
      <View className='search-container'>
        <Input
          className='search-input'
          placeholder='🔍 搜索课程、知识点...'
          type='text'
          value={searchValue}
          onInput={handleSearch}
        />
      </View>

      {/* Swiper滑动视图卡片 */}
      <View className='card'>
        <View className='card-title'>✨ 精选推荐</View>
        {loading ? (
          <View className='loading-container' style={{
            height: '200px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#999'
          }}>
            <Text style={{ fontSize: '20px' }}>加载中...</Text>
          </View>
        ) : recommendations.length > 0 ? (
          <Swiper
            className='swiper'
            indicatorDots
            autoplay
            interval={5000}
            duration={500}
            indicatorColor='rgba(255, 255, 255, 0.5)'
            indicatorActiveColor='#ffffff'
          >
            {recommendations.map(item => (
              <SwiperItem key={item.bannerId}>
                <View
                  className='swiper-item'
                  onClick={() => handleBannerClick(item)}
                >
                  <Image src={item.coverImageUrl} className='swiper-image' mode='aspectFill' />
                  <View className='swiper-content'>
                    <Text className='swiper-title'>{item.title}</Text>
                    <Text className='swiper-subtitle'>{item.subtitle}</Text>
                  </View>
                </View>
              </SwiperItem>
            ))}
          </Swiper>
        ) : (
          <View className='empty-container' style={{
            height: '200px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#999'
          }}>
            <Text style={{ fontSize: '20px' }}>暂无推荐内容</Text>
          </View>
        )}
      </View>

      {/* 功能图标卡片 */}
      <View className='card'>
        <View className='card-title'>🚀 学习功能</View>
        <View className='features-container'>
          {features.map(feature => (
            <View
              className='feature-item'
              key={feature.id}
              onClick={() => handleFeatureClick(feature)}
            >
              <Text className='feature-icon'>{feature.icon}</Text>
              <Text className='feature-title'>{feature.title}</Text>
              <Text className='feature-subtitle'>{feature.subtitle}</Text>
            </View>
          ))}
        </View>
      </View>

      {/* 推荐课程列表卡片 */}
      <View className='card'>
        <View className='card-title'>📚 热门课程</View>
        <View className='courses-container'>
          {courses.map(course => (
            <View
              className='course-item'
              key={course.id}
              onClick={() => handleCourseClick(course)}
            >
              <Image src={course.image} className='course-image' mode='aspectFill' />
              <View className='course-info'>
                <Text className='course-title'>{course.title}</Text>
                <Text className='course-description'>{course.description}</Text>
                <View className='course-meta'>
                  <Text className='course-level'>{course.level}</Text>
                  <Text className='course-duration'>{course.duration}</Text>
                  <Text className='course-students'>{course.students}人学习</Text>
                </View>
              </View>
            </View>
          ))}
        </View>
      </View>
    </View>
  );
}

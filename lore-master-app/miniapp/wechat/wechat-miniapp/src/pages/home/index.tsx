import { View, Text, Input, Swiper, SwiperItem, Image, Button } from '@tarojs/components';
import { useEffect, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
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


// 最近学习课程数据类型
interface RecentLearningCourse {
  id: number;
  courseCode: string;
  title: string;
  description: string;
  author: string;
  courseType: string;
  contentType?: string; // 添加内容类型属性
  coverImageUrl: string;
  difficultyLevel: string;
  estimatedMinutes: number;
  viewCount: number;
  likeCount: number;
  collectCount: number;
  status: string;
  learningDuration: number;
  progressPercent: number;
  isCompleted: number;
  lastLearningDate: string;
  learningRecordCreatedTime: string;
  learningRecordUpdatedTime: string;
}

export default function Index() {
  const [searchValue, setSearchValue] = useState('');
  const [recommendations, setRecommendations] = useState<CarouselBanner[]>([]);
  const [recentCourses, setRecentCourses] = useState<RecentLearningCourse[]>([]);
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
  // 获取用户最近学习的课程
  const loadRecentLearningCourses = async () => {
    try {
      const response = await post<RecentLearningCourse[]>('/api/consumer/course/getRecentLearningCourses', {});

      if (response.success && response.data) {
        setRecentCourses(response.data);
        console.log('获取最近学习课程成功:', response.data);
      } else {
        console.error('获取最近学习课程失败:', response.message);
        // 如果是用户未登录，不显示错误提示
        if (response.message && response.message.includes('用户未登录')) {
          console.log('用户未登录，显示空状态');
        }
        setRecentCourses([]);
      }
    } catch (error) {
      console.error('获取最近学习课程异常:', error);
      // 如果是登录过期的错误，不需要额外处理，request.ts已经处理了
      if (error.message && error.message.includes('登录已过期')) {
        return;
      }
      setRecentCourses([]);
    }
  };

  useEffect(() => {
    loadCarouselBanners();
    loadRecentLearningCourses();
  }, []);

  // 页面显示时刷新最近学习记录
  useDidShow(() => {
    console.log('页面显示，刷新最近学习记录');
    loadRecentLearningCourses();
  });

  // 功能图标数据
  const features = [
    { id: 1, title: '开始测评', subtitle: '了解你的水平', icon: '🎯' },
    { id: 2, title: '指定目标', subtitle: '制定学习计划', icon: '🎪' },
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

  // 处理搜索输入
  const handleSearchInput = (e: any) => {
    setSearchValue(e.detail.value);
  };

  // 处理搜索提交
  const handleSearch = () => {
    const keyword = searchValue.trim();
    if (keyword) {
      // 跳转到学习页面并传递搜索关键词
      Taro.switchTab({
        url: '/pages/study/study'
      }).then(() => {
        // 使用事件总线传递搜索关键词到学习页面
        Taro.eventCenter.trigger('searchFromHome', keyword);
      });
    } else {
      Taro.showToast({
        title: '请输入搜索内容',
        icon: 'none',
        duration: 1500
      });
    }
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



  // 处理最近学习课程点击
  const handleRecentCourseClick = (course: RecentLearningCourse) => {
    // 添加详细的调试信息
    console.log('点击课程详情:', {
      courseCode: course.courseCode,
      title: course.title,
      courseType: course.courseType,
      contentType: course.contentType,
      fullCourse: course
    });
    
    const learningTimeText = formatLearningTime(course.learningDuration);
    const lastLearningText = formatDate(course.lastLearningDate);

    Taro.showModal({
      title: course.title,
      content: `${course.description}\n\n作者：${course.author}\n难度：${course.difficultyLevel}\n学习时长：${learningTimeText}\n最近学习：${lastLearningText}`,
      showCancel: true,
      cancelText: '稍后再看',
      confirmText: '继续学习',
      success: (res) => {
        if (res.confirm) {
          // 根据课程类型和内容类型跳转到对应页面
          try {
            let url = ''

            console.log('开始构建跳转URL，courseType:', course.courseType, 'contentType:', course.contentType);

            if (course.courseType === 'COLLECTION') {
              url = `/pages/course/collection/collection?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
              console.log('跳转到合集页面:', url);
            } else if (course.courseType === 'NORMAL') {
              // NORMAL类型的课程，根据描述内容判断是文章还是视频
              // 先默认跳转到文章页面，因为从调试信息看大部分是文章内容
              if (course.contentType === 'VIDEO') {
                url = `/pages/course/video/video?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
                console.log('NORMAL课程跳转到视频页面:', url);
              } else {
                // 默认跳转到文章页面（包括contentType为ARTICLE或未定义的情况）
                url = `/pages/course/article/article?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
                console.log('NORMAL课程跳转到文章页面:', url);
              }
            } else if (course.contentType === 'ARTICLE') {
              url = `/pages/course/article/article?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
              console.log('跳转到文章页面:', url);
            } else if (course.contentType === 'VIDEO') {
              url = `/pages/course/video/video?courseCode=${course.courseCode}&title=${encodeURIComponent(course.title)}`
              console.log('跳转到视频页面:', url);
            } else {
              console.log('无法识别的课程类型:', {
                courseType: course.courseType,
                contentType: course.contentType
              });
              Taro.showToast({
                title: '暂不支持此类型内容',
                icon: 'none'
              })
              return
            }

            console.log('跳转到课程页面:', url)
            Taro.navigateTo({
              url,
              success: () => {
                console.log('跳转成功')
              },
              fail: (err) => {
                console.error('跳转失败:', err)
                Taro.showToast({
                  title: '跳转失败，请重试',
                  icon: 'error'
                })
              }
            })
          } catch (error) {
            console.error('处理跳转时发生错误:', error)
            Taro.showToast({
              title: '发生错误，请重试',
              icon: 'error'
            })
          }
        }
      }
    });
  };

  // 格式化学习时长
  const formatLearningTime = (seconds: number): string => {
    if (seconds < 60) {
      return `${seconds}秒`;
    } else if (seconds < 3600) {
      const minutes = Math.floor(seconds / 60);
      return `${minutes}分钟`;
    } else {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      return `${hours}小时${minutes}分钟`;
    }
  };

  // 格式化日期
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();

    // 获取日期部分（忽略时间）
    const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const nowOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    const diffTime = nowOnly.getTime() - dateOnly.getTime();
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return '今天';
    } else if (diffDays === 1) {
      return '昨天';
    } else if (diffDays < 7) {
      return `${diffDays}天前`;
    } else {
      return date.toLocaleDateString('zh-CN');
    }
  };

  return (
    <View className='index'>
      {/* 顶部搜索框 */}
      <View className='search-container'>
        <View className='search-box'>
          <Input
            className='search-input'
            placeholder='搜索课程、知识点...'
            type='text'
            value={searchValue}
            onInput={handleSearchInput}
            onConfirm={handleSearch}
          />
          <Button
            className='search-btn'
            onClick={handleSearch}
          >
            🔍 搜索
          </Button>
        </View>
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

      {/* 最近学习课程列表卡片 */}
      <View className='card'>
        <View className='card-title'>📚 最近学习</View>
        {recentCourses.length > 0 ? (
          <View className='courses-container'>
            {recentCourses.map(course => (
              <View
                className='course-item'
                key={course.id}
                onClick={() => handleRecentCourseClick(course)}
              >
                <Image
                  src={course.coverImageUrl || 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=100&h=100&fit=crop&crop=center'}
                  className='course-image'
                  mode='aspectFill'
                />
                <View className='course-info'>
                  <View className='course-title-row'>
                    <Text className='course-title'>{course.title}</Text>
                    <View className='course-meta-inline'>
                      <Text className='meta-item'>{course.difficultyLevel || '初级'}</Text>
                      <Text className='meta-item'>{formatLearningTime(course.learningDuration)}</Text>
                      <Text className='meta-item'>{formatDate(course.lastLearningDate)}</Text>
                    </View>
                  </View>
                  <Text className='course-description'>{course.description}</Text>
                </View>
              </View>
            ))}
          </View>
        ) : (
          <View className='empty-state'>
            <Text className='empty-text'>暂无学习记录</Text>
            <Text className='empty-hint'>登录后开始学习课程，这里会显示您的学习历史</Text>
            <View
              className='empty-action'
              onClick={() => {
                Taro.switchTab({
                  url: '/pages/profile/profile'
                })
              }}
            >
              <Text className='empty-action-text'>去登录</Text>
            </View>
          </View>
        )}
      </View>
    </View>
  );
}

import { View, Text, Input, Swiper, SwiperItem, Image } from '@tarojs/components';
import { useEffect, useState } from 'react';
import Taro from '@tarojs/taro';
import './index.css';

export default function Index() {
  const [searchValue, setSearchValue] = useState('');

  // 推荐内容数据
  const recommendations = [
    {
      id: 1,
      title: '🎯 个性化学习计划',
      subtitle: '根据你的水平定制专属学习路径',
      image: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=400&h=200&fit=crop&crop=center'
    },
    {
      id: 2,
      title: '🏆 挑战模式',
      subtitle: '与全球学习者一起竞技成长',
      image: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=200&fit=crop&crop=center'
    },
    {
      id: 3,
      title: '📚 知识宝库',
      subtitle: '海量优质学习资源等你探索',
      image: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop&crop=center'
    },
  ];

  // 功能图标数据
  const features = [
    { id: 1, title: '开始测评', subtitle: '了解你的水平', icon: '🎯' },
    { id: 2, title: '指定目标', subtitle: '制定学习计划', icon: '🎪' },
    { id: 3, title: '学习报告', subtitle: '查看进度统计', icon: '📊' },
    { id: 4, title: '学习社区', subtitle: '与他人交流', icon: '👥' },
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

  // 处理搜索
  const handleSearch = (e) => {
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
            <SwiperItem key={item.id}>
              <View className='swiper-item'>
                <Image src={item.image} className='swiper-image' mode='aspectFill' />
                <View className='swiper-content'>
                  <Text className='swiper-title'>{item.title}</Text>
                  <Text className='swiper-subtitle'>{item.subtitle}</Text>
                </View>
              </View>
            </SwiperItem>
          ))}
        </Swiper>
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

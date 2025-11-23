import React, { useState } from 'react';
import './Study.css';

const Study: React.FC = () => {
  const [activeCategory, setActiveCategory] = useState('all');

  const categories = [
    { id: 'all', name: '全部', icon: '📚' },
    { id: 'frontend', name: '前端', icon: '💻' },
    { id: 'backend', name: '后端', icon: '⚙️' },
    { id: 'mobile', name: '移动端', icon: '📱' },
    { id: 'ai', name: '人工智能', icon: '🤖' },
    { id: 'design', name: '设计', icon: '🎨' }
  ];

  const courses = [
    {
      id: 1,
      title: 'HTML5 + CSS3 完整教程',
      category: 'frontend',
      level: '初级',
      duration: '12小时',
      students: '2.3k',
      progress: 65,
      image: 'https://via.placeholder.com/150x100/4A90E2/FFFFFF?text=HTML5'
    },
    {
      id: 2,
      title: 'JavaScript ES6+ 现代开发',
      category: 'frontend',
      level: '中级',
      duration: '18小时',
      students: '1.8k',
      progress: 30,
      image: 'https://via.placeholder.com/150x100/F5A623/FFFFFF?text=JS'
    },
    {
      id: 3,
      title: 'React 全栈开发实战',
      category: 'frontend',
      level: '高级',
      duration: '25小时',
      students: '1.2k',
      progress: 0,
      image: 'https://via.placeholder.com/150x100/7ED321/FFFFFF?text=React'
    },
    {
      id: 4,
      title: 'Node.js 后端开发',
      category: 'backend',
      level: '中级',
      duration: '20小时',
      students: '956',
      progress: 45,
      image: 'https://via.placeholder.com/150x100/50E3C2/FFFFFF?text=Node'
    },
    {
      id: 5,
      title: 'Python 数据分析',
      category: 'ai',
      level: '中级',
      duration: '22小时',
      students: '1.5k',
      progress: 0,
      image: 'https://via.placeholder.com/150x100/BD10E0/FFFFFF?text=Python'
    },
    {
      id: 6,
      title: 'UI/UX 设计基础',
      category: 'design',
      level: '初级',
      duration: '15小时',
      students: '834',
      progress: 20,
      image: 'https://via.placeholder.com/150x100/D0021B/FFFFFF?text=Design'
    }
  ];

  const filteredCourses = activeCategory === 'all' 
    ? courses 
    : courses.filter(course => course.category === activeCategory);

  const getLevelColor = (level: string) => {
    switch (level) {
      case '初级': return '#7ED321';
      case '中级': return '#F5A623';
      case '高级': return '#D0021B';
      default: return '#9B9B9B';
    }
  };

  return (
    <div className="study-container">
      {/* 搜索栏 */}
      <div className="search-section">
        <div className="search-bar">
          <span className="search-icon">🔍</span>
          <input 
            type="text" 
            placeholder="搜索课程、知识点..." 
            className="search-input"
          />
        </div>
      </div>

      {/* 分类导航 */}
      <div className="category-section">
        <div className="category-scroll">
          {categories.map(category => (
            <div
              key={category.id}
              className={`category-item ${activeCategory === category.id ? 'active' : ''}`}
              onClick={() => setActiveCategory(category.id)}
            >
              <span className="category-icon">{category.icon}</span>
              <span className="category-name">{category.name}</span>
            </div>
          ))}
        </div>
      </div>

      {/* 学习进度概览 */}
      <div className="progress-section">
        <div className="progress-card">
          <div className="progress-info">
            <h3>今日学习</h3>
            <p>已学习 2小时30分钟</p>
          </div>
          <div className="progress-circle">
            <div className="circle-progress" style={{ '--progress': '75%' } as React.CSSProperties}>
              <span>75%</span>
            </div>
          </div>
        </div>
      </div>

      {/* 课程列表 */}
      <div className="course-section">
        <div className="section-header">
          <h3>
            {activeCategory === 'all' ? '全部课程' : categories.find(c => c.id === activeCategory)?.name + '课程'}
          </h3>
          <span className="course-count">{filteredCourses.length}门课程</span>
        </div>
        
        <div className="course-grid">
          {filteredCourses.map(course => (
            <div key={course.id} className="course-card">
              <div className="course-image-container">
                <img src={course.image} alt={course.title} className="course-image" />
                {course.progress > 0 && (
                  <div className="progress-overlay">
                    <div className="progress-bar">
                      <div 
                        className="progress-fill" 
                        style={{ width: `${course.progress}%` }}
                      ></div>
                    </div>
                    <span className="progress-text">{course.progress}%</span>
                  </div>
                )}
              </div>
              
              <div className="course-content">
                <h4 className="course-title">{course.title}</h4>
                <div className="course-meta">
                  <span 
                    className="course-level" 
                    style={{ color: getLevelColor(course.level) }}
                  >
                    {course.level}
                  </span>
                  <span className="course-duration">⏱ {course.duration}</span>
                </div>
                <div className="course-stats">
                  <span className="course-students">👥 {course.students}人学习</span>
                </div>
                
                <button className="study-button">
                  {course.progress > 0 ? '继续学习' : '开始学习'}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Study;

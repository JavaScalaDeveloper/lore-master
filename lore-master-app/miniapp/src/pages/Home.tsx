import React from 'react';
import './Home.css';

const Home: React.FC = () => {
  return (
    <div className="home-container">
      {/* 顶部轮播图区域 */}
      <div className="banner-section">
        <div className="banner-item">
          <img 
            src="https://via.placeholder.com/350x150/4A90E2/FFFFFF?text=通学万卷" 
            alt="Banner" 
            className="banner-image"
          />
        </div>
      </div>

      {/* 功能导航区域 */}
      <div className="nav-section">
        <div className="nav-grid">
          <div className="nav-item">
            <div className="nav-icon">📚</div>
            <span className="nav-text">开始学习</span>
          </div>
          <div className="nav-item">
            <div className="nav-icon">📊</div>
            <span className="nav-text">学习统计</span>
          </div>
          <div className="nav-item">
            <div className="nav-icon">🏆</div>
            <span className="nav-text">排行榜</span>
          </div>
          <div className="nav-item">
            <div className="nav-icon">🎯</div>
            <span className="nav-text">目标设定</span>
          </div>
        </div>
      </div>

      {/* 推荐课程区域 */}
      <div className="course-section">
        <div className="section-header">
          <h3>推荐课程</h3>
          <span className="more-link">更多 →</span>
        </div>
        <div className="course-list">
          <div className="course-item">
            <img 
              src="https://via.placeholder.com/120x80/7ED321/FFFFFF?text=课程1" 
              alt="Course" 
              className="course-image"
            />
            <div className="course-info">
              <h4 className="course-title">JavaScript基础入门</h4>
              <p className="course-desc">从零开始学习JavaScript编程</p>
              <div className="course-meta">
                <span className="course-level">初级</span>
                <span className="course-students">1.2k人学习</span>
              </div>
            </div>
          </div>
          
          <div className="course-item">
            <img 
              src="https://via.placeholder.com/120x80/F5A623/FFFFFF?text=课程2" 
              alt="Course" 
              className="course-image"
            />
            <div className="course-info">
              <h4 className="course-title">React开发实战</h4>
              <p className="course-desc">掌握现代前端开发技术</p>
              <div className="course-meta">
                <span className="course-level">中级</span>
                <span className="course-students">856人学习</span>
              </div>
            </div>
          </div>

          <div className="course-item">
            <img 
              src="https://via.placeholder.com/120x80/BD10E0/FFFFFF?text=课程3" 
              alt="Course" 
              className="course-image"
            />
            <div className="course-info">
              <h4 className="course-title">数据结构与算法</h4>
              <p className="course-desc">提升编程思维和解题能力</p>
              <div className="course-meta">
                <span className="course-level">高级</span>
                <span className="course-students">634人学习</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 学习动态区域 */}
      <div className="activity-section">
        <div className="section-header">
          <h3>学习动态</h3>
        </div>
        <div className="activity-list">
          <div className="activity-item">
            <div className="activity-avatar">👤</div>
            <div className="activity-content">
              <p><strong>小明</strong> 完成了 <strong>JavaScript基础入门</strong> 第3章</p>
              <span className="activity-time">2分钟前</span>
            </div>
          </div>
          
          <div className="activity-item">
            <div className="activity-avatar">👤</div>
            <div className="activity-content">
              <p><strong>小红</strong> 获得了 <strong>学习达人</strong> 徽章</p>
              <span className="activity-time">5分钟前</span>
            </div>
          </div>
          
          <div className="activity-item">
            <div className="activity-avatar">👤</div>
            <div className="activity-content">
              <p><strong>小李</strong> 连续学习7天，获得坚持奖励</p>
              <span className="activity-time">10分钟前</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;

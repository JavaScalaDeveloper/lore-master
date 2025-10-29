import React from 'react';
import { Card, Row, Col, Statistic, Progress, Typography, Space, Tag } from 'antd';
import {
  UserOutlined,
  BookOutlined,
  QuestionCircleOutlined,
  TrophyOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons';

const { Title, Paragraph } = Typography;

const Overview: React.FC = () => {
  // 模拟数据
  const stats = {
    totalUsers: 1248,
    totalCareers: 15,
    totalQuestions: 3456,
    totalKnowledge: 892,
    userGrowth: 12.5,
    questionGrowth: 8.3,
    careerGrowth: -2.1,
    knowledgeGrowth: 15.7
  };

  const recentActivities = [
    { type: 'user', action: '新用户注册', count: 23, time: '今天' },
    { type: 'question', action: '新增题目', count: 45, time: '本周' },
    { type: 'career', action: '更新职业路径', count: 3, time: '本周' },
    { type: 'knowledge', action: '新增知识点', count: 67, time: '本月' }
  ];

  const getGrowthIcon = (growth: number) => {
    return growth > 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />;
  };

  const getGrowthColor = (growth: number) => {
    return growth > 0 ? '#3f8600' : '#cf1322';
  };

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>📊 数据概览</Title>
      <Paragraph>
        欢迎来到通学万卷管理后台！这里是系统数据的总览页面。
      </Paragraph>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={stats.totalUsers}
              prefix={<UserOutlined />}
              suffix={
                <span style={{ fontSize: 12, color: getGrowthColor(stats.userGrowth) }}>
                  {getGrowthIcon(stats.userGrowth)} {Math.abs(stats.userGrowth)}%
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="职业目标"
              value={stats.totalCareers}
              prefix={<BookOutlined />}
              suffix={
                <span style={{ fontSize: 12, color: getGrowthColor(stats.careerGrowth) }}>
                  {getGrowthIcon(stats.careerGrowth)} {Math.abs(stats.careerGrowth)}%
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="题库总数"
              value={stats.totalQuestions}
              prefix={<QuestionCircleOutlined />}
              suffix={
                <span style={{ fontSize: 12, color: getGrowthColor(stats.questionGrowth) }}>
                  {getGrowthIcon(stats.questionGrowth)} {Math.abs(stats.questionGrowth)}%
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="知识点数"
              value={stats.totalKnowledge}
              prefix={<TrophyOutlined />}
              suffix={
                <span style={{ fontSize: 12, color: getGrowthColor(stats.knowledgeGrowth) }}>
                  {getGrowthIcon(stats.knowledgeGrowth)} {Math.abs(stats.knowledgeGrowth)}%
                </span>
              }
            />
          </Card>
        </Col>
      </Row>

      {/* 详细信息 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="📈 系统使用情况" size="small">
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>
                <div style={{ marginBottom: 8 }}>用户活跃度</div>
                <Progress percent={75} status="active" />
              </div>
              <div>
                <div style={{ marginBottom: 8 }}>题库完成度</div>
                <Progress percent={60} status="active" />
              </div>
              <div>
                <div style={{ marginBottom: 8 }}>知识点覆盖率</div>
                <Progress percent={85} status="active" />
              </div>
              <div>
                <div style={{ marginBottom: 8 }}>系统稳定性</div>
                <Progress percent={95} status="active" />
              </div>
            </Space>
          </Card>
        </Col>
        
        <Col xs={24} lg={12}>
          <Card title="🔔 最近活动" size="small">
            <Space direction="vertical" style={{ width: '100%' }}>
              {recentActivities.map((activity, index) => (
                <div key={index} style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center',
                  padding: '8px 0',
                  borderBottom: index < recentActivities.length - 1 ? '1px solid #f0f0f0' : 'none'
                }}>
                  <Space>
                    <Tag color={
                      activity.type === 'user' ? 'blue' :
                      activity.type === 'question' ? 'green' :
                      activity.type === 'career' ? 'orange' : 'purple'
                    }>
                      {activity.action}
                    </Tag>
                    <span>{activity.count} 项</span>
                  </Space>
                  <span style={{ color: '#999', fontSize: 12 }}>{activity.time}</span>
                </div>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 快捷操作 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="🚀 快捷操作" size="small">
            <Space wrap>
              <Tag color="blue" style={{ cursor: 'pointer', padding: '4px 12px' }}>
                📝 创建新题目
              </Tag>
              <Tag color="green" style={{ cursor: 'pointer', padding: '4px 12px' }}>
                👥 用户管理
              </Tag>
              <Tag color="orange" style={{ cursor: 'pointer', padding: '4px 12px' }}>
                📚 知识点管理
              </Tag>
              <Tag color="purple" style={{ cursor: 'pointer', padding: '4px 12px' }}>
                🎯 职业路径设置
              </Tag>
              <Tag color="red" style={{ cursor: 'pointer', padding: '4px 12px' }}>
                ⚙️ 系统设置
              </Tag>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Overview;

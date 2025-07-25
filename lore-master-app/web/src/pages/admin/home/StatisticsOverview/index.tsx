import React, { useEffect, useState } from 'react';
import { 
  Row, 
  Col, 
  Card, 
  Statistic, 
  Progress, 
  Table, 
  Tag,
  Typography,
  Space,
  Button,
  DatePicker,
  Select
} from 'antd';
import { 
  UserOutlined, 
  BookOutlined, 
  QuestionCircleOutlined,
  TrophyOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const StatisticsOverview: React.FC = () => {
  const [loading, setLoading] = useState(false);

  // 模拟数据
  const statsData = {
    totalUsers: 1248,
    activeUsers: 892,
    totalSubjects: 12,
    totalQuestions: 3456,
    userGrowth: 12.5,
    activeRate: 71.5,
    completionRate: 68.3,
    avgScore: 78.5
  };

  const levelDistribution = [
    { level: 'L1', count: 156, percentage: 12.5 },
    { level: 'L2', count: 234, percentage: 18.8 },
    { level: 'L3', count: 298, percentage: 23.9 },
    { level: 'L4', count: 187, percentage: 15.0 },
    { level: 'L5', count: 143, percentage: 11.5 },
    { level: 'L6', count: 98, percentage: 7.9 },
    { level: 'L7', count: 67, percentage: 5.4 },
    { level: 'L8', count: 43, percentage: 3.4 },
    { level: 'L9', count: 22, percentage: 1.8 },
  ];

  const recentActivities = [
    {
      key: '1',
      user: '张三',
      action: '完成Java基础测评',
      level: 'L3',
      score: 85,
      time: '2分钟前',
      status: 'success'
    },
    {
      key: '2',
      user: '李四',
      action: '开始英语语法学习',
      level: 'L2',
      score: null,
      time: '5分钟前',
      status: 'processing'
    },
    {
      key: '3',
      user: '王五',
      action: '数学L4等级测试失败',
      level: 'L4',
      score: 58,
      time: '8分钟前',
      status: 'error'
    },
    {
      key: '4',
      user: '赵六',
      action: '升级到L5等级',
      level: 'L5',
      score: 92,
      time: '12分钟前',
      status: 'success'
    },
  ];

  const activityColumns = [
    {
      title: '用户',
      dataIndex: 'user',
      key: 'user',
    },
    {
      title: '活动',
      dataIndex: 'action',
      key: 'action',
    },
    {
      title: '等级',
      dataIndex: 'level',
      key: 'level',
      render: (level: string) => (
        <Tag color={level <= 'L3' ? 'green' : level <= 'L6' ? 'orange' : 'red'}>
          {level}
        </Tag>
      ),
    },
    {
      title: '分数',
      dataIndex: 'score',
      key: 'score',
      render: (score: number | null) => score ? `${score}分` : '-',
    },
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap = {
          success: { color: 'success', text: '成功' },
          processing: { color: 'processing', text: '进行中' },
          error: { color: 'error', text: '失败' }
        };
        const config = statusMap[status as keyof typeof statusMap];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>数据概览</Title>
        <Space>
          <RangePicker />
          <Select defaultValue="all" style={{ width: 120 }}>
            <Select.Option value="all">全部学科</Select.Option>
            <Select.Option value="java">Java</Select.Option>
            <Select.Option value="english">英语</Select.Option>
            <Select.Option value="math">数学</Select.Option>
          </Select>
          <Button type="primary">导出报告</Button>
        </Space>
      </div>

      {/* 核心指标卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={statsData.totalUsers}
              prefix={<UserOutlined />}
              suffix={
                <span style={{ fontSize: '12px', color: '#52c41a' }}>
                  <ArrowUpOutlined /> {statsData.userGrowth}%
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={statsData.activeUsers}
              prefix={<TrophyOutlined />}
              suffix={
                <span style={{ fontSize: '12px', color: '#52c41a' }}>
                  活跃率 {statsData.activeRate}%
                </span>
              }
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="学科数量"
              value={statsData.totalSubjects}
              prefix={<BookOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="题库总量"
              value={statsData.totalQuestions}
              prefix={<QuestionCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {/* 等级分布 */}
        <Col xs={24} lg={12}>
          <Card title="用户等级分布" extra={<Button type="link">查看详情</Button>}>
            <div style={{ maxHeight: 400, overflowY: 'auto' }}>
              {levelDistribution.map((item) => (
                <div key={item.level} style={{ marginBottom: 16 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <Text strong>{item.level}</Text>
                    <Text>{item.count}人 ({item.percentage}%)</Text>
                  </div>
                  <Progress 
                    percent={item.percentage} 
                    showInfo={false}
                    strokeColor={
                      item.level <= 'L3' ? '#52c41a' : 
                      item.level <= 'L6' ? '#faad14' : '#f5222d'
                    }
                  />
                </div>
              ))}
            </div>
          </Card>
        </Col>

        {/* 学习完成率 */}
        <Col xs={24} lg={12}>
          <Card title="学习数据统计">
            <Row gutter={16}>
              <Col span={12}>
                <div style={{ textAlign: 'center', marginBottom: 24 }}>
                  <Progress
                    type="circle"
                    percent={statsData.completionRate}
                    format={(percent) => `${percent}%`}
                    strokeColor="#1890ff"
                  />
                  <div style={{ marginTop: 8 }}>
                    <Text strong>完成率</Text>
                  </div>
                </div>
              </Col>
              <Col span={12}>
                <div style={{ textAlign: 'center', marginBottom: 24 }}>
                  <Progress
                    type="circle"
                    percent={statsData.avgScore}
                    format={(percent) => `${percent}分`}
                    strokeColor="#52c41a"
                  />
                  <div style={{ marginTop: 8 }}>
                    <Text strong>平均分</Text>
                  </div>
                </div>
              </Col>
            </Row>
            <div style={{ textAlign: 'center' }}>
              <Space direction="vertical" size="small">
                <Text type="secondary">本月学习时长: 1,234小时</Text>
                <Text type="secondary">平均学习时长: 2.3小时/人</Text>
              </Space>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 最近活动 */}
      <Card title="最近活动" extra={<Button type="link">查看全部</Button>}>
        <Table
          columns={activityColumns}
          dataSource={recentActivities}
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  );
};

export default StatisticsOverview;

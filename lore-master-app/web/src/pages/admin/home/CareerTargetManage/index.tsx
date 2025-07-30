import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  message,
  Popconfirm,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  Typography,
  Tree,
  Tooltip,
  Badge,
  Breadcrumb,
  Tabs,
  Divider
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  CopyOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  FolderOutlined,
  FileOutlined,
  HomeOutlined,
  SettingOutlined,
  BarChartOutlined,
  ImportOutlined,
  ExportOutlined
} from '@ant-design/icons';
import { adminApi } from '../../../../utils/request';
import { API_PATHS } from '../../../../config/api';

const { Title, Text } = Typography;
const { TextArea } = Input;
const { TabPane } = Tabs;

interface LearningSkillCatalog {
  id: number;
  skillCode: string;
  skillName: string;
  skillPath: string;
  level: number;
  parentCode?: string;
  parentName?: string;
  icon?: string;
  description?: string;
  sortOrder: number;
  isActive: boolean;
  difficultyLevel?: string;
  difficultyLevelName?: string;
  estimatedHours?: number;
  tagList?: string[];
  createdTime: string;
  updatedTime: string;
  createdBy: string;
  updatedBy: string;
  children?: LearningSkillCatalog[];
  childrenCount?: number;
  hasChildren?: boolean;
  fullPathName?: string;
  levelName?: string;
  statusName?: string;
}

interface QueryParams {
  skillCode?: string;
  skillName?: string;
  skillPathPrefix?: string;
  level?: number;
  parentCode?: string;
  isActive?: boolean;
  difficultyLevel?: string;
  pageNum?: number;
  pageSize?: number;
  sortField?: string;
  sortDirection?: string;
  treeStructure?: boolean;
  includeChildrenCount?: boolean;
}

const SkillCatalogManage: React.FC = () => {
  const [skillList, setSkillList] = useState<LearningSkillCatalog[]>([]);
  const [treeData, setTreeData] = useState<LearningSkillCatalog[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [viewModalVisible, setViewModalVisible] = useState(false);
  const [editingSkill, setEditingSkill] = useState<LearningSkillCatalog | null>(null);
  const [viewingSkill, setViewingSkill] = useState<LearningSkillCatalog | null>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('list');
  const [queryParams, setQueryParams] = useState<QueryParams>({
    pageNum: 1,
    pageSize: 20,
    isActive: true,
    sortField: 'sortOrder',
    sortDirection: 'asc'
  });
  const [statistics, setStatistics] = useState<any>({});
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
  const [searchValue, setSearchValue] = useState('');

  useEffect(() => {
    fetchSkillList();
    fetchStatistics();
  }, [queryParams]);

  // 获取技能目录列表
  const fetchSkillList = async () => {
    setLoading(true);
    try {
      const result = await adminApi.post(API_PATHS.ADMIN.SKILL_CATALOG.PAGE, queryParams);
      if (result.code === 200) {
        const pageData = result.data;
        const skillListData = pageData.content || pageData.records || pageData || [];
        setSkillList(Array.isArray(skillListData) ? skillListData : []);
      } else {
        message.error(result.message || '获取技能目录列表失败');
        setSkillList([]);
      }
    } catch (error) {
      console.error('获取技能目录列表错误:', error);
      message.error('获取技能目录列表失败');
      setSkillList([]);
    } finally {
      setLoading(false);
    }
  };

  // 获取树形结构数据
  const fetchTreeData = async () => {
    setLoading(true);
    try {
      const result = await adminApi.post(API_PATHS.ADMIN.SKILL_CATALOG.TREE, {
        isActive: true,
        treeStructure: true
      });
      if (result.code === 200) {
        setTreeData(result.data || []);
      } else {
        message.error(result.message || '获取技能目录树失败');
        setTreeData([]);
      }
    } catch (error) {
      console.error('获取技能目录树错误:', error);
      message.error('获取技能目录树失败');
      setTreeData([]);
    } finally {
      setLoading(false);
    }
  };

  // 获取统计信息
  const fetchStatistics = async () => {
    try {
      const result = await adminApi.get(API_PATHS.ADMIN.SKILL_CATALOG.STATISTICS);
      if (result.code === 200) {
        setStatistics(result.data || {});
      }
    } catch (error) {
      console.error('获取统计信息错误:', error);
    }
  };

  // 添加技能目录
  const handleAdd = (parentCode?: string, level?: number) => {
    setEditingSkill(null);
    form.resetFields();
    if (parentCode) {
      form.setFieldsValue({ parentCode, level: (level || 0) + 1 });
    }
    setModalVisible(true);
  };

  // 编辑技能目录
  const handleEdit = (record: LearningSkillCatalog) => {
    setEditingSkill(record);
    form.setFieldsValue({
      ...record,
      tagList: record.tagList || []
    });
    setModalVisible(true);
  };

  // 查看技能目录详情
  const handleView = (record: LearningSkillCatalog) => {
    setViewingSkill(record);
    setViewModalVisible(true);
  };

  // 删除技能目录
  const handleDelete = async (id: number) => {
    try {
      const result = await adminApi.delete(`${API_PATHS.ADMIN.SKILL_CATALOG.DELETE}/${id}`);
      if (result.code === 200) {
        message.success('删除成功');
        fetchSkillList();
        if (activeTab === 'tree') {
          fetchTreeData();
        }
      } else {
        message.error(result.message || '删除失败');
      }
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的记录');
      return;
    }

    try {
      const result = await adminApi.post(API_PATHS.ADMIN.SKILL_CATALOG.BATCH_DELETE, selectedRowKeys);
      if (result.code === 200) {
        message.success('批量删除成功');
        setSelectedRowKeys([]);
        fetchSkillList();
      } else {
        message.error(result.message || '批量删除失败');
      }
    } catch (error) {
      message.error('批量删除失败');
    }
  };

  // 提交表单
  const handleSubmit = async (values: any) => {
    try {
      const submitData = {
        ...values,
        tagList: values.tagList || []
      };

      let result;
      if (editingSkill) {
        result = await adminApi.put(`${API_PATHS.ADMIN.SKILL_CATALOG.UPDATE}/${editingSkill.id}`, submitData);
      } else {
        result = await adminApi.post(API_PATHS.ADMIN.SKILL_CATALOG.CREATE, submitData);
      }

      if (result.code === 200) {
        message.success(editingSkill ? '更新成功' : '添加成功');
        setModalVisible(false);
        fetchSkillList();
        if (activeTab === 'tree') {
          fetchTreeData();
        }
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 切换状态
  const handleToggleStatus = async (id: number, isActive: boolean) => {
    try {
      const result = await adminApi.put(`${API_PATHS.ADMIN.SKILL_CATALOG.TOGGLE_STATUS}/${id}?isActive=${isActive}`);
      if (result.code === 200) {
        message.success('状态切换成功');
        fetchSkillList();
      } else {
        message.error(result.message || '状态切换失败');
      }
    } catch (error) {
      message.error('状态切换失败');
    }
  };

  // 复制技能目录
  const handleCopy = async (record: LearningSkillCatalog) => {
    const newSkillCode = `${record.skillCode}_copy_${Date.now()}`;
    const newSkillName = `${record.skillName}_副本`;

    try {
      const result = await adminApi.post(
        `${API_PATHS.ADMIN.SKILL_CATALOG.COPY}/${record.id}?newSkillCode=${newSkillCode}&newSkillName=${newSkillName}`
      );
      if (result.code === 200) {
        message.success('复制成功');
        fetchSkillList();
      } else {
        message.error(result.message || '复制失败');
      }
    } catch (error) {
      message.error('复制失败');
    }
  };

  // 工具方法
  const getDifficultyColor = (level: string) => {
    const colors: Record<string, string> = {
      'beginner': '#52c41a',
      'intermediate': '#1890ff',
      'advanced': '#f5222d'
    };
    return colors[level] || '#666';
  };

  const getLevelColor = (level: number) => {
    const colors = ['#52c41a', '#1890ff', '#faad14'];
    return colors[level - 1] || '#666';
  };

  const getLevelIcon = (level: number) => {
    return level === 3 ? <FileOutlined /> : <FolderOutlined />;
  };

  // 搜索过滤
  const handleSearch = (value: string) => {
    setSearchValue(value);
    setQueryParams(prev => ({
      ...prev,
      skillName: value,
      pageNum: 1
    }));
  };

  // 切换Tab
  const handleTabChange = (key: string) => {
    setActiveTab(key);
    if (key === 'tree' && treeData.length === 0) {
      fetchTreeData();
    }
  };

  const columns = [
    {
      title: '技能信息',
      dataIndex: 'skillName',
      key: 'skillName',
      width: 250,
      render: (text: string, record: LearningSkillCatalog) => (
        <Space direction="vertical" size="small">
          <Space>
            {getLevelIcon(record.level)}
            <Text strong>{text}</Text>
            <Tag color={getLevelColor(record.level)}>{record.levelName}</Tag>
          </Space>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {record.skillCode}
          </Text>
          {record.fullPathName && (
            <Text type="secondary" style={{ fontSize: '11px' }}>
              {record.fullPathName}
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: '层级',
      dataIndex: 'level',
      key: 'level',
      width: 80,
      align: 'center' as const,
      render: (level: number) => (
        <Badge count={level} color={getLevelColor(level)} />
      ),
    },
    {
      title: '父级',
      dataIndex: 'parentName',
      key: 'parentName',
      width: 120,
      render: (parentName: string) => parentName || '-',
    },
    {
      title: '难度',
      dataIndex: 'difficultyLevel',
      key: 'difficultyLevel',
      width: 100,
      align: 'center' as const,
      render: (level: string, record: LearningSkillCatalog) => (
        level ? (
          <Tag color={getDifficultyColor(level)}>
            {record.difficultyLevelName}
          </Tag>
        ) : '-'
      ),
    },
    {
      title: '预估时长',
      dataIndex: 'estimatedHours',
      key: 'estimatedHours',
      width: 100,
      align: 'center' as const,
      render: (hours: number) => hours ? `${hours}h` : '-',
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 80,
      align: 'center' as const,
    },
    {
      title: '子项数量',
      dataIndex: 'childrenCount',
      key: 'childrenCount',
      width: 100,
      align: 'center' as const,
      render: (count: number) => (
        <Badge count={count || 0} showZero color="#108ee9" />
      ),
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 80,
      align: 'center' as const,
      render: (isActive: boolean, record: LearningSkillCatalog) => (
        <Switch
          checked={isActive}
          size="small"
          onChange={(checked) => handleToggleStatus(record.id, checked)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: LearningSkillCatalog) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleView(record)}
            />
          </Tooltip>
          <Tooltip title="添加子项">
            <Button
              type="link"
              size="small"
              icon={<PlusOutlined />}
              disabled={record.level >= 3}
              onClick={() => handleAdd(record.skillCode, record.level)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="复制">
            <Button
              type="link"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopy(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除这个技能目录吗？"
            description="删除后不可恢复，请谨慎操作"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (selectedKeys: React.Key[]) => {
      setSelectedRowKeys(selectedKeys as number[]);
    },
  };

  return (
    <div style={{ padding: '24px', background: '#f5f5f5', minHeight: '100vh' }}>
      {/* 页面头部 */}
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Title level={2} style={{ margin: 0 }}>技能目录管理</Title>
          <Breadcrumb style={{ marginTop: 8 }}>
            <Breadcrumb.Item>
              <HomeOutlined />
            </Breadcrumb.Item>
            <Breadcrumb.Item>系统管理</Breadcrumb.Item>
            <Breadcrumb.Item>技能目录管理</Breadcrumb.Item>
          </Breadcrumb>
        </div>
        <Space>
          <Button icon={<ImportOutlined />}>导入</Button>
          <Button icon={<ExportOutlined />}>导出</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
            添加技能目录
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="技能目录总数"
              value={statistics.totalCount || 0}
              prefix={<BarChartOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="启用目录"
              value={statistics.activeCount || 0}
              prefix={<SettingOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="一级分类"
              value={statistics.levelStats?.level1 || 0}
              prefix={<FolderOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="学习目标"
              value={statistics.levelStats?.level3 || 0}
              prefix={<FileOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 主要内容区域 */}
      <Card>
        <Tabs activeKey={activeTab} onChange={handleTabChange}>
          <TabPane tab="列表视图" key="list">
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Space>
                <Input.Search
                  placeholder="搜索技能名称"
                  allowClear
                  style={{ width: 300 }}
                  onSearch={handleSearch}
                  value={searchValue}
                  onChange={(e) => setSearchValue(e.target.value)}
                />
                <Select
                  placeholder="选择层级"
                  allowClear
                  style={{ width: 120 }}
                  onChange={(value) => setQueryParams(prev => ({ ...prev, level: value, pageNum: 1 }))}
                >
                  <Select.Option value={1}>一级分类</Select.Option>
                  <Select.Option value={2}>二级分类</Select.Option>
                  <Select.Option value={3}>三级目标</Select.Option>
                </Select>
                <Select
                  placeholder="选择状态"
                  allowClear
                  style={{ width: 120 }}
                  onChange={(value) => setQueryParams(prev => ({ ...prev, isActive: value, pageNum: 1 }))}
                >
                  <Select.Option value={true}>启用</Select.Option>
                  <Select.Option value={false}>禁用</Select.Option>
                </Select>
              </Space>
              <Space>
                {selectedRowKeys.length > 0 && (
                  <Popconfirm
                    title={`确定要删除选中的 ${selectedRowKeys.length} 个技能目录吗？`}
                    onConfirm={handleBatchDelete}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button danger>批量删除</Button>
                  </Popconfirm>
                )}
              </Space>
            </div>

            <Table
              columns={columns}
              dataSource={skillList}
              rowKey="id"
              loading={loading}
              rowSelection={rowSelection}
              scroll={{ x: 1200 }}
              pagination={{
                current: queryParams.pageNum,
                pageSize: queryParams.pageSize,
                total: skillList.length,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`,
                onChange: (page, size) => {
                  setQueryParams(prev => ({ ...prev, pageNum: page, pageSize: size }));
                },
              }}
            />
          </TabPane>

          <TabPane tab="树形视图" key="tree">
            <div style={{ marginBottom: 16 }}>
              <Input.Search
                placeholder="搜索技能名称"
                allowClear
                style={{ width: 300 }}
                onSearch={(value) => {
                  // 树形搜索逻辑
                  if (value) {
                    const filterTree = (nodes: LearningSkillCatalog[]): LearningSkillCatalog[] => {
                      return nodes.filter(node => {
                        const match = node.skillName.toLowerCase().includes(value.toLowerCase());
                        const childMatch = node.children && filterTree(node.children).length > 0;
                        if (childMatch) {
                          node.children = filterTree(node.children!);
                        }
                        return match || childMatch;
                      });
                    };
                    // 这里可以实现树形搜索
                  }
                }}
              />
            </div>

            <Tree
              treeData={treeData.map(item => ({
                title: (
                  <Space>
                    <Text strong>{item.skillName}</Text>
                    <Tag color={getLevelColor(item.level)}>
                      {item.levelName}
                    </Tag>
                    {item.difficultyLevel && (
                      <Tag color={getDifficultyColor(item.difficultyLevel)}>
                        {item.difficultyLevelName}
                      </Tag>
                    )}
                    <Switch
                      size="small"
                      checked={item.isActive}
                      onChange={(checked) => handleToggleStatus(item.id, checked)}
                    />
                  </Space>
                ),
                key: item.skillCode,
                children: item.children?.map(child => ({
                  title: (
                    <Space>
                      <Text>{child.skillName}</Text>
                      <Tag color={getLevelColor(child.level)}>
                        {child.levelName}
                      </Tag>
                      {child.difficultyLevel && (
                        <Tag color={getDifficultyColor(child.difficultyLevel)}>
                          {child.difficultyLevelName}
                        </Tag>
                      )}
                      <Switch
                        size="small"
                        checked={child.isActive}
                        onChange={(checked) => handleToggleStatus(child.id, checked)}
                      />
                    </Space>
                  ),
                  key: child.skillCode,
                  children: child.children?.map(grandChild => ({
                    title: (
                      <Space>
                        <Text>{grandChild.skillName}</Text>
                        <Tag color={getLevelColor(grandChild.level)}>
                          {grandChild.levelName}
                        </Tag>
                        {grandChild.difficultyLevel && (
                          <Tag color={getDifficultyColor(grandChild.difficultyLevel)}>
                            {grandChild.difficultyLevelName}
                          </Tag>
                        )}
                        <Switch
                          size="small"
                          checked={grandChild.isActive}
                          onChange={(checked) => handleToggleStatus(grandChild.id, checked)}
                        />
                      </Space>
                    ),
                    key: grandChild.skillCode,
                  }))
                }))
              }))}
              expandedKeys={expandedKeys}
              onExpand={(keys) => setExpandedKeys(keys as string[])}
              showLine
              showIcon
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingSkill ? '编辑技能目录' : '添加技能目录'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="skillName"
                label="技能名称"
                rules={[{ required: true, message: '请输入技能名称' }]}
              >
                <Input placeholder="请输入技能名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="skillCode"
                label="技能编码"
                rules={[
                  { required: true, message: '请输入技能编码' },
                  { pattern: /^[a-zA-Z0-9_]+$/, message: '技能编码只能包含字母、数字和下划线' }
                ]}
              >
                <Input placeholder="请输入技能编码" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="level"
                label="层级"
                rules={[{ required: true, message: '请选择层级' }]}
              >
                <Select placeholder="请选择层级">
                  <Select.Option value={1}>一级分类</Select.Option>
                  <Select.Option value={2}>二级分类</Select.Option>
                  <Select.Option value={3}>三级目标</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="parentCode"
                label="父级编码"
                tooltip="二级和三级必须指定父级编码"
              >
                <Input placeholder="请输入父级编码" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="sortOrder"
                label="排序序号"
              >
                <InputNumber min={0} style={{ width: '100%' }} placeholder="排序序号" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="技能描述"
          >
            <TextArea rows={3} placeholder="请输入技能描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="difficultyLevel"
                label="难度等级"
              >
                <Select placeholder="请选择难度等级" allowClear>
                  <Select.Option value="beginner">初级</Select.Option>
                  <Select.Option value="intermediate">中级</Select.Option>
                  <Select.Option value="advanced">高级</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="estimatedHours"
                label="预估学习时长(小时)"
              >
                <InputNumber min={0} style={{ width: '100%' }} placeholder="预估学习时长" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="icon"
                label="图标"
              >
                <Input placeholder="请输入图标" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="tagList"
            label="标签"
            tooltip="多个标签用回车分隔"
          >
            <Select
              mode="tags"
              style={{ width: '100%' }}
              placeholder="请输入标签"
              tokenSeparators={[',']}
            />
          </Form.Item>

          <Form.Item
            name="isActive"
            label="状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 查看详情模态框 */}
      <Modal
        title="技能目录详情"
        open={viewModalVisible}
        onCancel={() => setViewModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setViewModalVisible(false)}>
            关闭
          </Button>,
          <Button key="edit" type="primary" onClick={() => {
            setViewModalVisible(false);
            if (viewingSkill) {
              handleEdit(viewingSkill);
            }
          }}>
            编辑
          </Button>
        ]}
        width={700}
      >
        {viewingSkill && (
          <div>
            <Row gutter={16}>
              <Col span={12}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>技能名称：</Text>
                  <Text>{viewingSkill.skillName}</Text>
                </div>
              </Col>
              <Col span={12}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>技能编码：</Text>
                  <Text>{viewingSkill.skillCode}</Text>
                </div>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>层级：</Text>
                  <Tag color={getLevelColor(viewingSkill.level)}>
                    {viewingSkill.levelName}
                  </Tag>
                </div>
              </Col>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>父级：</Text>
                  <Text>{viewingSkill.parentName || '-'}</Text>
                </div>
              </Col>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>排序：</Text>
                  <Text>{viewingSkill.sortOrder}</Text>
                </div>
              </Col>
            </Row>

            <div style={{ marginBottom: 16 }}>
              <Text strong>完整路径：</Text>
              <Text>{viewingSkill.fullPathName}</Text>
            </div>

            {viewingSkill.description && (
              <div style={{ marginBottom: 16 }}>
                <Text strong>描述：</Text>
                <div style={{ marginTop: 8, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
                  {viewingSkill.description}
                </div>
              </div>
            )}

            <Row gutter={16}>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>难度等级：</Text>
                  {viewingSkill.difficultyLevel ? (
                    <Tag color={getDifficultyColor(viewingSkill.difficultyLevel)}>
                      {viewingSkill.difficultyLevelName}
                    </Tag>
                  ) : <Text>-</Text>}
                </div>
              </Col>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>预估时长：</Text>
                  <Text>{viewingSkill.estimatedHours ? `${viewingSkill.estimatedHours}小时` : '-'}</Text>
                </div>
              </Col>
              <Col span={8}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>状态：</Text>
                  <Tag color={viewingSkill.isActive ? 'success' : 'default'}>
                    {viewingSkill.statusName}
                  </Tag>
                </div>
              </Col>
            </Row>

            {viewingSkill.tagList && viewingSkill.tagList.length > 0 && (
              <div style={{ marginBottom: 16 }}>
                <Text strong>标签：</Text>
                <div style={{ marginTop: 8 }}>
                  {viewingSkill.tagList.map((tag, index) => (
                    <Tag key={index} color="blue">{tag}</Tag>
                  ))}
                </div>
              </div>
            )}

            <Divider />

            <Row gutter={16}>
              <Col span={12}>
                <div style={{ marginBottom: 8 }}>
                  <Text strong>创建时间：</Text>
                  <Text>{viewingSkill.createdTime}</Text>
                </div>
              </Col>
              <Col span={12}>
                <div style={{ marginBottom: 8 }}>
                  <Text strong>更新时间：</Text>
                  <Text>{viewingSkill.updatedTime}</Text>
                </div>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <div>
                  <Text strong>创建人：</Text>
                  <Text>{viewingSkill.createdBy}</Text>
                </div>
              </Col>
              <Col span={12}>
                <div>
                  <Text strong>更新人：</Text>
                  <Text>{viewingSkill.updatedBy}</Text>
                </div>
              </Col>
            </Row>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default SkillCatalogManage;

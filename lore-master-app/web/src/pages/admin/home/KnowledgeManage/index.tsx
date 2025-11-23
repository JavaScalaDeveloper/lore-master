import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Popconfirm,
  Tag,
  Tabs,
  Typography,
  Row,
  Col,
  Tooltip,
  Spin,
  Tree
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
  NodeIndexOutlined,
  TableOutlined
} from '@ant-design/icons';
import { adminApi } from '../../../../utils/request';

const { Title } = Typography;
const { TextArea } = Input;

// 知识图谱节点接口
interface KnowledgeMapNode {
  id: number;
  nodeCode: string;
  nodeNameStr: string; // 后端返回的显示字段
  nodeType: 'ROOT' | 'LEVEL' | 'BRANCH' | 'LEAF';
  parentCode?: string;
  rootCode: string;
  nodePath: string;
  levelDepth: number;
  levelType?: string;
  sortOrder: number;
  skillCatalogCode?: string;
  description?: string;
  difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  estimatedHours: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdBy?: string;
  createdTime?: string;
  updatedBy?: string;
  updatedTime?: string;
}

// 树形节点接口
interface TreeNode {
  nodeCode: string;
  nodeNameStr: string; // 后端返回的显示字段
  nodeType: string;
  levelDepth: number;
  levelType?: string;
  sortOrder: number;
  difficultyLevel?: string;
  estimatedHours?: number;
  description?: string;
  children?: TreeNode[];
}

// 技能树响应接口
interface SkillTreeResponse {
  rootCode: string;
  rootName: string;
  children: TreeNode[];
}

// 查询参数接口
interface QueryRequest {
  pageNum: number;
  pageSize: number;
  rootCode?: string;
  nodeType?: string;
  difficultyLevel?: string;
  status?: string;
}

const KnowledgeManage: React.FC = () => {
  // 状态管理
  const [knowledgeList, setKnowledgeList] = useState<KnowledgeMapNode[]>([]);
  const [skillTreeData, setSkillTreeData] = useState<SkillTreeResponse | null>(null);
  const [rootNodes, setRootNodes] = useState<KnowledgeMapNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [treeLoading, setTreeLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<KnowledgeMapNode | null>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState<string>('list');
  const [selectedRootCode, setSelectedRootCode] = useState<string>('');
  const [queryParams, setQueryParams] = useState<QueryRequest>({
    pageNum: 1,
    pageSize: 20,
    status: 'ACTIVE'
  });

  useEffect(() => {
    fetchRootNodes();
    fetchKnowledgeList();
  }, []);

  useEffect(() => {
    if (selectedRootCode && activeTab === 'tree') {
      fetchSkillTree();
    }
  }, [selectedRootCode, activeTab]);

  // 获取根节点列表
  const fetchRootNodes = async () => {
    try {
      const result = await adminApi.post('/api/admin/knowledge-map/getRootNodes');
      if (result.success) {
        setRootNodes(result.data || []);
        if (result.data && result.data.length > 0) {
          setSelectedRootCode(result.data[0].nodeCode);
        }
      } else {
        message.error(result.message || '获取根节点失败');
      }
    } catch (error) {
      message.error('获取根节点失败');
    }
  };

  // 获取知识图谱节点列表
  const fetchKnowledgeList = async () => {
    setLoading(true);
    try {
      const result = await adminApi.post('/api/admin/knowledge-map/getNodeList', queryParams);
      if (result.success) {
        setKnowledgeList(result.data || []);
      } else {
        message.error(result.message || '获取知识图谱列表失败');
      }
    } catch (error) {
      message.error('获取知识图谱列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取技能树结构
  const fetchSkillTree = async () => {
    if (!selectedRootCode) return;

    setTreeLoading(true);
    try {
      const result = await adminApi.post(`/api/admin/knowledge-map/getSkillTree?rootCode=${selectedRootCode}`);
      if (result.success) {
        setSkillTreeData(result.data);
      } else {
        message.error(result.message || '获取技能树失败');
      }
    } catch (error) {
      message.error('获取技能树失败');
    } finally {
      setTreeLoading(false);
    }
  };

  // 添加节点
  const handleAdd = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({
      nodeType: 'LEAF',
      difficultyLevel: 'BEGINNER',
      estimatedHours: 0,
      status: 'ACTIVE',
      sortOrder: 0
    });
    setModalVisible(true);
  };

  // 编辑节点
  const handleEdit = (record: KnowledgeMapNode) => {
    setEditingItem(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除节点
  const handleDelete = async (nodeCode: string) => {
    try {
      const result = await adminApi.post(`/api/admin/knowledge-map/deleteNode?nodeCode=${nodeCode}`);
      if (result.success) {
        message.success('删除成功');
        fetchKnowledgeList();
        if (selectedRootCode && activeTab === 'tree') {
          fetchSkillTree();
        }
      } else {
        message.error(result.message || '删除失败');
      }
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 提交表单
  const handleSubmit = async (values: any) => {
    try {
      const submitData = {
        ...values,
        rootCode: values.rootCode || selectedRootCode
      };

      let result;
      if (editingItem) {
        result = await adminApi.post('/api/admin/knowledge-map/updateNode', submitData);
      } else {
        result = await adminApi.post('/api/admin/knowledge-map/addNode', submitData);
      }

      if (result.success) {
        message.success(editingItem ? '更新成功' : '添加成功');
        setModalVisible(false);
        fetchKnowledgeList();
        if (selectedRootCode && activeTab === 'tree') {
          fetchSkillTree();
        }
      } else {
        message.error(result.message || '操作失败');
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 获取节点类型图标
  const getNodeTypeIcon = (type: string) => {
    switch (type) {
      case 'ROOT': return '🎯';
      case 'LEVEL': return '📁';
      case 'BRANCH': return '🌿';
      case 'LEAF': return '📄';
      default: return '📄';
    }
  };

  // 获取节点类型标签
  const getNodeTypeTag = (type: string) => {
    const typeMap = {
      'ROOT': { color: 'red', text: '根节点' },
      'LEVEL': { color: 'blue', text: '层级节点' },
      'BRANCH': { color: 'green', text: '分支节点' },
      'LEAF': { color: 'orange', text: '叶子节点' }
    };
    const config = typeMap[type as keyof typeof typeMap] || { color: 'default', text: type };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 获取难度等级标签
  const getDifficultyTag = (level: string) => {
    const levelMap = {
      'BEGINNER': { color: 'green', text: '初级' },
      'INTERMEDIATE': { color: 'orange', text: '中级' },
      'ADVANCED': { color: 'red', text: '高级' },
      'EXPERT': { color: 'purple', text: '专家' }
    };
    const config = levelMap[level as keyof typeof levelMap] || { color: 'default', text: level };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 转换树形数据为Antd Tree组件格式
  const convertToTreeData = (nodes: TreeNode[]): any[] => {
    return nodes.map(node => ({
      key: node.nodeCode,
      title: (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span>{getNodeTypeIcon(node.nodeType)}</span>
          <span>{node.nodeNameStr }</span>
          {node.difficultyLevel && getDifficultyTag(node.difficultyLevel)}
          {node.estimatedHours && (
            <Tag color="blue">{node.estimatedHours}h</Tag>
          )}
          {node.description && (
            <Tooltip title={node.description}>
              <span style={{ color: '#1890ff', cursor: 'help' }}>ℹ️</span>
            </Tooltip>
          )}
        </div>
      ),
      children: node.children ? convertToTreeData(node.children) : undefined
    }));
  };

  // 表格列定义
  const columns = [
    {
      title: '节点编码',
      dataIndex: 'nodeCode',
      key: 'nodeCode',
      width: 150,
    },
    {
      title: '节点名称',
      dataIndex: 'nodeNameStr',
      key: 'nodeNameStr',
      render: (text: string, record: KnowledgeMapNode) => (
        <Space>
          {getNodeTypeIcon(record.nodeType)}
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '节点类型',
      dataIndex: 'nodeType',
      key: 'nodeType',
      render: (nodeType: string) => getNodeTypeTag(nodeType),
    },
    {
      title: '层级深度',
      dataIndex: 'levelDepth',
      key: 'levelDepth',
      render: (depth: number) => (
        <Tag color="blue">第{depth}层</Tag>
      ),
    },
    {
      title: '难度等级',
      dataIndex: 'difficultyLevel',
      key: 'difficultyLevel',
      render: (level: string) => getDifficultyTag(level),
    },
    {
      title: '预估时长',
      dataIndex: 'estimatedHours',
      key: 'estimatedHours',
      render: (hours: number) => (
        <Tag color="cyan">{hours}小时</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'ACTIVE' ? 'green' : 'red'}>
          {status === 'ACTIVE' ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: KnowledgeMapNode) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleEdit(record)}
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
          <Popconfirm
            title="确定要删除这个节点吗？"
            onConfirm={() => handleDelete(record.nodeCode)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={3} style={{ margin: 0 }}>知识图谱管理</Title>
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAdd}
            >
              添加节点
            </Button>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => {
                fetchKnowledgeList();
                if (selectedRootCode && activeTab === 'tree') {
                  fetchSkillTree();
                }
              }}
            >
              刷新
            </Button>
          </Space>
        </div>

        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'list',
              label: (
                <span>
                  <TableOutlined />
                  列表视图
                </span>
              ),
              children: (
                <>
                  {/* 搜索筛选区域 */}
                  <Card size="small" style={{ marginBottom: 16 }}>
                    <Row gutter={16}>
                      <Col span={6}>
                        <Select
                          value={queryParams.rootCode}
                          onChange={(value) => setQueryParams({...queryParams, rootCode: value})}
                          style={{ width: '100%' }}
                          placeholder="选择根节点"
                          allowClear
                        >
                          {rootNodes.map(node => (
                            <Select.Option key={node.nodeCode} value={node.nodeCode}>
                              {node.nodeNameStr}
                            </Select.Option>
                          ))}
                        </Select>
                      </Col>
                      <Col span={6}>
                        <Select
                          value={queryParams.nodeType}
                          onChange={(value) => setQueryParams({...queryParams, nodeType: value})}
                          style={{ width: '100%' }}
                          placeholder="选择节点类型"
                          allowClear
                        >
                          <Select.Option value="ROOT">根节点</Select.Option>
                          <Select.Option value="LEVEL">层级节点</Select.Option>
                          <Select.Option value="BRANCH">分支节点</Select.Option>
                          <Select.Option value="LEAF">叶子节点</Select.Option>
                        </Select>
                      </Col>
                      <Col span={6}>
                        <Select
                          value={queryParams.difficultyLevel}
                          onChange={(value) => setQueryParams({...queryParams, difficultyLevel: value})}
                          style={{ width: '100%' }}
                          placeholder="选择难度等级"
                          allowClear
                        >
                          <Select.Option value="BEGINNER">初级</Select.Option>
                          <Select.Option value="INTERMEDIATE">中级</Select.Option>
                          <Select.Option value="ADVANCED">高级</Select.Option>
                          <Select.Option value="EXPERT">专家</Select.Option>
                        </Select>
                      </Col>
                      <Col span={6}>
                        <Button
                          type="primary"
                          onClick={fetchKnowledgeList}
                          style={{ width: '100%' }}
                        >
                          查询
                        </Button>
                      </Col>
                    </Row>
                  </Card>

                  {/* 知识图谱列表 */}
                  <Table
                    columns={columns}
                    dataSource={knowledgeList}
                    rowKey="nodeCode"
                    loading={loading}
                    pagination={{
                      current: queryParams.pageNum,
                      pageSize: queryParams.pageSize,
                      total: knowledgeList.length,
                      showSizeChanger: true,
                      showQuickJumper: true,
                      showTotal: (total) => `共 ${total} 条记录`,
                      onChange: (page, size) => setQueryParams({
                        ...queryParams,
                        pageNum: page,
                        pageSize: size
                      })
                    }}
                  />
                </>
              )
            },
            {
              key: 'tree',
              label: (
                <span>
                  <NodeIndexOutlined />
                  树形视图
                </span>
              ),
              children: (
                <Card>
                  <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <span style={{ marginRight: 8 }}>选择技能树：</span>
                      <Select
                        value={selectedRootCode}
                        onChange={setSelectedRootCode}
                        style={{ width: 300 }}
                        placeholder="选择要查看的技能树"
                      >
                        {rootNodes.map(node => (
                          <Select.Option key={node.nodeCode} value={node.nodeCode}>
                            {node.nodeNameStr}
                          </Select.Option>
                        ))}
                      </Select>
                    </div>
                    <Button
                      icon={<ReloadOutlined />}
                      onClick={() => {
                        if (selectedRootCode) {
                          fetchSkillTree();
                        }
                      }}
                    >
                      刷新
                    </Button>
                  </div>

                  <Spin spinning={treeLoading}>
                    {skillTreeData ? (
                      <div>
                        <Title level={4}>{skillTreeData.rootName}</Title>
                        <Tree
                          showLine
                          defaultExpandAll
                          treeData={convertToTreeData(skillTreeData.children)}
                        />
                      </div>
                    ) : (
                      <div style={{ textAlign: 'center', padding: '50px 0', color: '#999' }}>
                        请选择要查看的技能树
                      </div>
                    )}
                  </Spin>
                </Card>
              )
            }
          ]}
        />
      </Card>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingItem ? '编辑节点' : '添加节点'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="nodeCode"
                label="节点编码"
                rules={[{ required: true, message: '请输入节点编码' }]}
              >
                <Input placeholder="请输入节点编码" disabled={!!editingItem} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="nodeNameStr"
                label="节点名称"
                rules={[{ required: true, message: '请输入节点名称' }]}
              >
                <Input placeholder="请输入节点名称" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="nodeType"
                label="节点类型"
                rules={[{ required: true, message: '请选择节点类型' }]}
              >
                <Select placeholder="请选择节点类型">
                  <Select.Option value="ROOT">根节点</Select.Option>
                  <Select.Option value="LEVEL">层级节点</Select.Option>
                  <Select.Option value="BRANCH">分支节点</Select.Option>
                  <Select.Option value="LEAF">叶子节点</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="parentCode"
                label="父节点编码"
              >
                <Input placeholder="请输入父节点编码（根节点可为空）" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="levelDepth"
                label="层级深度"
                rules={[
                  { required: true, message: '请输入层级深度' },
                  { type: 'number', min: 1, max: 10, message: '请输入1-10之间的数字' }
                ]}
              >
                <InputNumber
                  min={1}
                  max={10}
                  placeholder="层级深度"
                  style={{ width: '100%' }}
                  controls={true}
                  keyboard={true}
                  stringMode={false}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="levelType"
                label="层级类型"
              >
                <Input placeholder="如：L1, L2等" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="sortOrder"
                label="排序序号"
                rules={[
                  { required: true, message: '请输入排序序号' },
                  { type: 'number', min: 0, message: '请输入有效的数字' }
                ]}
              >
                <InputNumber
                  min={0}
                  placeholder="排序序号"
                  style={{ width: '100%' }}
                  controls={true}
                  keyboard={true}
                  stringMode={false}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="difficultyLevel"
                label="难度等级"
                rules={[{ required: true, message: '请选择难度等级' }]}
              >
                <Select placeholder="请选择难度等级">
                  <Select.Option value="BEGINNER">初级</Select.Option>
                  <Select.Option value="INTERMEDIATE">中级</Select.Option>
                  <Select.Option value="ADVANCED">高级</Select.Option>
                  <Select.Option value="EXPERT">专家</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="estimatedHours"
                label="预估时长(小时)"
                rules={[
                  { required: true, message: '请输入预估时长' },
                  { type: 'number', min: 0, message: '请输入有效的数字' }
                ]}
              >
                <InputNumber
                  min={0}
                  placeholder="预估学习时长"
                  style={{ width: '100%' }}
                  controls={true}
                  keyboard={true}
                  stringMode={false}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="skillCatalogCode"
            label="技能目录编码"
            rules={[{ whitespace: true }]}
          >
            <Input
              id="skillCatalogCodeInput"
              placeholder="请输入技能目录编码"
              autoComplete="off"
              spellCheck={false}
              onBlur={() => {}}
            />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={4} placeholder="请输入节点描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default KnowledgeManage;

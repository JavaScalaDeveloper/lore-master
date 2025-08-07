import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  message,
  Popconfirm,
  Tag,
  Card,
  Tree,
  Tabs,
  Typography,
  Divider,
  Row,
  Col,
  InputNumber,
  Tooltip,
  Spin
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FolderOutlined,
  FileTextOutlined,
  EyeOutlined,
  NodeIndexOutlined,
  TableOutlined,
  ReloadOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons';
import { adminApi } from '../../../../utils/request';
import EditableTree from './components/EditableTree';

const { Title, Paragraph } = Typography;
const { TextArea } = Input;
const { TabPane } = Tabs;

// 知识图谱节点接口
interface KnowledgeMapNode {
  id: number;
  nodeCode: string;
  nodeName: string;
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
  nodeName: string;
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

// 查询请求接口
interface QueryRequest {
  rootCode?: string;
  nodeType?: string;
  levelDepth?: number;
  levelType?: string;
  difficultyLevel?: string;
  status?: string;
  keyword?: string;
  pageNum?: number;
  pageSize?: number;
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
    // 设置默认值
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

  // 树形视图专用的处理函数
  const handleNodeEdit = async (nodeCode: string, data: any) => {
    try {
      const result = await adminApi.post('/api/admin/knowledge-map/updateNode', {
        nodeCode,
        ...data
      });
      if (result.success) {
        fetchSkillTree();
        fetchKnowledgeList();
      } else {
        message.error(result.message || '更新失败');
      }
    } catch (error) {
      message.error('更新失败');
    }
  };

  const handleNodeDelete = async (nodeCode: string) => {
    await handleDelete(nodeCode);
  };

  const handleAddChildNode = (parentCode?: string) => {
    setEditingItem(null);
    form.resetFields();
    if (parentCode) {
      form.setFieldsValue({ parentCode });
    }
    setModalVisible(true);
  };

  const handleNodeMove = async (nodeCode: string, newParentCode: string, newSortOrder: number) => {
    try {
      const result = await adminApi.post('/api/admin/knowledge-map/moveNode', {
        nodeCode,
        newParentCode,
        newSortOrder
      });
      if (result.success) {
        fetchSkillTree();
        fetchKnowledgeList();
      } else {
        message.error(result.message || '移动失败');
      }
    } catch (error) {
      message.error('移动失败');
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

  // 状态切换
  const handleStatusChange = async (nodeCode: string, status: string) => {
    try {
      const result = await adminApi.post('/api/admin/knowledge-map/updateNode', {
        nodeCode,
        status
      });
      if (result.success) {
        message.success(`${status === 'ACTIVE' ? '启用' : '禁用'}成功`);
        fetchKnowledgeList();
      } else {
        message.error(result.message || '状态更新失败');
      }
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  // 获取节点类型图标
  const getNodeTypeIcon = (nodeType: string) => {
    const iconMap = {
      ROOT: <FolderOutlined style={{ color: '#722ed1' }} />,
      LEVEL: <FolderOutlined style={{ color: '#1890ff' }} />,
      BRANCH: <FolderOutlined style={{ color: '#52c41a' }} />,
      LEAF: <FileTextOutlined style={{ color: '#faad14' }} />
    };
    return iconMap[nodeType as keyof typeof iconMap] || <FileTextOutlined />;
  };

  // 获取节点类型标签
  const getNodeTypeTag = (nodeType: string) => {
    const typeMap = {
      ROOT: { color: 'purple', text: '根节点' },
      LEVEL: { color: 'blue', text: '层级节点' },
      BRANCH: { color: 'green', text: '分支节点' },
      LEAF: { color: 'orange', text: '叶子节点' }
    };
    const config = typeMap[nodeType as keyof typeof typeMap];
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 获取难度等级颜色
  const getDifficultyColor = (level: string) => {
    const colorMap = {
      BEGINNER: 'green',
      INTERMEDIATE: 'orange',
      ADVANCED: 'red',
      EXPERT: 'purple'
    };
    return colorMap[level as keyof typeof colorMap] || 'default';
  };

  // 获取难度等级文本
  const getDifficultyText = (level: string) => {
    const textMap = {
      BEGINNER: '初级',
      INTERMEDIATE: '中级',
      ADVANCED: '高级',
      EXPERT: '专家'
    };
    return textMap[level as keyof typeof textMap] || level;
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
      dataIndex: 'nodeName',
      key: 'nodeName',
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
      title: '层级类型',
      dataIndex: 'levelType',
      key: 'levelType',
      render: (levelType: string) => levelType ? (
        <Tag color="green">{levelType}</Tag>
      ) : '-',
    },
    {
      title: '难度等级',
      dataIndex: 'difficultyLevel',
      key: 'difficultyLevel',
      render: (level: string) => (
        <Tag color={getDifficultyColor(level)}>
          {getDifficultyText(level)}
        </Tag>
      ),
    },
    {
      title: '预估时长',
      dataIndex: 'estimatedHours',
      key: 'estimatedHours',
      render: (hours: number) => `${hours}小时`,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: KnowledgeMapNode) => (
        <Switch
          checked={status === 'ACTIVE'}
          onChange={(checked) => handleStatusChange(record.nodeCode, checked ? 'ACTIVE' : 'INACTIVE')}
          size="small"
        />
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

  // 转换树形数据
  const convertToTreeData = (treeData: SkillTreeResponse | null): any[] => {
    if (!treeData) return [];

    const convertNode = (node: TreeNode): any => ({
      title: (
        <Space>
          {getNodeTypeIcon(node.nodeType)}
          <span>{node.nodeName}</span>
          {node.difficultyLevel && (
            <Tag color={getDifficultyColor(node.difficultyLevel)}>
              {getDifficultyText(node.difficultyLevel)}
            </Tag>
          )}
          {node.estimatedHours && (
            <Tag color="blue">{node.estimatedHours}h</Tag>
          )}
        </Space>
      ),
      key: node.nodeCode,
      children: node.children?.map(convertNode) || []
    });

    return treeData.children?.map(convertNode) || [];
  };

  // 转换为可编辑树形数据
  const convertToEditableTreeData = (treeData: SkillTreeResponse | null): any[] => {
    if (!treeData) return [];

    const convertNode = (node: TreeNode, parentCode?: string): any => ({
      nodeCode: node.nodeCode,
      nodeName: node.nodeName,
      nodeType: node.nodeType,
      levelDepth: node.levelDepth,
      levelType: node.levelType,
      difficultyLevel: node.difficultyLevel || '初级',
      estimatedHours: node.estimatedHours || 0,
      description: node.description,
      parentCode: parentCode,
      sortOrder: node.sortOrder,
      key: node.nodeCode,
      children: node.children ? node.children.map(child => convertNode(child, node.nodeCode)) : undefined,
    });

    return treeData.children ? treeData.children.map(child => convertNode(child, treeData.rootCode)) : [];
  };

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>知识图谱管理</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => {
            fetchKnowledgeList();
            fetchRootNodes();
          }}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加节点
          </Button>
        </Space>
      </div>

      {/* 标签页 */}
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab={<span><TableOutlined />列表视图</span>} key="list">
          {/* 筛选条件 */}
          <Card style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={6}>
                <span style={{ marginRight: 8 }}>根节点：</span>
                <Select
                  value={queryParams.rootCode}
                  onChange={(value) => setQueryParams({...queryParams, rootCode: value})}
                  style={{ width: '100%' }}
                  placeholder="选择根节点"
                >
                  <Select.Option value="">全部</Select.Option>
                  {rootNodes.map(node => (
                    <Select.Option key={node.nodeCode} value={node.nodeCode}>
                      {node.nodeName}
                    </Select.Option>
                  ))}
                </Select>
              </Col>
              <Col span={6}>
                <span style={{ marginRight: 8 }}>节点类型：</span>
                <Select
                  value={queryParams.nodeType}
                  onChange={(value) => setQueryParams({...queryParams, nodeType: value})}
                  style={{ width: '100%' }}
                  placeholder="选择节点类型"
                >
                  <Select.Option value="">全部</Select.Option>
                  <Select.Option value="ROOT">根节点</Select.Option>
                  <Select.Option value="LEVEL">层级节点</Select.Option>
                  <Select.Option value="BRANCH">分支节点</Select.Option>
                  <Select.Option value="LEAF">叶子节点</Select.Option>
                </Select>
              </Col>
              <Col span={6}>
                <span style={{ marginRight: 8 }}>难度等级：</span>
                <Select
                  value={queryParams.difficultyLevel}
                  onChange={(value) => setQueryParams({...queryParams, difficultyLevel: value})}
                  style={{ width: '100%' }}
                  placeholder="选择难度等级"
                >
                  <Select.Option value="">全部</Select.Option>
                  <Select.Option value="BEGINNER">初级</Select.Option>
                  <Select.Option value="INTERMEDIATE">中级</Select.Option>
                  <Select.Option value="ADVANCED">高级</Select.Option>
                  <Select.Option value="EXPERT">专家</Select.Option>
                </Select>
              </Col>
              <Col span={6}>
                <Button type="primary" onClick={fetchKnowledgeList}>
                  查询
                </Button>
              </Col>
            </Row>
          </Card>

          {/* 知识图谱列表 */}
          <Card>
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
          </Card>
        </TabPane>

        <TabPane tab={<span><NodeIndexOutlined />树形视图</span>} key="tree">
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
                      {node.nodeName}
                    </Select.Option>
                  ))}
                </Select>
              </div>
              <Space>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => handleAddChildNode()}
                  disabled={!selectedRootCode}
                >
                  添加子节点
                </Button>
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
              </Space>
            </div>

            <Spin spinning={treeLoading}>
              {skillTreeData ? (
                <div>
                  <Title level={4}>{skillTreeData.rootName}</Title>
                  <EditableTree
                    treeData={convertToEditableTreeData(skillTreeData)}
                    onNodeEdit={handleNodeEdit}
                    onNodeDelete={handleNodeDelete}
                    onNodeAdd={handleAddChildNode}
                    onNodeMove={handleNodeMove}
                  />
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '50px 0', color: '#999' }}>
                  请选择要查看的技能树
                </div>
              )}
            </Spin>
          </Card>
        </TabPane>
      </Tabs>

      {/* 添加/编辑模态框 */}
      <Modal
        title={editingItem ? '编辑节点' : '添加节点'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={800}
        forceRender={false}
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
                name="nodeName"
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
                rules={[{ required: true, message: '请输入层级深度' }]}
              >
                <InputNumber min={1} max={10} placeholder="层级深度" style={{ width: '100%' }} />
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
                rules={[{ required: true, message: '请输入排序序号' }]}
              >
                <InputNumber min={0} placeholder="排序序号" style={{ width: '100%' }} />
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
                rules={[{ required: true, message: '请输入预估时长' }]}
              >
                <InputNumber min={0} placeholder="预估学习时长" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="skillCatalogCode"
            label="技能目录编码"
          >
            <Input placeholder="请输入技能目录编码" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={4} placeholder="请输入节点描述" />
          </Form.Item>

          <Form.Item
            name="status"
            label="状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch
              checkedChildren="启用"
              unCheckedChildren="禁用"
              onChange={(checked) => form.setFieldsValue({ status: checked ? 'ACTIVE' : 'INACTIVE' })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default KnowledgeManage;

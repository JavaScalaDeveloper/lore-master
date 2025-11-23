import React, { useState } from 'react';
import {
  Tree,
  Input,
  InputNumber,
  Button,
  Space,
  Dropdown,
  Modal,
  Form,
  Select,
  message,
  Tooltip,
  Tag
} from 'antd';
import { EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  MoreOutlined
} from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import styles from './EditableTree.module.css';

interface EditableTreeNode extends DataNode {
  nodeCode: string;
  nodeName: string;
  nodeNameStr: string; // 后端返回的显示字段
  nodeType: string;
  levelDepth: number;
  levelType?: string;
  difficultyLevel: string;
  estimatedHours: number;
  description?: string;
  parentCode?: string;
  sortOrder: number;
  isEditing?: boolean;
  children?: EditableTreeNode[];
}

interface EditableTreeProps {
  treeData: EditableTreeNode[];
  onNodeEdit: (nodeCode: string, data: any) => Promise<void>;
  onNodeDelete: (nodeCode: string) => Promise<void>;
  onNodeAdd: (parentCode?: string, data?: any) => void;
  onNodeMove: (nodeCode: string, newParentCode: string, newSortOrder: number) => Promise<void>;
}

const EditableTree: React.FC<EditableTreeProps> = ({
  treeData,
  onNodeEdit,
  onNodeDelete,
  onNodeAdd,
  onNodeMove
}) => {
  const [editingNode, setEditingNode] = useState<string | null>(null);
  const [editForm] = Form.useForm();
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [addForm] = Form.useForm();
  const [selectedParentCode, setSelectedParentCode] = useState<string>('');

  // 获取难度等级颜色
  const getDifficultyColor = (level: string) => {
    const colors: Record<string, string> = {
      '初级': 'green',
      '中级': 'orange', 
      '高级': 'red',
      '专家': 'purple'
    };
    return colors[level] || 'default';
  };

  // 获取节点类型图标
  const getNodeTypeIcon = (type: string) => {
    switch (type) {
      case 'ROOT': return '🎯';
      case 'LEVEL': return '📁';
      case 'LEAF': return '📄';
      default: return '📄';
    }
  };

  // 开始编辑节点
  const startEdit = (node: EditableTreeNode) => {
    setEditingNode(node.nodeCode);
    editForm.setFieldsValue({
      nodeCode: node.nodeCode,
      nodeName: node.nodeName,
      nodeType: node.nodeType,
      difficultyLevel: node.difficultyLevel,
      estimatedHours: node.estimatedHours,
      description: node.description
    });
    setAddModalVisible(true); // 复用添加模态框进行编辑
  };

  // 删除节点
  const deleteNode = (nodeCode: string, nodeNameStr: string) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除节点"${nodeNameStr}"吗？此操作不可恢复。`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await onNodeDelete(nodeCode);
          message.success('节点删除成功');
        } catch (error) {
          console.error('删除失败:', error);
        }
      }
    });
  };

  // 添加子节点
  const showAddModal = (parentCode?: string) => {
    setSelectedParentCode(parentCode || '');
    setAddModalVisible(true);
    addForm.resetFields();
  };

  // 确认添加/编辑节点
  const handleAddNode = async () => {
    try {
      if (editingNode) {
        // 编辑模式
        const values = await editForm.validateFields();
        await onNodeEdit(editingNode, values);
        message.success('节点更新成功');
      } else {
        // 添加模式
        const values = await addForm.validateFields();
        onNodeAdd(selectedParentCode || undefined, values);
        message.success('节点添加成功');
      }
      // 先关闭模态框，再重置状态
      setAddModalVisible(false);
      setTimeout(() => {
        setEditingNode(null);
        editForm.resetFields();
        addForm.resetFields();
      }, 300);
    } catch (error) {
      console.error(editingNode ? '更新失败:' : '添加失败:', error);
      message.error(editingNode ? '更新失败，请检查输入内容' : '添加失败，请检查输入内容');
    }
  };

  // 渲染节点操作按钮
  const renderNodeActions = (node: EditableTreeNode) => {

    const menuItems = [
      {
        key: 'edit',
        label: '编辑',
        icon: <EditOutlined />
      },
      {
        key: 'add',
        label: '添加子节点',
        icon: <PlusOutlined />
      },
      {
        key: 'delete',
        label: '删除',
        icon: <DeleteOutlined />,
        danger: true
      }
    ];

    const handleMenuClick = ({ key }: { key: string }) => {
      switch (key) {
        case 'edit':
          startEdit(node);
          break;
        case 'add':
          showAddModal(node.nodeCode);
          break;
        case 'delete':
          deleteNode(node.nodeCode, node.nodeNameStr || node.nodeName);
          break;
        default:
          break;
      }
    };

    return (
      <Space size="small">
        <Dropdown
          menu={{
            items: menuItems,
            onClick: handleMenuClick
          }}
          trigger={['click']}
        >
          <Button size="small" icon={<MoreOutlined />} />
        </Dropdown>
      </Space>
    );
  };

  // 渲染节点内容
  const renderNodeContent = (node: EditableTreeNode) => {
    if (editingNode === node.nodeCode) {
      return (
        <div className={styles.editForm}>
          <span style={{ color: '#1890ff', fontWeight: 'bold' }}>
            [编辑模式] {node.nodeNameStr || node.nodeName}
          </span>
          <Tag color="orange">编辑中...</Tag>
        </div>
      );
    }

    // 为叶子节点添加特殊处理，确保生成有效的DOM结构
    const nodeIcon = (
      <span className={styles.nodeIcon}>{getNodeTypeIcon(node.nodeType)}</span>
    );
    const nodeName = (
      <span className={`${styles.nodeName} ${node.nodeType === 'ROOT' ? styles.root : ''}`}>
        {node.nodeNameStr || node.nodeName}
      </span>
    );
    const difficultyTag = (
      <Tag
        color={getDifficultyColor(node.difficultyLevel)}
        className={styles.difficultyTag}
      >
        {node.difficultyLevel}
      </Tag>
    );
    const hoursTag = (
      <Tag color="blue" className={styles.hoursTag}>
        {node.estimatedHours}h
      </Tag>
    );
    const descriptionIcon = node.description ? (
      <Tooltip title={node.description}>
        <span className={styles.descriptionIcon}>ℹ️</span>
      </Tooltip>
    ) : null;

    return (
      <div className={styles.nodeContent}>
        {nodeIcon}
        {nodeName}
        {difficultyTag}
        {hoursTag}
        {descriptionIcon}
      </div>
    );
  };

  // 转换树数据，添加自定义渲染
  const convertTreeData = (nodes: EditableTreeNode[]): DataNode[] => {
    return nodes.map(node => ({
      key: node.nodeCode,
      title: (
        <div className={styles.treeNode}>
          {renderNodeContent(node)}
          <div className={styles.nodeActions}>
            {renderNodeActions(node)}
          </div>
        </div>
      ),
      children: node.children ? convertTreeData(node.children) : undefined,
      // 只传递DataNode需要的属性，避免nodeName等属性污染DOM事件系统
      isLeaf: node.nodeType === 'LEAF',
      selectable: true,
      disabled: false
    }));
  };

  return (
    <div className={styles.editableTree}>
      <Tree
        showLine
        defaultExpandAll
        treeData={convertTreeData(treeData)}
        draggable
        onDrop={async (info) => {
          const { dragNode, node, dropPosition } = info;
          // 处理拖拽移动逻辑
          try {
            if (dragNode && node && dragNode.key && node.key) {
              await onNodeMove(
                dragNode.key as string,
                node.key as string,
                dropPosition
              );
              message.success('节点移动成功');
            } else {
              console.error('无效的拖拽操作: 缺少必要的节点信息');
              message.error('无效的拖拽操作');
            }
          } catch (error) {
            console.error('移动失败:', error);
            message.error('节点移动失败');
          }
        }}
      />

      {/* 添加/编辑节点模态框 */}
      <Modal
        title={editingNode ? "编辑节点" : "添加节点"}
        open={addModalVisible}
        onOk={handleAddNode}
        onCancel={() => {
          setAddModalVisible(false);
          setTimeout(() => {
            setEditingNode(null);
            editForm.resetFields();
            addForm.resetFields();
          }, 300);
        }}
        okText="确定"
        cancelText="取消"
        className={styles.addModal}
      >
        <Form form={editingNode ? editForm : addForm} layout="vertical">
          <Form.Item
            name="nodeCode"
            label="节点编码"
            rules={[{ required: true, message: '请输入节点编码' }]}
          >
            <Input placeholder="请输入节点编码" disabled={!!editingNode} />
          </Form.Item>
          <Form.Item
            name="nodeName"
            label="节点名称"
            rules={[{ required: true, message: '请输入节点名称' }]}
          >
            <Input placeholder="请输入节点名称" />
          </Form.Item>
          <Form.Item
            name="nodeType"
            label="节点类型"
            rules={[{ required: true, message: '请选择节点类型' }]}
          >
            <Select placeholder="请选择节点类型">
              <Select.Option value="LEVEL">层级节点</Select.Option>
              <Select.Option value="LEAF">叶子节点</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="difficultyLevel"
            label="难度等级"
            rules={[{ required: true, message: '请选择难度等级' }]}
          >
            <Select placeholder="请选择难度等级">
              <Select.Option value="初级">初级</Select.Option>
              <Select.Option value="中级">中级</Select.Option>
              <Select.Option value="高级">高级</Select.Option>
              <Select.Option value="专家">专家</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="estimatedHours"
            label="预估时长(小时)"
            rules={[
              { required: true, message: '请输入预估时长' },
              { type: 'number', min: 1, max: 9999, message: '请输入1-9999之间的数字' }
            ]}
          >
            <InputNumber
              placeholder="请输入预估时长"
              style={{ width: '100%' }}
              min={1}
              max={9999}
              controls={true}
              keyboard={true}
              stringMode={false}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入节点描述" rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default EditableTree;

import React, { useState, useRef } from 'react';
import {
  Tree,
  Input,
  Button,
  Space,
  Dropdown,
  Modal,
  Form,
  Select,
  InputNumber,
  message,
  Tooltip,
  Tag
} from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  MoreOutlined,
  DragOutlined,
  SaveOutlined,
  CloseOutlined
} from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import styles from './EditableTree.module.css';

interface EditableTreeNode extends DataNode {
  nodeCode: string;
  nodeName: string;
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
      nodeName: node.nodeName,
      difficultyLevel: node.difficultyLevel,
      estimatedHours: node.estimatedHours,
      description: node.description
    });
  };

  // 保存编辑
  const saveEdit = async () => {
    try {
      const values = await editForm.validateFields();
      if (editingNode) {
        await onNodeEdit(editingNode, values);
        setEditingNode(null);
        message.success('节点更新成功');
      }
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  // 取消编辑
  const cancelEdit = () => {
    setEditingNode(null);
    editForm.resetFields();
  };

  // 删除节点
  const deleteNode = (nodeCode: string, nodeName: string) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除节点"${nodeName}"吗？此操作不可恢复。`,
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

  // 确认添加节点
  const handleAddNode = async () => {
    try {
      const values = await addForm.validateFields();
      onNodeAdd(selectedParentCode || undefined);
      setAddModalVisible(false);
      message.success('节点添加成功');
    } catch (error) {
      console.error('添加失败:', error);
    }
  };

  // 渲染节点操作按钮
  const renderNodeActions = (node: EditableTreeNode) => {
    if (editingNode === node.nodeCode) {
      return (
        <Space size="small">
          <Button
            type="primary"
            size="small"
            icon={<SaveOutlined />}
            onClick={saveEdit}
            className={styles.saveButton}
          >
            保存
          </Button>
          <Button
            size="small"
            icon={<CloseOutlined />}
            onClick={cancelEdit}
            className={styles.cancelButton}
          >
            取消
          </Button>
        </Space>
      );
    }

    const menuItems = [
      {
        key: 'edit',
        label: '编辑',
        icon: <EditOutlined />,
        onClick: () => startEdit(node)
      },
      {
        key: 'add',
        label: '添加子节点',
        icon: <PlusOutlined />,
        onClick: () => showAddModal(node.nodeCode)
      },
      {
        key: 'delete',
        label: '删除',
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => deleteNode(node.nodeCode, node.nodeName)
      }
    ];

    return (
      <Space size="small">
        <Dropdown menu={{ items: menuItems }} trigger={['click']}>
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
          <Form form={editForm} layout="inline" style={{ flex: 1 }}>
            <Form.Item name="nodeName" style={{ marginBottom: 0, flex: 1 }}>
              <Input placeholder="节点名称" className={styles.editInput} />
            </Form.Item>
            <Form.Item name="difficultyLevel" style={{ marginBottom: 0 }}>
              <Select className={styles.editSelect}>
                <Select.Option value="初级">初级</Select.Option>
                <Select.Option value="中级">中级</Select.Option>
                <Select.Option value="高级">高级</Select.Option>
                <Select.Option value="专家">专家</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="estimatedHours" style={{ marginBottom: 0 }}>
              <InputNumber
                placeholder="时长"
                min={1}
                max={9999}
                className={styles.editNumber}
                addonAfter="h"
              />
            </Form.Item>
          </Form>
        </div>
      );
    }

    return (
      <div className={styles.nodeContent}>
        <span className={styles.nodeIcon}>{getNodeTypeIcon(node.nodeType)}</span>
        <span className={`${styles.nodeName} ${node.nodeType === 'ROOT' ? styles.root : ''}`}>
          {node.nodeName}
        </span>
        <Tag
          color={getDifficultyColor(node.difficultyLevel)}
          className={styles.difficultyTag}
        >
          {node.difficultyLevel}
        </Tag>
        <Tag color="blue" className={styles.hoursTag}>
          {node.estimatedHours}h
        </Tag>
        {node.description && (
          <Tooltip title={node.description}>
            <span className={styles.descriptionIcon}>ℹ️</span>
          </Tooltip>
        )}
      </div>
    );
  };

  // 转换树数据，添加自定义渲染
  const convertTreeData = (nodes: EditableTreeNode[]): DataNode[] => {
    return nodes.map(node => ({
      ...node,
      key: node.nodeCode,
      title: (
        <div className={styles.treeNode}>
          {renderNodeContent(node)}
          <div className={styles.nodeActions}>
            {renderNodeActions(node)}
          </div>
        </div>
      ),
      children: node.children ? convertTreeData(node.children) : undefined
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
            await onNodeMove(
              dragNode.key as string,
              node.key as string,
              dropPosition
            );
            message.success('节点移动成功');
          } catch (error) {
            console.error('移动失败:', error);
          }
        }}
      />

      {/* 添加节点模态框 */}
      <Modal
        title="添加节点"
        open={addModalVisible}
        onOk={handleAddNode}
        onCancel={() => setAddModalVisible(false)}
        okText="确定"
        cancelText="取消"
        className={styles.addModal}
      >
        <Form form={addForm} layout="vertical">
          <Form.Item
            name="nodeCode"
            label="节点编码"
            rules={[{ required: true, message: '请输入节点编码' }]}
          >
            <Input placeholder="请输入节点编码" />
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
            rules={[{ required: true, message: '请输入预估时长' }]}
          >
            <InputNumber
              min={1}
              max={9999}
              placeholder="请输入预估时长"
              style={{ width: '100%' }}
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

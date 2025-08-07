import React from 'react';
import { render, screen } from '@testing-library/react';
import EditableTree from './EditableTree';

// Mock数据
const mockTreeData = [
  {
    nodeCode: 'java_expert',
    nodeName: 'Java专家',
    nodeType: 'ROOT',
    levelDepth: 1,
    difficultyLevel: '专家',
    estimatedHours: 2000,
    sortOrder: 1,
    key: 'java_expert',
    children: [
      {
        nodeCode: 'java_l1',
        nodeName: 'L1初级',
        nodeType: 'LEVEL',
        levelDepth: 2,
        levelType: 'L1',
        difficultyLevel: '初级',
        estimatedHours: 200,
        sortOrder: 1,
        key: 'java_l1',
        children: []
      }
    ]
  }
];

// Mock函数
const mockOnNodeEdit = jest.fn();
const mockOnNodeDelete = jest.fn();
const mockOnNodeAdd = jest.fn();
const mockOnNodeMove = jest.fn();

describe('EditableTree', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders tree with nodes', () => {
    render(
      <EditableTree
        treeData={mockTreeData}
        onNodeEdit={mockOnNodeEdit}
        onNodeDelete={mockOnNodeDelete}
        onNodeAdd={mockOnNodeAdd}
        onNodeMove={mockOnNodeMove}
      />
    );

    // 检查是否渲染了节点名称
    expect(screen.getByText('Java专家')).toBeInTheDocument();
    expect(screen.getByText('L1初级')).toBeInTheDocument();
  });

  test('displays difficulty and hours tags', () => {
    render(
      <EditableTree
        treeData={mockTreeData}
        onNodeEdit={mockOnNodeEdit}
        onNodeDelete={mockOnNodeDelete}
        onNodeAdd={mockOnNodeAdd}
        onNodeMove={mockOnNodeMove}
      />
    );

    // 检查是否显示了难度和时长标签
    expect(screen.getByText('专家')).toBeInTheDocument();
    expect(screen.getByText('2000h')).toBeInTheDocument();
    expect(screen.getByText('初级')).toBeInTheDocument();
    expect(screen.getByText('200h')).toBeInTheDocument();
  });

  test('renders empty tree when no data provided', () => {
    render(
      <EditableTree
        treeData={[]}
        onNodeEdit={mockOnNodeEdit}
        onNodeDelete={mockOnNodeDelete}
        onNodeAdd={mockOnNodeAdd}
        onNodeMove={mockOnNodeMove}
      />
    );

    // 树应该为空但组件应该正常渲染
    const treeContainer = document.querySelector('.ant-tree');
    expect(treeContainer).toBeInTheDocument();
  });
});

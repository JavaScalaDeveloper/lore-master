import React from 'react';
import './TabBar.css';

interface TabItem {
  id: string;
  title: string;
  icon: string;
  activeIcon: string;
}

interface TabBarProps {
  activeTab: string;
  onTabChange: (tabId: string) => void;
}

const TabBar: React.FC<TabBarProps> = ({ activeTab, onTabChange }) => {
  const tabs: TabItem[] = [
    {
      id: 'home',
      title: '主页',
      icon: '🏠',
      activeIcon: '🏠'
    },
    {
      id: 'study',
      title: '学习',
      icon: '📚',
      activeIcon: '📚'
    },
    {
      id: 'profile',
      title: '个人中心',
      icon: '👤',
      activeIcon: '👤'
    }
  ];

  return (
    <div className="tab-bar">
      {tabs.map(tab => (
        <div
          key={tab.id}
          className={`tab-item ${activeTab === tab.id ? 'active' : ''}`}
          onClick={() => onTabChange(tab.id)}
        >
          <div className="tab-icon">
            {activeTab === tab.id ? tab.activeIcon : tab.icon}
          </div>
          <div className="tab-title">{tab.title}</div>
        </div>
      ))}
    </div>
  );
};

export default TabBar;

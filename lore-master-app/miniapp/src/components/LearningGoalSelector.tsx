import React, { useState } from 'react';
import './LearningGoalSelector.css';

interface LearningGoal {
  id: string;
  name: string;
  category: string;
  subcategory: string;
}

interface LearningGoalSelectorProps {
  isVisible: boolean;
  onClose: () => void;
  onSave: (goal: LearningGoal) => void;
  currentGoal?: LearningGoal | null;
}

const LearningGoalSelector: React.FC<LearningGoalSelectorProps> = ({
  isVisible,
  onClose,
  onSave,
  currentGoal
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedSubcategory, setSelectedSubcategory] = useState<string>('');
  const [selectedGoal, setSelectedGoal] = useState<string>('');

  // 学习目标数据结构
  const learningGoals = {
    '外语': {
      icon: '🌍',
      subcategories: {
        '英语': {
          icon: '🇺🇸',
          goals: [
            { id: 'cet4', name: '大学英语CET-4' },
            { id: 'cet6', name: '大学英语CET-6' },
            { id: 'ielts', name: '雅思' },
            { id: 'toefl', name: '托福' }
          ]
        }
      }
    },
    'IT/互联网': {
      icon: '💻',
      subcategories: {
        '后端': {
          icon: '⚙️',
          goals: [
            { id: 'java', name: 'Java' },
            { id: 'bigdata', name: '大数据开发工程师' },
            { id: 'python', name: 'Python' },
            { id: 'golang', name: 'Golang' }
          ]
        },
        '前端': {
          icon: '🎨',
          goals: [
            { id: 'react-vue', name: 'React/Vue' },
            { id: 'mobile', name: 'IOS/Android' }
          ]
        }
      }
    },
    '财务/审计/税务': {
      icon: '💰',
      subcategories: {
        '财务': {
          icon: '📊',
          goals: [
            { id: 'cpa', name: '注册会计师CPA' }
          ]
        },
        '审计': {
          icon: '🔍',
          goals: [
            { id: 'cia', name: '国际注册内部审计师CIA' }
          ]
        },
        '税务': {
          icon: '📋',
          goals: [
            { id: 'cta', name: '税务师CTA' }
          ]
        }
      }
    }
  };

  // 重置选择状态
  const resetSelection = () => {
    setSelectedCategory('');
    setSelectedSubcategory('');
    setSelectedGoal('');
  };

  // 处理关闭
  const handleClose = () => {
    resetSelection();
    onClose();
  };

  // 处理保存
  const handleSave = () => {
    if (selectedCategory && selectedSubcategory && selectedGoal) {
      const categoryData = learningGoals[selectedCategory as keyof typeof learningGoals];
      const subcategoryData = categoryData.subcategories[selectedSubcategory as keyof typeof categoryData.subcategories];

      const goalInfo = (subcategoryData as any).goals.find((g: any) => g.id === selectedGoal);

      if (goalInfo) {
        const goal: LearningGoal = {
          id: selectedGoal,
          name: goalInfo.name,
          category: selectedCategory,
          subcategory: selectedSubcategory
        };

        onSave(goal);
        handleClose();
      }
    }
  };

  // 处理分类选择
  const handleCategorySelect = (category: string) => {
    setSelectedCategory(category);
    setSelectedSubcategory('');
    setSelectedGoal('');
  };

  // 处理子分类选择
  const handleSubcategorySelect = (subcategory: string) => {
    setSelectedSubcategory(subcategory);
    setSelectedGoal('');
  };

  // 处理目标选择
  const handleGoalSelect = (goalId: string) => {
    setSelectedGoal(goalId);
  };

  if (!isVisible) return null;

  return (
    <div className="learning-goal-overlay" onClick={handleClose}>
      <div className="learning-goal-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>🎯 选择学习目标</h2>
          <button className="close-button" onClick={handleClose}>✕</button>
        </div>

        <div className="modal-content">
          {/* 面包屑导航 */}
          <div className="breadcrumb">
            <span className={selectedCategory ? 'active' : ''}>选择领域</span>
            {selectedCategory && (
              <>
                <span className="separator">›</span>
                <span className={selectedSubcategory ? 'active' : ''}>选择方向</span>
              </>
            )}
            {selectedSubcategory && (
              <>
                <span className="separator">›</span>
                <span className={selectedGoal ? 'active' : ''}>选择目标</span>
              </>
            )}
          </div>

          {/* 第一级：选择分类 */}
          {!selectedCategory && (
            <div className="selection-level">
              <h3>请选择学习领域</h3>
              <div className="options-grid">
                {Object.entries(learningGoals).map(([category, data]) => (
                  <div
                    key={category}
                    className="option-card category-card"
                    onClick={() => handleCategorySelect(category)}
                  >
                    <div className="option-icon">{data.icon}</div>
                    <div className="option-title">{category}</div>
                    <div className="option-arrow">›</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 第二级：选择子分类 */}
          {selectedCategory && !selectedSubcategory && (
            <div className="selection-level">
              <div className="level-header">
                <button 
                  className="back-button"
                  onClick={() => setSelectedCategory('')}
                >
                  ‹ 返回
                </button>
                <h3>{selectedCategory} - 选择专业方向</h3>
              </div>
              <div className="options-grid">
                {Object.entries(learningGoals[selectedCategory as keyof typeof learningGoals].subcategories).map(([subcategory, data]) => (
                  <div
                    key={subcategory}
                    className="option-card subcategory-card"
                    onClick={() => handleSubcategorySelect(subcategory)}
                  >
                    <div className="option-icon">{data.icon}</div>
                    <div className="option-title">{subcategory}</div>
                    <div className="option-count">{data.goals.length}个目标</div>
                    <div className="option-arrow">›</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 第三级：选择具体目标 */}
          {selectedCategory && selectedSubcategory && (
            <div className="selection-level">
              <div className="level-header">
                <button 
                  className="back-button"
                  onClick={() => setSelectedSubcategory('')}
                >
                  ‹ 返回
                </button>
                <h3>{selectedSubcategory} - 选择学习目标</h3>
              </div>
              <div className="options-list">
                {(() => {
                  const categoryData = learningGoals[selectedCategory as keyof typeof learningGoals];
                  const subcategoryData = categoryData.subcategories[selectedSubcategory as keyof typeof categoryData.subcategories];
                  return (subcategoryData as any).goals.map((goal: any) => (
                  <div
                    key={goal.id}
                    className={`option-item ${selectedGoal === goal.id ? 'selected' : ''}`}
                    onClick={() => handleGoalSelect(goal.id)}
                  >
                    <div className="option-content">
                      <div className="option-title">{goal.name}</div>
                      <div className="option-path">
                        {selectedCategory} › {selectedSubcategory}
                      </div>
                    </div>
                    <div className="option-radio">
                      {selectedGoal === goal.id && '✓'}
                    </div>
                  </div>
                  ));
                })()}
              </div>
            </div>
          )}
        </div>

        {/* 底部操作栏 */}
        <div className="modal-footer">
          <button className="cancel-button" onClick={handleClose}>
            取消
          </button>
          <button 
            className={`save-button ${selectedGoal ? 'enabled' : 'disabled'}`}
            onClick={handleSave}
            disabled={!selectedGoal}
          >
            保存目标
          </button>
        </div>
      </div>
    </div>
  );
};

export default LearningGoalSelector;

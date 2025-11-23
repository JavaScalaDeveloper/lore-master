import React, { useState } from 'react';
import './App.css';
import TabBar from './components/TabBar';
import Home from './pages/Home';
import Assessment from './pages/Assessment';
import Study from './pages/Study';
import Profile from './pages/Profile';

function App() {
  const [activeTab, setActiveTab] = useState('home');

  const renderContent = () => {
    switch (activeTab) {
      case 'home':
        return <Home />;
      case 'assessment':
        return <Assessment />;
      case 'study':
        return <Study />;
      case 'profile':
        return <Profile />;
      default:
        return <Home />;
    }
  };

  return (
    <div className="App">
      <div className="app-content">
        {renderContent()}
      </div>
      <TabBar activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
}

export default App;

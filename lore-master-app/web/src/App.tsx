import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/admin/home/Login';
import Home from './pages/admin/home';
import BusinessHomePage from './pages/business';
import ConsumerPage from './pages/consumer';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 检查登录状态
    const checkLoginStatus = () => {
      const token = localStorage.getItem('adminToken');
      const user = localStorage.getItem('adminUser');
      const loggedIn = !!(token && user);
      console.log('🔍 检查登录状态:', { token: !!token, user: !!user, loggedIn });
      setIsLoggedIn(loggedIn);
      setLoading(false);
    };

    checkLoginStatus();

    // 监听storage变化
    window.addEventListener('storage', checkLoginStatus);

    // 监听自定义事件（用于同一页面内的状态更新）
    const handleLoginStateChange = () => {
      console.log('🔄 收到登录状态变化事件');
      checkLoginStatus();
    };

    window.addEventListener('loginStateChange', handleLoginStateChange);

    return () => {
      window.removeEventListener('storage', checkLoginStatus);
      window.removeEventListener('loginStateChange', handleLoginStateChange);
    };
  }, []);

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '16px'
      }}>
        加载中...
      </div>
    );
  }

  return (
    <Router>
      <Routes>
        {/* C端用户路由 */}
        <Route path="/" element={<ConsumerPage />} />
        <Route path="/consumer" element={<ConsumerPage />} />

        {/* 业务端路由 */}
        <Route path="/business" element={<BusinessHomePage />} />

        {/* 管理端路由 */}
        <Route path="/admin/login" element={!isLoggedIn ? <Login /> : <Navigate to="/admin/home" replace />} />
        <Route path="/admin/home" element={isLoggedIn ? <Home /> : <Navigate to="/admin/login" replace />} />

        {/* 兼容旧路由 */}
        <Route path="/login" element={<Navigate to="/admin/login" replace />} />
        <Route path="/home" element={<Navigate to="/admin/home" replace />} />

        {/* 默认路由 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;

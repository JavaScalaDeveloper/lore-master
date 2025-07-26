import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/admin/home/Login';
import Home from './pages/admin/home';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 检查登录状态
    const checkLoginStatus = () => {
      const token = localStorage.getItem('adminToken');
      const user = localStorage.getItem('adminUser');
      setIsLoggedIn(!!(token && user));
      setLoading(false);
    };

    checkLoginStatus();

    // 监听storage变化
    window.addEventListener('storage', checkLoginStatus);
    return () => window.removeEventListener('storage', checkLoginStatus);
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
        <Route path="/login" element={!isLoggedIn ? <Login /> : <Navigate to="/home" replace />} />
        <Route path="/home" element={isLoggedIn ? <Home /> : <Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to={isLoggedIn ? "/home" : "/login"} replace />} />
      </Routes>
    </Router>
  );
}

export default App;

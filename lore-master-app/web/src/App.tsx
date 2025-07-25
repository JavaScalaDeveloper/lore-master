import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/admin/home/Login';
import Home from './pages/admin/home';

function App() {
  const isLoggedIn = !!localStorage.getItem('adminUser');
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/home" element={isLoggedIn ? <Home /> : <Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to={isLoggedIn ? "/home" : "/login"} replace />} />
      </Routes>
    </Router>
  );
}

export default App;

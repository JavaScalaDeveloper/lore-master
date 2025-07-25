import React from 'react';

const Home: React.FC = () => {
  return (
    <div style={{ width: 600, margin: '120px auto', padding: 32, boxShadow: '0 2px 8px #f0f1f2', borderRadius: 8, background: '#fff' }}>
      <h2 style={{ textAlign: 'center', marginBottom: 24 }}>欢迎来到管理后台首页</h2>
      <p style={{ textAlign: 'center' }}>您已成功登录。</p>
    </div>
  );
};

export default Home; 
import React, { useEffect, useState } from 'react';
import { Table, message } from 'antd';
import axios from 'axios';

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id' },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
];

const UserManage: React.FC = () => {
  const [users, setUsers] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [loading, setLoading] = useState(false);

  const fetchUsers = async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const token = localStorage.getItem('adminToken');
      const res = await axios.post('/api/admin/users/page', {
        page,
        pageSize,
      }, {
        withCredentials: true,
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (res.data && res.data.success) {
        setUsers(res.data.data.content || res.data.data.records || res.data.data);
        setPagination({
          current: page,
          pageSize,
          total: res.data.total || res.data.data.totalElements || res.data.data.total || 0,
        });
      } else {
        message.error(res.data.message || '获取用户列表失败');
      }
    } catch (e) {
      message.error('网络错误');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(1, pagination.pageSize);
    // eslint-disable-next-line
  }, []);

  const handleTableChange = (pagination: any) => {
    fetchUsers(pagination.current, pagination.pageSize);
  };

  return (
      <>
        <h2>用户管理</h2>
        <Table
            rowKey="id"
            columns={columns}
            dataSource={users}
            loading={loading}
            pagination={pagination}
            onChange={handleTableChange}
        />
      </>
  );
};

export default UserManage; 
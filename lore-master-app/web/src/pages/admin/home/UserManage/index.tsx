import React, { useEffect, useState } from 'react';
import { Table, message } from 'antd';
import { adminApi } from '../../../../utils/request';
import { API_PATHS } from '../../../../config/api';

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
      const res = await adminApi.post(API_PATHS.ADMIN.USERS.PAGE, {
        current: page,
        size: pageSize,
      });

      // 检查新的响应格式：{ code: 200, message: "success", data: {...} }
      if (res.data && res.data.code === 200) {
        const responseData = res.data.data;
        setUsers(responseData.records || responseData.content || []);
        setPagination({
          current: page,
          pageSize,
          total: responseData.total || responseData.totalElements || 0,
        });
      } else {
        message.error(res.data.message || '获取用户列表失败');
      }
    } catch (error: any) {
      console.error('获取用户列表错误:', error);
      if (error.response && error.response.data) {
        message.error(error.response.data.message || '获取用户列表失败');
      } else {
        message.error('网络错误，请检查网络连接');
      }
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
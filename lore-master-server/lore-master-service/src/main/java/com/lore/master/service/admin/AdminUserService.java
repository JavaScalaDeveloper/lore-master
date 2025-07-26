package com.lore.master.service.admin;

import com.lore.master.data.dto.LoginRequest;
import com.lore.master.data.entity.AdminUser;
import com.lore.master.data.vo.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService {
    
    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest, String clientIp);
    
    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    AdminUser getById(Long id);
    
    /**
     * 分页查询管理员用户
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<AdminUser> getAdminUserPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建管理员用户
     *
     * @param adminUser 用户信息
     * @return 是否成功
     */
    boolean createAdminUser(AdminUser adminUser);
    
    /**
     * 更新管理员用户
     *
     * @param adminUser 用户信息
     * @return 是否成功
     */
    boolean updateAdminUser(AdminUser adminUser);
    
    /**
     * 删除管理员用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteAdminUser(Long id);
    
    /**
     * 批量删除管理员用户
     *
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean batchDeleteAdminUsers(Long[] ids);
    
    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 重置密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
}

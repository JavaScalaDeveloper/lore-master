package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
    
    /**
     * 分页查询用户
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<User> getUserPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(User user);
    
    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);
    
    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean batchDeleteUsers(Long[] ids);
    
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
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param clientIp 客户端IP
     * @return 用户信息
     */
    User login(String username, String password, String clientIp);
    
    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取用户等级分布
     *
     * @return 等级分布
     */
    Map<String, Object> getUserLevelDistribution();
    
    /**
     * 获取用户学历分布
     *
     * @return 学历分布
     */
    Map<String, Object> getUserEducationDistribution();
}

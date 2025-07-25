package com.lore.master.data.repository;

import com.lore.master.data.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员用户Repository
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    
    /**
     * 根据用户名查询用户
     */
    Optional<AdminUser> findByUsernameAndStatus(String username, Integer status);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<AdminUser> findByEmailAndStatus(String email, Integer status);
    
    /**
     * 根据手机号查询用户
     */
    Optional<AdminUser> findByPhoneAndStatus(String phone, Integer status);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndIdNot(String username, Long id);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhoneAndIdNot(String phone, Long id);
    
    /**
     * 分页查询管理员用户
     */
    @Query("SELECT u FROM AdminUser u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<AdminUser> findByConditions(@Param("username") String username,
                                   @Param("realName") String realName,
                                   @Param("email") String email,
                                   @Param("role") String role,
                                   @Param("status") Integer status,
                                   Pageable pageable);
    
    /**
     * 获取统计信息
     */
    @Query("SELECT " +
           "COUNT(u) as totalCount, " +
           "SUM(CASE WHEN u.status = 1 THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN u.status = 0 THEN 1 ELSE 0 END) as inactiveCount, " +
           "SUM(CASE WHEN DATE(u.createTime) = CURRENT_DATE THEN 1 ELSE 0 END) as todayCount " +
           "FROM AdminUser u")
    Object[] getStatistics();
}

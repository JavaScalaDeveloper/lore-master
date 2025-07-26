package com.lore.master.data.repository.admin;

import com.lore.master.data.entity.admin.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsernameAndStatus(String username, Integer status);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmailAndStatus(String email, Integer status);
    
    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhoneAndStatus(String phone, Integer status);
    
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
     * 分页查询用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:phone IS NULL OR u.phone LIKE %:phone%) AND " +
           "(:gender IS NULL OR u.gender = :gender) AND " +
           "(:education IS NULL OR u.education = :education) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findByConditions(@Param("username") String username,
                               @Param("realName") String realName,
                               @Param("email") String email,
                               @Param("phone") String phone,
                               @Param("gender") Integer gender,
                               @Param("education") Integer education,
                               @Param("status") Integer status,
                               Pageable pageable);
    
    /**
     * 获取用户统计信息
     */
    @Query("SELECT " +
           "COUNT(u) as totalCount, " +
           "SUM(CASE WHEN u.status = 1 THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN u.status = 0 THEN 1 ELSE 0 END) as inactiveCount, " +
           "SUM(CASE WHEN DATE(u.createTime) = CURRENT_DATE THEN 1 ELSE 0 END) as todayCount, " +
           "SUM(CASE WHEN u.gender = 1 THEN 1 ELSE 0 END) as maleCount, " +
           "SUM(CASE WHEN u.gender = 2 THEN 1 ELSE 0 END) as femaleCount " +
           "FROM User u")
    Object[] getUserStatistics();
    
    /**
     * 根据等级查询用户数量
     */
    @Query("SELECT u.currentLevel, COUNT(u) FROM User u WHERE u.status = 1 GROUP BY u.currentLevel ORDER BY u.currentLevel")
    Object[][] getUserCountByLevel();
    
    /**
     * 根据学历查询用户数量
     */
    @Query("SELECT u.education, COUNT(u) FROM User u WHERE u.status = 1 GROUP BY u.education ORDER BY u.education")
    Object[][] getUserCountByEducation();
}

package com.lore.master.data.repository.business;

import com.lore.master.data.entity.business.User;
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
     * 根据用户ID查询用户
     */
    Optional<User> findByUserId(String userId);
    
    /**
     * 根据用户ID和状态查询用户
     */
    Optional<User> findByUserIdAndStatus(String userId, Integer status);
    
    /**
     * 检查用户ID是否存在
     */
    boolean existsByUserId(String userId);
    
    /**
     * 根据昵称查询用户（模糊查询）
     */
    Optional<User> findByNicknameContaining(String nickname);
    
    /**
     * 统计指定状态的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") Integer status);
    
    /**
     * 检查用户ID是否已存在（用于生成唯一ID时检查）
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId")
    boolean existsByUserIdForGeneration(@Param("userId") String userId);
}

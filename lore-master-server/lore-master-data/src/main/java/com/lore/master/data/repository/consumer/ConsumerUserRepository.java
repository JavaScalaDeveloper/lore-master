package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ConsumerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * C端用户Repository
 */
@Repository
public interface ConsumerUserRepository extends JpaRepository<ConsumerUser, Long> {

    /**
     * 根据用户ID查找用户
     */
    Optional<ConsumerUser> findByUserId(String userId);

    /**
     * 检查用户ID是否存在
     */
    boolean existsByUserId(String userId);

    /**
     * 根据用户ID删除用户
     */
    void deleteByUserId(String userId);

    /**
     * 根据昵称查找用户
     */
    Optional<ConsumerUser> findByNickname(String nickname);

    /**
     * 检查昵称是否存在
     */
    boolean existsByNickname(String nickname);

    /**
     * 根据状态查找用户列表
     */
    @Query("SELECT u FROM ConsumerUser u WHERE u.status = :status ORDER BY u.createTime DESC")
    java.util.List<ConsumerUser> findByStatusOrderByCreateTimeDesc(@Param("status") Integer status);

    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(u) FROM ConsumerUser u WHERE u.status = 1")
    long countActiveUsers();

    /**
     * 根据等级统计用户数量
     */
    @Query("SELECT COUNT(u) FROM ConsumerUser u WHERE u.currentLevel = :level AND u.status = 1")
    long countUsersByLevel(@Param("level") Integer level);

    /**
     * 查找积分排行榜前N名用户
     */
    @Query("SELECT u FROM ConsumerUser u WHERE u.status = 1 ORDER BY u.totalScore DESC")
    java.util.List<ConsumerUser> findTopUsersByScore(org.springframework.data.domain.Pageable pageable);

    /**
     * 检查用户ID是否已存在（用于生成唯一ID时检查）
     */
    @Query("SELECT COUNT(u) > 0 FROM ConsumerUser u WHERE u.userId = :userId")
    boolean existsByUserIdForGeneration(@Param("userId") String userId);

    /**
     * 更新用户最后登录信息
     */
    @Query("UPDATE ConsumerUser u SET u.lastLoginTime = CURRENT_TIMESTAMP, u.lastLoginIp = :ip, u.loginCount = u.loginCount + 1 WHERE u.userId = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void updateLastLoginInfo(@Param("userId") String userId, @Param("ip") String ip);

    /**
     * 更新用户积分和等级
     */
    @Query("UPDATE ConsumerUser u SET u.totalScore = :score, u.currentLevel = :level WHERE u.userId = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void updateScoreAndLevel(@Param("userId") String userId, @Param("score") Integer score, @Param("level") Integer level);
}

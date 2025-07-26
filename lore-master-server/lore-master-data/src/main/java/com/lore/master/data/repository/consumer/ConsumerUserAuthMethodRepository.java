package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ConsumerUserAuthMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * C端用户认证方式Repository
 */
@Repository
public interface ConsumerUserAuthMethodRepository extends JpaRepository<ConsumerUserAuthMethod, Long> {

    /**
     * 根据认证类型和认证标识查找认证方式
     */
    Optional<ConsumerUserAuthMethod> findByAuthTypeAndAuthKey(String authType, String authKey);

    /**
     * 检查认证类型和认证标识是否存在
     */
    boolean existsByAuthTypeAndAuthKey(String authType, String authKey);

    /**
     * 根据用户ID查找所有认证方式
     */
    List<ConsumerUserAuthMethod> findByUserIdAndStatus(String userId, Integer status);

    /**
     * 根据用户ID查找主要认证方式
     */
    Optional<ConsumerUserAuthMethod> findByUserIdAndIsPrimaryAndStatus(String userId, Integer isPrimary, Integer status);

    /**
     * 根据第三方平台ID查找认证方式
     */
    Optional<ConsumerUserAuthMethod> findByThirdPartyIdAndAuthType(String thirdPartyId, String authType);

    /**
     * 根据UnionID查找认证方式
     */
    Optional<ConsumerUserAuthMethod> findByUnionId(String unionId);

    /**
     * 检查UnionID是否存在
     */
    boolean existsByUnionId(String unionId);

    /**
     * 根据用户ID和认证类型查找认证方式
     */
    Optional<ConsumerUserAuthMethod> findByUserIdAndAuthTypeAndStatus(String userId, String authType, Integer status);

    /**
     * 根据用户ID删除所有认证方式
     */
    void deleteByUserId(String userId);

    /**
     * 统计用户的认证方式数量
     */
    @Query("SELECT COUNT(uam) FROM ConsumerUserAuthMethod uam WHERE uam.userId = :userId AND uam.status = 1")
    long countByUserIdAndStatus(@Param("userId") String userId);

    /**
     * 查找用户的所有有效认证方式
     */
    @Query("SELECT uam FROM ConsumerUserAuthMethod uam WHERE uam.userId = :userId AND uam.status = 1 ORDER BY uam.isPrimary DESC, uam.createTime ASC")
    List<ConsumerUserAuthMethod> findValidAuthMethodsByUserId(@Param("userId") String userId);

    /**
     * 根据认证类型统计用户数量
     */
    @Query("SELECT COUNT(DISTINCT uam.userId) FROM ConsumerUserAuthMethod uam WHERE uam.authType = :authType AND uam.status = 1")
    long countDistinctUsersByAuthType(@Param("authType") String authType);

    /**
     * 查找指定认证类型的所有用户
     */
    @Query("SELECT DISTINCT uam.userId FROM ConsumerUserAuthMethod uam WHERE uam.authType = :authType AND uam.status = 1")
    List<String> findUserIdsByAuthType(@Param("authType") String authType);

    /**
     * 更新认证方式的最后使用时间
     */
    @Query("UPDATE ConsumerUserAuthMethod uam SET uam.lastUsedTime = CURRENT_TIMESTAMP WHERE uam.userId = :userId AND uam.authType = :authType")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void updateLastUsedTime(@Param("userId") String userId, @Param("authType") String authType);

    /**
     * 设置主要认证方式（先清除其他主要标记，再设置新的）
     */
    @Query("UPDATE ConsumerUserAuthMethod uam SET uam.isPrimary = 0 WHERE uam.userId = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void clearPrimaryFlags(@Param("userId") String userId);

    /**
     * 设置指定认证方式为主要方式
     */
    @Query("UPDATE ConsumerUserAuthMethod uam SET uam.isPrimary = 1 WHERE uam.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void setPrimary(@Param("id") Long id);
}

package com.lore.master.data.repository.business;

import com.lore.master.data.entity.business.UserAuthMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户登录方式Repository
 */
@Repository
public interface UserAuthMethodRepository extends JpaRepository<UserAuthMethod, Long> {
    
    /**
     * 根据认证类型和认证标识查询
     */
    Optional<UserAuthMethod> findByAuthTypeAndAuthKey(String authType, String authKey);
    
    /**
     * 根据认证类型和认证标识查询（包含状态）
     */
    Optional<UserAuthMethod> findByAuthTypeAndAuthKeyAndStatus(String authType, String authKey, Integer status);
    
    /**
     * 根据用户ID查询所有登录方式
     */
    List<UserAuthMethod> findByUserIdAndStatus(String userId, Integer status);
    
    /**
     * 根据用户ID和认证类型查询
     */
    Optional<UserAuthMethod> findByUserIdAndAuthType(String userId, String authType);
    
    /**
     * 根据第三方ID查询
     */
    Optional<UserAuthMethod> findByThirdPartyIdAndStatus(String thirdPartyId, Integer status);
    
    /**
     * 根据UnionID查询
     */
    Optional<UserAuthMethod> findByUnionIdAndStatus(String unionId, Integer status);
    
    /**
     * 检查认证方式是否已存在
     */
    boolean existsByAuthTypeAndAuthKey(String authType, String authKey);
    
    /**
     * 获取用户的主要登录方式
     */
    @Query("SELECT uam FROM UserAuthMethod uam WHERE uam.userId = :userId AND uam.isPrimary = 1 AND uam.status = 1")
    Optional<UserAuthMethod> findPrimaryAuthMethod(@Param("userId") String userId);
}

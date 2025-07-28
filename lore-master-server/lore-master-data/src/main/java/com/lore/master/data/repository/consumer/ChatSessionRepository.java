package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天会话Repository
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * 根据会话ID查找会话
     */
    Optional<ChatSession> findBySessionId(String sessionId);

    /**
     * 根据用户ID查找会话列表（按最后活跃时间排序）
     */
    List<ChatSession> findByUserIdAndIsDeletedFalseOrderByLastActiveTimeDesc(String userId);

    /**
     * 根据用户ID分页查找会话列表
     */
    Page<ChatSession> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    /**
     * 根据用户ID和会话类型查找会话列表
     */
    List<ChatSession> findByUserIdAndSessionTypeAndIsDeletedFalseOrderByLastActiveTimeDesc(String userId, String sessionType);

    /**
     * 根据用户ID和状态查找会话列表
     */
    List<ChatSession> findByUserIdAndStatusAndIsDeletedFalseOrderByLastActiveTimeDesc(String userId, String status);

    /**
     * 统计用户的会话数量
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.isDeleted = false")
    Long countByUserId(@Param("userId") String userId);

    /**
     * 统计用户指定类型的会话数量
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.sessionType = :sessionType AND s.isDeleted = false")
    Long countByUserIdAndSessionType(@Param("userId") String userId, @Param("sessionType") String sessionType);

    /**
     * 根据时间范围查找会话
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.createdTime BETWEEN :startTime AND :endTime AND s.isDeleted = false ORDER BY s.lastActiveTime DESC")
    List<ChatSession> findByUserIdAndTimeRange(@Param("userId") String userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查找用户最近的会话
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.isDeleted = false ORDER BY s.lastActiveTime DESC LIMIT :limit")
    List<ChatSession> findRecentSessionsByUserId(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 更新会话的最后活跃时间
     */
    @Modifying
    @Query("UPDATE ChatSession s SET s.lastActiveTime = :lastActiveTime WHERE s.sessionId = :sessionId")
    void updateLastActiveTime(@Param("sessionId") String sessionId, @Param("lastActiveTime") LocalDateTime lastActiveTime);

    /**
     * 更新会话的消息数量
     */
    @Modifying
    @Query("UPDATE ChatSession s SET s.messageCount = :messageCount WHERE s.sessionId = :sessionId")
    void updateMessageCount(@Param("sessionId") String sessionId, @Param("messageCount") Integer messageCount);

    /**
     * 更新会话的Token使用量
     */
    @Modifying
    @Query("UPDATE ChatSession s SET s.totalTokens = :totalTokens WHERE s.sessionId = :sessionId")
    void updateTotalTokens(@Param("sessionId") String sessionId, @Param("totalTokens") Integer totalTokens);

    /**
     * 软删除会话
     */
    @Modifying
    @Query("UPDATE ChatSession s SET s.isDeleted = true WHERE s.sessionId = :sessionId")
    void softDeleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 归档会话
     */
    @Modifying
    @Query("UPDATE ChatSession s SET s.status = 'archived' WHERE s.sessionId = :sessionId")
    void archiveSession(@Param("sessionId") String sessionId);

    /**
     * 根据标题模糊查询会话
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.title LIKE %:keyword% AND s.isDeleted = false ORDER BY s.lastActiveTime DESC")
    List<ChatSession> findByUserIdAndTitleContaining(@Param("userId") String userId, @Param("keyword") String keyword);
}

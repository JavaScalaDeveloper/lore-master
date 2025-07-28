package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天消息Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 根据消息ID查找消息
     */
    Optional<ChatMessage> findByMessageId(String messageId);

    /**
     * 根据会话ID查找消息列表（按创建时间排序）
     */
    List<ChatMessage> findBySessionIdAndIsDeletedFalseOrderByCreatedTimeAsc(String sessionId);

    /**
     * 根据会话ID分页查找消息列表
     */
    Page<ChatMessage> findBySessionIdAndIsDeletedFalse(String sessionId, Pageable pageable);

    /**
     * 根据用户ID查找消息列表
     */
    List<ChatMessage> findByUserIdAndIsDeletedFalseOrderByCreatedTimeDesc(String userId);

    /**
     * 根据用户ID分页查找消息列表
     */
    Page<ChatMessage> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    /**
     * 统计会话中的消息数量
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.isDeleted = false")
    Long countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计用户的消息数量
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.userId = :userId AND m.isDeleted = false")
    Long countByUserId(@Param("userId") String userId);

    /**
     * 根据时间范围查找消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.userId = :userId AND m.createdTime BETWEEN :startTime AND :endTime AND m.isDeleted = false ORDER BY m.createdTime DESC")
    List<ChatMessage> findByUserIdAndTimeRange(@Param("userId") String userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据消息类型查找消息
     */
    List<ChatMessage> findBySessionIdAndMessageTypeAndIsDeletedFalseOrderByCreatedTimeAsc(String sessionId, String messageType);

    /**
     * 查找会话中的最后一条消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.isDeleted = false ORDER BY m.createdTime DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计会话中的Token使用量
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.isDeleted = false")
    Integer sumTokensBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计用户的Token使用量
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m WHERE m.userId = :userId AND m.isDeleted = false")
    Integer sumTokensByUserId(@Param("userId") String userId);

    /**
     * 根据状态查找消息
     */
    List<ChatMessage> findByStatusAndIsDeletedFalse(String status);

    /**
     * 软删除消息
     */
    @Query("UPDATE ChatMessage m SET m.isDeleted = true WHERE m.messageId = :messageId")
    void softDeleteByMessageId(@Param("messageId") String messageId);

    /**
     * 软删除会话中的所有消息
     */
    @Query("UPDATE ChatMessage m SET m.isDeleted = true WHERE m.sessionId = :sessionId")
    void softDeleteBySessionId(@Param("sessionId") String sessionId);
}

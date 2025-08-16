package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户聊天消息Repository
 */
@Repository("consumerChatMessageRepository")
public interface ConsumerChatMessageRepository extends JpaRepository<ConsumerChatMessage, Long> {

    /**
     * 根据用户ID和会话ID查询消息（分页，按时间倒序）
     */
    List<ConsumerChatMessage> findByUserIdAndSessionIdOrderByCreateTimeDesc(
            String userId, String sessionId, Pageable pageable);

    /**
     * 根据用户ID和会话ID查询消息（按时间正序）
     */
    List<ConsumerChatMessage> findByUserIdAndSessionIdOrderByCreateTimeAsc(
            String userId, String sessionId);

    /**
     * 根据用户ID和会话ID查询消息（分页，按时间正序）
     */
    List<ConsumerChatMessage> findByUserIdAndSessionIdOrderByCreateTimeAsc(
            String userId, String sessionId, Pageable pageable);

    /**
     * 根据用户ID和会话ID查询消息（分页，按时间正序）
     */
    List<ConsumerChatMessage> findByUserIdOrderByCreateTimeAsc(
            String userId, Pageable pageable);

    /**
     * 获取用户会话的最近N条消息（用于ChatMemory）
     */
    @Query("SELECT m FROM ConsumerChatMessage m WHERE m.userId = :userId AND m.sessionId = :sessionId " +
            "ORDER BY m.createTime DESC")
    List<ConsumerChatMessage> findRecentMessages(@Param("userId") String userId,
                                                 @Param("sessionId") String sessionId,
                                                 Pageable pageable);

    /**
     * 统计用户会话的消息数量
     */
    long countByUserIdAndSessionId(String userId, String sessionId);

    /**
     * 获取用户的所有会话ID
     */
    @Query("SELECT DISTINCT m.sessionId FROM ConsumerChatMessage m WHERE m.userId = :userId ORDER BY MAX(m.createTime) DESC")
    List<String> findDistinctSessionIdsByUserId(@Param("userId") String userId);
}

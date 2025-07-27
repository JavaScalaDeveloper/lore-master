package com.lore.master.data.repository.storage;

import com.lore.master.data.entity.storage.FileAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件访问日志Repository
 */
@Repository
public interface FileAccessLogRepository extends JpaRepository<FileAccessLog, Long> {

    /**
     * 根据文件ID查找访问日志
     */
    Page<FileAccessLog> findByFileIdOrderByAccessTimeDesc(String fileId, Pageable pageable);

    /**
     * 根据访问用户查找日志
     */
    Page<FileAccessLog> findByAccessUserIdAndAccessUserTypeOrderByAccessTimeDesc(
            String accessUserId, String accessUserType, Pageable pageable);

    /**
     * 统计文件访问次数
     */
    @Query("SELECT COUNT(l) FROM FileAccessLog l WHERE l.fileId = :fileId")
    Long countByFileId(@Param("fileId") String fileId);

    /**
     * 统计用户访问次数
     */
    @Query("SELECT COUNT(l) FROM FileAccessLog l WHERE l.accessUserId = :userId AND l.accessUserType = :userType")
    Long countByUser(@Param("userId") String userId, @Param("userType") String userType);

    /**
     * 统计指定时间范围内的访问次数
     */
    @Query("SELECT COUNT(l) FROM FileAccessLog l WHERE l.fileId = :fileId AND l.accessTime BETWEEN :startTime AND :endTime")
    Long countByFileIdAndTimeRange(@Param("fileId") String fileId, 
                                  @Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找热门文件（按访问次数排序）
     */
    @Query("SELECT l.fileId, COUNT(l) as accessCount FROM FileAccessLog l " +
           "WHERE l.accessTime >= :startTime " +
           "GROUP BY l.fileId " +
           "ORDER BY accessCount DESC")
    List<Object[]> findPopularFiles(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    /**
     * 根据IP地址查找访问日志
     */
    Page<FileAccessLog> findByAccessIpOrderByAccessTimeDesc(String accessIp, Pageable pageable);

    /**
     * 删除过期的访问日志
     */
    @Query("DELETE FROM FileAccessLog l WHERE l.accessTime < :expireTime")
    int deleteExpiredLogs(@Param("expireTime") LocalDateTime expireTime);
}

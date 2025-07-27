package com.lore.master.data.repository.storage;

import com.lore.master.data.entity.storage.FileStorage;
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
 * 文件存储Repository
 */
@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {

    /**
     * 根据文件ID查找文件
     */
    Optional<FileStorage> findByFileId(String fileId);

    /**
     * 根据MD5哈希值查找文件
     */
    Optional<FileStorage> findByMd5Hash(String md5Hash);

    /**
     * 根据文件分类查找文件
     */
    Page<FileStorage> findByFileCategoryAndStatus(String fileCategory, Integer status, Pageable pageable);

    /**
     * 根据上传用户查找文件
     */
    Page<FileStorage> findByUploadUserIdAndUploadUserTypeAndStatus(
            String uploadUserId, String uploadUserType, Integer status, Pageable pageable);

    /**
     * 根据存储桶查找文件
     */
    Page<FileStorage> findByBucketNameAndStatus(String bucketName, Integer status, Pageable pageable);

    /**
     * 查找公开文件
     */
    Page<FileStorage> findByIsPublicAndStatus(Boolean isPublic, Integer status, Pageable pageable);

    /**
     * 根据文件名模糊查询
     */
    @Query("SELECT f FROM FileStorage f WHERE f.originalName LIKE %:fileName% AND f.status = :status")
    Page<FileStorage> findByFileNameContaining(@Param("fileName") String fileName, 
                                             @Param("status") Integer status, 
                                             Pageable pageable);

    /**
     * 统计用户上传的文件数量
     */
    @Query("SELECT COUNT(f) FROM FileStorage f WHERE f.uploadUserId = :userId AND f.uploadUserType = :userType AND f.status = :status")
    Long countByUser(@Param("userId") String userId, 
                    @Param("userType") String userType, 
                    @Param("status") Integer status);

    /**
     * 统计用户上传的文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileStorage f WHERE f.uploadUserId = :userId AND f.uploadUserType = :userType AND f.status = :status")
    Long sumFileSizeByUser(@Param("userId") String userId, 
                          @Param("userType") String userType, 
                          @Param("status") Integer status);

    /**
     * 更新文件访问次数和最后访问时间
     */
    @Modifying
    @Query("UPDATE FileStorage f SET f.accessCount = f.accessCount + 1, f.lastAccessTime = :accessTime WHERE f.fileId = :fileId")
    int updateAccessInfo(@Param("fileId") String fileId, @Param("accessTime") LocalDateTime accessTime);

    /**
     * 软删除文件
     */
    @Modifying
    @Query("UPDATE FileStorage f SET f.status = 0 WHERE f.fileId = :fileId")
    int softDeleteByFileId(@Param("fileId") String fileId);

    /**
     * 批量软删除文件
     */
    @Modifying
    @Query("UPDATE FileStorage f SET f.status = 0 WHERE f.fileId IN :fileIds")
    int batchSoftDelete(@Param("fileIds") List<String> fileIds);

    /**
     * 查找过期的临时文件（用于清理）
     */
    @Query("SELECT f FROM FileStorage f WHERE f.bucketName = 'temp' AND f.createdTime < :expireTime AND f.status = 1")
    List<FileStorage> findExpiredTempFiles(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据文件类型统计
     */
    @Query("SELECT f.fileCategory, COUNT(f) FROM FileStorage f WHERE f.status = :status GROUP BY f.fileCategory")
    List<Object[]> countByFileCategory(@Param("status") Integer status);

    /**
     * 查找大文件（超过指定大小）
     */
    @Query("SELECT f FROM FileStorage f WHERE f.fileSize > :minSize AND f.status = :status ORDER BY f.fileSize DESC")
    Page<FileStorage> findLargeFiles(@Param("minSize") Long minSize, 
                                   @Param("status") Integer status, 
                                   Pageable pageable);

    /**
     * 查找最近上传的文件
     */
    @Query("SELECT f FROM FileStorage f WHERE f.status = :status ORDER BY f.createdTime DESC")
    Page<FileStorage> findRecentFiles(@Param("status") Integer status, Pageable pageable);

    /**
     * 根据文件ID和状态查找文件
     */
    Optional<FileStorage> findByFileIdAndStatus(String fileId, Integer status);

    /**
     * 根据文件ID统计数量
     */
    Long countByFileIdAndStatus(String fileId, Integer status);

    /**
     * 查找用户在指定存储桶中相同MD5的文件（用于重复上传检测）
     */
    @Query("SELECT f FROM FileStorage f WHERE f.md5Hash = :md5Hash AND f.uploadUserId = :userId AND f.bucketName = :bucketName AND f.status = 1 ORDER BY f.createdTime DESC")
    List<FileStorage> findByMd5HashAndUploadUserIdAndBucketNameForDuplicateCheck(
            @Param("md5Hash") String md5Hash,
            @Param("userId") String userId,
            @Param("bucketName") String bucketName);

    /**
     * 更新文件访问统计
     */
    @Modifying
    @Query("UPDATE FileStorage f SET f.accessCount = f.accessCount + 1, f.lastAccessTime = CURRENT_TIMESTAMP WHERE f.fileId = :fileId")
    int updateAccessCount(@Param("fileId") String fileId);
}

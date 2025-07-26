package com.lore.master.service.admin;

import java.util.List;
import java.util.Map;

/**
 * 向量数据库服务
 * 用于存储和检索用户学习画像、知识点向量等
 */
public interface VectorDatabaseService {
    
    /**
     * 存储用户学习画像向量
     * @param userId 用户ID
     * @param profileData 用户画像数据
     * @param embedding 向量表示
     * @return 向量ID
     */
    String storeUserProfile(Long userId, Map<String, Object> profileData, float[] embedding);
    
    /**
     * 存储知识点向量
     * @param knowledgePointId 知识点ID
     * @param knowledgeData 知识点数据
     * @param embedding 向量表示
     * @return 向量ID
     */
    String storeKnowledgePoint(Long knowledgePointId, Map<String, Object> knowledgeData, float[] embedding);
    
    /**
     * 存储学习内容向量
     * @param contentId 内容ID
     * @param contentData 内容数据
     * @param embedding 向量表示
     * @return 向量ID
     */
    String storeLearningContent(Long contentId, Map<String, Object> contentData, float[] embedding);
    
    /**
     * 存储测评结果向量
     * @param assessmentId 测评ID
     * @param resultData 结果数据
     * @param embedding 向量表示
     * @return 向量ID
     */
    String storeAssessmentResult(Long assessmentId, Map<String, Object> resultData, float[] embedding);
    
    /**
     * 基于用户画像查找相似用户
     * @param userId 用户ID
     * @param topK 返回前K个相似用户
     * @return 相似用户列表
     */
    List<Map<String, Object>> findSimilarUsers(Long userId, int topK);
    
    /**
     * 基于用户画像推荐知识点
     * @param userId 用户ID
     * @param topK 返回前K个推荐知识点
     * @return 推荐知识点列表
     */
    List<Map<String, Object>> recommendKnowledgePoints(Long userId, int topK);
    
    /**
     * 基于知识点查找相关内容
     * @param knowledgePointId 知识点ID
     * @param topK 返回前K个相关内容
     * @return 相关内容列表
     */
    List<Map<String, Object>> findRelatedContent(Long knowledgePointId, int topK);
    
    /**
     * 基于学习历史推荐学习路径
     * @param userId 用户ID
     * @param careerTargetId 职业目标ID
     * @param topK 返回前K个推荐路径
     * @return 推荐路径列表
     */
    List<Map<String, Object>> recommendLearningPaths(Long userId, Long careerTargetId, int topK);
    
    /**
     * 更新用户画像向量
     * @param userId 用户ID
     * @param newProfileData 新的画像数据
     * @param newEmbedding 新的向量表示
     * @return 是否更新成功
     */
    boolean updateUserProfile(Long userId, Map<String, Object> newProfileData, float[] newEmbedding);
    
    /**
     * 删除向量数据
     * @param vectorId 向量ID
     * @return 是否删除成功
     */
    boolean deleteVector(String vectorId);
    
    /**
     * 批量查询向量相似度
     * @param queryEmbedding 查询向量
     * @param entityType 实体类型
     * @param topK 返回前K个结果
     * @return 相似度结果列表
     */
    List<Map<String, Object>> searchSimilarVectors(float[] queryEmbedding, String entityType, int topK);
    
    /**
     * 获取向量数据库统计信息
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
}

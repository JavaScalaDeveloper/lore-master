package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.KnowledgePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 知识点服务接口
 */
public interface KnowledgePointService {
    
    /**
     * 根据ID查询知识点
     *
     * @param id 知识点ID
     * @return 知识点信息
     */
    KnowledgePoint getById(Long id);
    
    /**
     * 根据编码查询知识点
     *
     * @param code 知识点编码
     * @return 知识点信息
     */
    KnowledgePoint getByCode(String code);
    
    /**
     * 根据学科ID获取知识点
     *
     * @param subjectId 学科ID
     * @return 知识点列表
     */
    List<KnowledgePoint> getBySubjectId(Long subjectId);
    
    /**
     * 根据父级ID获取子知识点
     *
     * @param parentId 父级ID
     * @return 知识点列表
     */
    List<KnowledgePoint> getByParentId(Long parentId);
    
    /**
     * 分页查询知识点
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<KnowledgePoint> getKnowledgePointPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 是否成功
     */
    boolean createKnowledgePoint(KnowledgePoint knowledgePoint);
    
    /**
     * 更新知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 是否成功
     */
    boolean updateKnowledgePoint(KnowledgePoint knowledgePoint);
    
    /**
     * 删除知识点
     *
     * @param id 知识点ID
     * @return 是否成功
     */
    boolean deleteKnowledgePoint(Long id);
    
    /**
     * 批量删除知识点
     *
     * @param ids 知识点ID列表
     * @return 是否成功
     */
    boolean batchDeleteKnowledgePoints(Long[] ids);
    
    /**
     * 更新知识点状态
     *
     * @param id 知识点ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 根据标签搜索知识点
     *
     * @param tag 标签
     * @return 知识点列表
     */
    List<KnowledgePoint> searchByTag(String tag);
    
    /**
     * 全文搜索知识点
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<KnowledgePoint> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * 获取热门知识点
     *
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<KnowledgePoint> getPopularKnowledgePoints(Pageable pageable);
    
    /**
     * 增加浏览次数
     *
     * @param id 知识点ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 获取知识点统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getKnowledgePointStatistics();
    
    /**
     * 获取知识点学科分布
     *
     * @return 学科分布
     */
    Map<String, Object> getSubjectDistribution();
    
    /**
     * 获取知识点难度分布
     *
     * @return 难度分布
     */
    Map<String, Object> getDifficultyDistribution();
    
    /**
     * 获取知识点重要程度分布
     *
     * @return 重要程度分布
     */
    Map<String, Object> getImportanceDistribution();
    
    /**
     * 调整排序权重
     *
     * @param id 知识点ID
     * @param sortOrder 排序权重
     * @return 是否成功
     */
    boolean updateSortOrder(Long id, Integer sortOrder);
}

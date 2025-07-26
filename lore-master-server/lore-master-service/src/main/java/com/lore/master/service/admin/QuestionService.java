package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 题目服务接口
 */
public interface QuestionService {
    
    /**
     * 根据ID查询题目
     *
     * @param id 题目ID
     * @return 题目信息
     */
    Question getById(Long id);
    
    /**
     * 根据学科ID获取题目
     *
     * @param subjectId 学科ID
     * @return 题目列表
     */
    List<Question> getBySubjectId(Long subjectId);
    
    /**
     * 根据知识点ID获取题目
     *
     * @param knowledgePointId 知识点ID
     * @return 题目列表
     */
    List<Question> getByKnowledgePointId(Long knowledgePointId);
    
    /**
     * 分页查询题目
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Question> getQuestionPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建题目
     *
     * @param question 题目信息
     * @return 是否成功
     */
    boolean createQuestion(Question question);
    
    /**
     * 更新题目
     *
     * @param question 题目信息
     * @return 是否成功
     */
    boolean updateQuestion(Question question);
    
    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 是否成功
     */
    boolean deleteQuestion(Long id);
    
    /**
     * 批量删除题目
     *
     * @param ids 题目ID列表
     * @return 是否成功
     */
    boolean batchDeleteQuestions(Long[] ids);
    
    /**
     * 更新题目状态
     *
     * @param id 题目ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 根据标签搜索题目
     *
     * @param tag 标签
     * @return 题目列表
     */
    List<Question> searchByTag(String tag);
    
    /**
     * 全文搜索题目
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Question> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * 随机获取题目
     *
     * @param subjectId 学科ID
     * @param difficultyLevel 难度等级
     * @param type 题目类型
     * @param limit 数量限制
     * @return 题目列表
     */
    List<Question> getRandomQuestions(Long subjectId, Integer difficultyLevel, Integer type, Integer limit);
    
    /**
     * 增加使用次数
     *
     * @param id 题目ID
     * @return 是否成功
     */
    boolean incrementUsageCount(Long id);
    
    /**
     * 更新正确率
     *
     * @param id 题目ID
     * @param accuracyRate 正确率
     * @return 是否成功
     */
    boolean updateAccuracyRate(Long id, Double accuracyRate);
    
    /**
     * 获取题目统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getQuestionStatistics();
    
    /**
     * 获取题目类型分布
     *
     * @return 类型分布
     */
    Map<String, Object> getTypeDistribution();
    
    /**
     * 获取题目难度分布
     *
     * @return 难度分布
     */
    Map<String, Object> getDifficultyDistribution();
    
    /**
     * 获取题目学科分布
     *
     * @return 学科分布
     */
    Map<String, Object> getSubjectDistribution();
}

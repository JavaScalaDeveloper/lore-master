package com.lore.master.service.business;

import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.dto.business.CourseRequest;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.data.vo.business.CourseListPageVO;
import com.lore.master.data.vo.business.RecentLearningCourseVO;

import java.util.List;

/**
 * 课程业务服务接口
 */
public interface BusinessCourseService {

    /**
     * 分页查询课程（轻量级，不包含大字段）
     * 
     * @param queryDTO 查询条件
     * @return 分页课程数据
     */
    CourseListPageVO getCourses(CourseQueryDTO queryDTO);

    /**
     * 根据课程编码获取课程详情
     * 
     * @param courseCode 课程编码
     * @param userId 用户ID（可选，用于获取用户相关状态）
     * @return 课程详情
     */
    CourseVO getCourseByCode(String courseCode, String userId);

    /**
     * 根据课程ID获取课程详情
     * 
     * @param courseId 课程ID
     * @param userId 用户ID（可选，用于获取用户相关状态）
     * @return 课程详情
     */
    CourseVO getCourseById(Long courseId, String userId);

    /**
     * 获取合集下的子课程
     * 
     * @param parentCourseId 父课程ID
     * @param userId 用户ID（可选）
     * @return 子课程列表
     */
    List<CourseVO> getSubCourses(Long parentCourseId, String userId);

    /**
     * 搜索课程
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 搜索结果
     */
    CourseListPageVO searchCourses(String keyword, Integer page, Integer size, String userId);

    /**
     * 获取热门课程
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 热门课程列表
     */
    CourseListPageVO getPopularCourses(Integer page, Integer size, String userId);

    /**
     * 获取最新课程
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 最新课程列表
     */
    CourseListPageVO getLatestCourses(Integer page, Integer size, String userId);

    /**
     * 根据知识点路径获取相关课程
     * 
     * @param knowledgeNodePath 知识点路径
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 相关课程列表
     */
    CourseListPageVO getCoursesByKnowledgePath(String knowledgeNodePath, Integer page, Integer size, String userId);

    /**
     * 根据难度等级获取课程
     * 
     * @param difficultyLevel 难度等级
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 课程列表
     */
    CourseListPageVO getCoursesByDifficulty(String difficultyLevel, Integer page, Integer size, String userId);

    /**
     * 根据作者获取课程
     * 
     * @param author 作者
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 课程列表
     */
    CourseListPageVO getCoursesByAuthor(String author, Integer page, Integer size, String userId);

    /**
     * 获取课程统计信息
     * 
     * @return 统计信息
     */
    CoursePageVO.CourseStatisticsVO getCourseStatistics();

    /**
     * 增加课程观看次数
     *
     * @param courseId 课程ID
     * @param userId 用户ID（可选）
     */
    void incrementViewCount(Long courseId, String userId);

    /**
     * 保存用户学习记录
     *
     * @param userId 用户ID
     * @param courseCode 课程编码
     */
    void saveLearningRecord(String userId, String courseCode);

    /**
     * 获取用户最近学习的课程
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近学习的课程列表
     */
    List<RecentLearningCourseVO> getRecentLearningCourses(String userId, int limit);

    /**
     * 获取推荐课程（基于用户学习目标或历史）
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐课程列表
     */
    CourseListPageVO getRecommendedCourses(String userId, Integer page, Integer size);

    /**
     * 创建课程
     *
     * @param request 课程请求数据
     * @return 创建的课程详情
     */
    CourseVO createCourse(CourseRequest request);

    /**
     * 更新课程
     *
     * @param request 课程请求数据
     * @return 更新后的课程详情
     */
    CourseVO updateCourse(CourseRequest request);

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @return 是否删除成功
     */
    Boolean deleteCourse(Long courseId);

    /**
     * 增加点赞数
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     */
    void incrementLikeCount(Long courseId, String userId);

    /**
     * 增加收藏数
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     */
    void incrementCollectCount(Long courseId, String userId);
}

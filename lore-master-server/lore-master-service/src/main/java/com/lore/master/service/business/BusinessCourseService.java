package com.lore.master.service.business;

import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.CoursePageVO;

import java.util.List;

/**
 * 课程业务服务接口
 */
public interface BusinessCourseService {

    /**
     * 分页查询课程
     * 
     * @param queryDTO 查询条件
     * @return 分页课程数据
     */
    CoursePageVO getCourses(CourseQueryDTO queryDTO);

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
    CoursePageVO searchCourses(String keyword, Integer page, Integer size, String userId);

    /**
     * 获取热门课程
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 热门课程列表
     */
    CoursePageVO getPopularCourses(Integer page, Integer size, String userId);

    /**
     * 获取最新课程
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 最新课程列表
     */
    CoursePageVO getLatestCourses(Integer page, Integer size, String userId);

    /**
     * 根据知识点路径获取相关课程
     * 
     * @param knowledgeNodePath 知识点路径
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 相关课程列表
     */
    CoursePageVO getCoursesByKnowledgePath(String knowledgeNodePath, Integer page, Integer size, String userId);

    /**
     * 根据难度等级获取课程
     * 
     * @param difficultyLevel 难度等级
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 课程列表
     */
    CoursePageVO getCoursesByDifficulty(String difficultyLevel, Integer page, Integer size, String userId);

    /**
     * 根据作者获取课程
     * 
     * @param author 作者
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @return 课程列表
     */
    CoursePageVO getCoursesByAuthor(String author, Integer page, Integer size, String userId);

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
     * 获取推荐课程（基于用户学习目标或历史）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐课程列表
     */
    CoursePageVO getRecommendedCourses(String userId, Integer page, Integer size);
}

package com.lore.master.web.consumer.controller;

import com.lore.master.common.annotation.RequireLogin;
import com.lore.master.common.context.UserContext;
import com.lore.master.data.dto.business.*;
import com.lore.master.data.vo.business.ApiResponse;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.data.vo.business.CourseListPageVO;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.RecentLearningCourseVO;
import com.lore.master.service.business.BusinessCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/consumer/course")
@RequiredArgsConstructor
public class ConsumerCourseController {

    private final BusinessCourseService courseService;

    /**
     * 分页查询课程
     */
    @PostMapping("/queryCourseList")
    public ApiResponse<CourseListPageVO> queryCourseList(@RequestBody CourseQueryDTO queryDTO) {
        
        log.info("分页查询课程，参数：{}", queryDTO);
        
        try {
            CourseListPageVO result = courseService.getCourses(queryDTO);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询课程失败", e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据课程编码获取课程详情
     */
    @RequireLogin
    @PostMapping("/getCourseByCode")
    public ApiResponse<CourseVO> getCourseByCode(@RequestBody CourseDetailQueryDTO queryDTO) {

        log.info("根据课程编码获取课程详情，参数：{}", queryDTO);

        try {
            // 从UserContext获取当前登录用户ID
            String currentUserId = UserContext.getCurrentUserId();
            log.info("从UserContext获取到用户ID: {}", currentUserId);

            // 使用UserContext中的userId，如果没有则使用请求参数中的userId
            String effectiveUserId = currentUserId != null ? currentUserId : queryDTO.getUserId();

            CourseVO result = courseService.getCourseByCode(queryDTO.getCourseCode(), effectiveUserId);

            // 增加观看次数和保存学习记录
            if (result != null && effectiveUserId != null) {
                courseService.incrementViewCount(result.getId(), effectiveUserId);

                // 保存用户学习记录
                courseService.saveLearningRecord(effectiveUserId, result.getCourseCode());
                log.info("已保存用户学习记录，用户ID: {}, 课程编码: {}", effectiveUserId, result.getCourseCode());
            }

            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询课程详情失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户最近学习的课程
     */
    @RequireLogin
    @PostMapping("/getRecentLearningCourses")
    public ApiResponse<List<RecentLearningCourseVO>> getRecentLearningCourses() {
        log.info("获取用户最近学习的课程");

        try {
            // 从UserContext获取当前登录用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error("用户未登录");
            }

            List<RecentLearningCourseVO> recentCourses = courseService.getRecentLearningCourses(currentUserId, 3);
            return ApiResponse.success("查询成功", recentCourses);
        } catch (Exception e) {
            log.error("获取用户最近学习课程失败", e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据课程ID获取课程详情
     */
    @PostMapping("/getCourseById")
    public ApiResponse<CourseVO> getCourseById(@RequestBody CourseDetailQueryDTO queryDTO) {
        
        log.info("根据课程ID获取课程详情，参数：{}", queryDTO);
        
        try {
            CourseVO result = courseService.getCourseById(queryDTO.getCourseId(), queryDTO.getUserId());
            
            // 增加观看次数
            if (result != null) {
                courseService.incrementViewCount(queryDTO.getCourseId(), queryDTO.getUserId());
            }
            
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询课程详情失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取合集下的子课程
     */
    @PostMapping("/getSubCourses")
    public ApiResponse<List<CourseVO>> getSubCourses(@RequestBody SubCourseQueryDTO queryDTO) {
        
        log.info("获取合集子课程，参数：{}", queryDTO);
        
        try {
            List<CourseVO> result = courseService.getSubCourses(queryDTO.getParentCourseId(), queryDTO.getUserId());
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询子课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 搜索课程
     */
    @PostMapping("/searchCourses")
    public ApiResponse<CourseListPageVO> searchCourses(@RequestBody CourseSearchDTO searchDTO) {
        
        log.info("搜索课程，参数：{}", searchDTO);
        
        try {
            CourseListPageVO result = courseService.searchCourses(
                    searchDTO.getKeyword(), 
                    searchDTO.getPage(), 
                    searchDTO.getSize(), 
                    searchDTO.getUserId()
            );
            return ApiResponse.success("搜索成功", result);
        } catch (Exception e) {
            log.error("搜索课程失败，参数：{}", searchDTO, e);
            return ApiResponse.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门课程
     */
    @PostMapping("/getPopularCourses")
    public ApiResponse<CourseListPageVO> getPopularCourses(@RequestBody PageQueryDTO queryDTO) {
        
        log.info("获取热门课程，参数：{}", queryDTO);
        
        try {
            CourseListPageVO result = courseService.getPopularCourses(
                    queryDTO.getPage(), 
                    queryDTO.getSize(), 
                    queryDTO.getUserId()
            );
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询热门课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取最新课程
     */
    @PostMapping("/getLatestCourses")
    public ApiResponse<CourseListPageVO> getLatestCourses(@RequestBody PageQueryDTO queryDTO) {
        
        log.info("获取最新课程，参数：{}", queryDTO);
        
        try {
            CourseListPageVO result = courseService.getLatestCourses(
                    queryDTO.getPage(), 
                    queryDTO.getSize(), 
                    queryDTO.getUserId()
            );
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询最新课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据知识点路径获取相关课程
     */
    @PostMapping("/getCoursesByKnowledgePath")
    public ApiResponse<CourseListPageVO> getCoursesByKnowledgePath(@RequestBody KnowledgePathQueryDTO queryDTO) {
        
        log.info("根据知识点路径获取课程，参数：{}", queryDTO);
        
//        try {
//            CourseListPageVO result = courseService.getCoursesByKnowledgePath(
//                    queryDTO.getKnowledgeNodePath(),
//                    queryDTO.getPage(),
//                    queryDTO.getSize(),
//                    queryDTO.getUserId()
//            );
//            return ApiResponse.success("查询成功", result);
//        } catch (Exception e) {
//            log.error("根据知识点路径查询课程失败，参数：{}", queryDTO, e);
//            return ApiResponse.error("查询失败：" + e.getMessage());
//        }
        return null;
    }

    /**
     * 根据难度等级获取课程
     */
    @PostMapping("/getCoursesByDifficulty")
    public ApiResponse<CourseListPageVO> getCoursesByDifficulty(@RequestBody DifficultyQueryDTO queryDTO) {
        
        log.info("根据难度等级获取课程，参数：{}", queryDTO);
        
        try {
            CourseListPageVO result = courseService.getCoursesByDifficulty(
                    queryDTO.getDifficultyLevel(), 
                    queryDTO.getPage(), 
                    queryDTO.getSize(), 
                    queryDTO.getUserId()
            );
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("根据难度等级查询课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据作者获取课程
     */
    @PostMapping("/getCoursesByAuthor")
    public ApiResponse<CourseListPageVO> getCoursesByAuthor(@RequestBody AuthorQueryDTO queryDTO) {
        
        log.info("根据作者获取课程，参数：{}", queryDTO);
        
        try {
            CourseListPageVO result = courseService.getCoursesByAuthor(
                    queryDTO.getAuthor(), 
                    queryDTO.getPage(), 
                    queryDTO.getSize(), 
                    queryDTO.getUserId()
            );
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("根据作者查询课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取课程统计信息
     */
    @PostMapping("/getCourseStatistics")
    public ApiResponse<CoursePageVO.CourseStatisticsVO> getCourseStatistics() {
        
        log.info("获取课程统计信息");
        
        try {
            CoursePageVO.CourseStatisticsVO result = courseService.getCourseStatistics();
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取课程统计信息失败", e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取推荐课程
     */
    @PostMapping("/getRecommendedCourses")
    public ApiResponse<CourseListPageVO> getRecommendedCourses(@RequestBody PageQueryDTO queryDTO) {

        log.info("获取推荐课程，参数：{}", queryDTO);

        try {
            CourseListPageVO result = courseService.getRecommendedCourses(
                    queryDTO.getUserId(),
                    queryDTO.getPage(),
                    queryDTO.getSize()
            );
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取推荐课程失败，参数：{}", queryDTO, e);
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞课程
     */
    @RequireLogin
    @PostMapping("/like/{courseId}")
    public ApiResponse<Boolean> likeCourse(@PathVariable Long courseId, @RequestBody(required = false) PageQueryDTO queryDTO) {

        // 从UserContext获取当前登录用户ID
        String currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录");
        }

        log.info("点赞课程，courseId：{}，userId：{}", courseId, currentUserId);

        try {
            // 这里简化处理，实际应该有用户点赞记录表
            // 暂时只是增加点赞数
            courseService.incrementLikeCount(courseId, currentUserId);
            return ApiResponse.success("点赞成功", true);
        } catch (Exception e) {
            log.error("点赞课程失败，courseId：{}，userId：{}", courseId, currentUserId, e);
            return ApiResponse.error("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 收藏/取消收藏课程
     */
    @RequireLogin
    @PostMapping("/collect/{courseId}")
    public ApiResponse<Boolean> collectCourse(@PathVariable Long courseId, @RequestBody(required = false) PageQueryDTO queryDTO) {

        // 从UserContext获取当前登录用户ID
        String currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录");
        }

        log.info("收藏课程，courseId：{}，userId：{}", courseId, currentUserId);

        try {
            // 这里简化处理，实际应该有用户收藏记录表
            // 暂时只是增加收藏数
            courseService.incrementCollectCount(courseId, currentUserId);
            return ApiResponse.success("收藏成功", true);
        } catch (Exception e) {
            log.error("收藏课程失败，courseId：{}，userId：{}", courseId, currentUserId, e);
            return ApiResponse.error("收藏失败：" + e.getMessage());
        }
    }
}

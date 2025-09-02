package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.dto.business.CourseRequest;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.data.vo.business.CourseListPageVO;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.service.business.BusinessCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理端课程控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
@Validated
public class AdminCourseController {

    private final BusinessCourseService courseService;

    /**
     * 分页查询课程列表
     */
    @PostMapping("/list")
    public Result<CourseListPageVO> getCourseList(@Valid @RequestBody CourseQueryDTO queryDTO) {
        log.info("管理端查询课程列表，参数：{}", queryDTO);

        // 管理端查询不限制发布状态
        queryDTO.setPublishedOnly(false);

        CourseListPageVO result = courseService.getCourses(queryDTO);
        return Result.success(result);
    }

    /**
     * 根据课程ID获取课程详情
     */
    @GetMapping("/detail/{courseId}")
    public Result<CourseVO> getCourseDetail(@PathVariable Long courseId) {
        log.info("管理端获取课程详情，courseId：{}", courseId);

        CourseVO result = courseService.getCourseById(courseId, null);
        return Result.success(result);
    }

    /**
     * 根据课程编码获取课程详情
     */
    @GetMapping("/detail/code/{courseCode}")
    public Result<CourseVO> getCourseDetailByCode(@PathVariable String courseCode) {
        log.info("管理端根据编码获取课程详情，courseCode：{}", courseCode);

        CourseVO result = courseService.getCourseByCode(courseCode, null);
        return Result.success(result);
    }

    /**
     * 获取课程统计信息
     */
    @GetMapping("/statistics")
    public Result<CoursePageVO.CourseStatisticsVO> getCourseStatistics() {
        log.info("管理端获取课程统计信息");

        CoursePageVO.CourseStatisticsVO result = courseService.getCourseStatistics();
        return Result.success(result);
    }

    /**
     * 搜索课程
     */
    @PostMapping("/search")
    public Result<CourseListPageVO> searchCourses(@Valid @RequestBody CourseQueryDTO queryDTO) {
        log.info("管理端搜索课程，参数：{}", queryDTO);

        // 管理端搜索不限制发布状态
        queryDTO.setPublishedOnly(false);

        CourseListPageVO result = courseService.searchCourses(
            queryDTO.getKeyword(),
            queryDTO.getPage(),
            queryDTO.getSize(),
            null
        );
        return Result.success(result);
    }

    /**
     * 创建课程
     */
    @PostMapping("/create")
    public Result<CourseVO> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("管理端创建课程，参数：{}", request);

        CourseVO result = courseService.createCourse(request);
        return Result.success(result);
    }

    /**
     * 更新课程
     */
    @PostMapping("/update")
    public Result<CourseVO> updateCourse(@Valid @RequestBody CourseRequest request) {
        log.info("管理端更新课程，参数：{}", request);

        CourseVO result = courseService.updateCourse(request);
        return Result.success(result);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{courseId}")
    public Result<Boolean> deleteCourse(@PathVariable Long courseId) {
        log.info("管理端删除课程，courseId：{}", courseId);

        Boolean result = courseService.deleteCourse(courseId);
        return Result.success(result);
    }
}

package com.lore.master.service.business.impl;

import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.entity.business.BusinessCourse;
import com.lore.master.data.repository.business.BusinessCourseRepository;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.service.business.BusinessCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 课程业务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessCourseServiceImpl implements BusinessCourseService {

    private final BusinessCourseRepository courseRepository;

    @Override
    public CoursePageVO getCourses(CourseQueryDTO queryDTO) {
        log.info("查询课程列表，条件：{}", queryDTO);

        // 构建分页参数
        Pageable pageable = buildPageable(queryDTO);

        Page<BusinessCourse> coursePage;

        // 如果有父课程ID，查询子课程
        if (queryDTO.getParentCourseId() != null) {
            List<BusinessCourse> subCourses = courseRepository
                    .findByParentCourseIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeAsc(queryDTO.getParentCourseId());
            return buildCoursePageVO(subCourses, queryDTO);
        }

        // 如果有搜索关键词，进行全文搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            coursePage = courseRepository.searchCourses(queryDTO.getKeyword(), pageable);
        } else {
            // 复合条件查询
            String status = queryDTO.getPublishedOnly() ? BusinessCourse.STATUS_PUBLISHED : queryDTO.getStatus();
            String knowledgeNodePath = StringUtils.hasText(queryDTO.getKnowledgeNodePath()) 
                    ? "%" + queryDTO.getKnowledgeNodePath() + "%" : null;

            coursePage = courseRepository.findCoursesWithConditions(
                    queryDTO.getCourseType(),
                    status,
                    queryDTO.getDifficultyLevel(),
                    queryDTO.getAuthor(),
                    knowledgeNodePath,
                    pageable
            );
        }

        return buildCoursePageVO(coursePage, queryDTO);
    }

    @Override
    public CourseVO getCourseByCode(String courseCode, String userId) {
        log.info("根据课程编码获取课程详情，courseCode：{}，userId：{}", courseCode, userId);

        Optional<BusinessCourse> courseOpt = courseRepository.findByCourseCodeAndIsDeletedFalse(courseCode);
        if (!courseOpt.isPresent()) {
            throw new RuntimeException("课程不存在：" + courseCode);
        }

        BusinessCourse course = courseOpt.get();
        CourseVO courseVO = convertToVO(course, userId);

        // 如果是合集，获取子课程
        if (BusinessCourse.COURSE_TYPE_COLLECTION.equals(course.getCourseType())) {
            List<CourseVO> subCourses = getSubCourses(course.getId(), userId);
            courseVO.setSubCourses(subCourses);
            courseVO.setSubCourseCount(subCourses.size());
        }

        return courseVO;
    }

    @Override
    public CourseVO getCourseById(Long courseId, String userId) {
        log.info("根据课程ID获取课程详情，courseId：{}，userId：{}", courseId, userId);

        Optional<BusinessCourse> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent() || courseOpt.get().getIsDeleted()) {
            throw new RuntimeException("课程不存在：" + courseId);
        }

        BusinessCourse course = courseOpt.get();
        CourseVO courseVO = convertToVO(course, userId);

        // 如果是合集，获取子课程
        if (BusinessCourse.COURSE_TYPE_COLLECTION.equals(course.getCourseType())) {
            List<CourseVO> subCourses = getSubCourses(course.getId(), userId);
            courseVO.setSubCourses(subCourses);
            courseVO.setSubCourseCount(subCourses.size());
        }

        return courseVO;
    }

    @Override
    public List<CourseVO> getSubCourses(Long parentCourseId, String userId) {
        log.info("获取合集子课程，parentCourseId：{}，userId：{}", parentCourseId, userId);

        List<BusinessCourse> subCourses = courseRepository
                .findByParentCourseIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeAsc(parentCourseId);

        return subCourses.stream()
                .map(course -> convertToVO(course, userId))
                .collect(Collectors.toList());
    }

    @Override
    public CoursePageVO searchCourses(String keyword, Integer page, Integer size, String userId) {
        log.info("搜索课程，keyword：{}，page：{}，size：{}，userId：{}", keyword, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository.searchCourses(keyword, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO getPopularCourses(Integer page, Integer size, String userId) {
        log.info("获取热门课程，page：{}，size：{}，userId：{}", page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository
                .findByStatusAndIsDeletedFalseOrderByViewCountDescPublishTimeDesc(
                        BusinessCourse.STATUS_PUBLISHED, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO getLatestCourses(Integer page, Integer size, String userId) {
        log.info("获取最新课程，page：{}，size：{}，userId：{}", page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository
                .findByStatusAndIsDeletedFalseOrderByPublishTimeDesc(
                        BusinessCourse.STATUS_PUBLISHED, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO getCoursesByKnowledgePath(String knowledgeNodePath, Integer page, Integer size, String userId) {
        log.info("根据知识点路径获取课程，knowledgeNodePath：{}，page：{}，size：{}，userId：{}", 
                knowledgeNodePath, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        String pathPattern = "%" + knowledgeNodePath + "%";
        Page<BusinessCourse> coursePage = courseRepository.findByKnowledgeNodePath(pathPattern, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO getCoursesByDifficulty(String difficultyLevel, Integer page, Integer size, String userId) {
        log.info("根据难度等级获取课程，difficultyLevel：{}，page：{}，size：{}，userId：{}", 
                difficultyLevel, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository.findByDifficultyLevel(difficultyLevel, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO getCoursesByAuthor(String author, Integer page, Integer size, String userId) {
        log.info("根据作者获取课程，author：{}，page：{}，size：{}，userId：{}", author, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository
                .findByAuthorAndIsDeletedFalseOrderByPublishTimeDesc(author, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CoursePageVO.CourseStatisticsVO getCourseStatistics() {
        log.info("获取课程统计信息");

        Long totalCourses = courseRepository.countByStatusAndIsDeletedFalse(BusinessCourse.STATUS_PUBLISHED);
        Long normalCourses = courseRepository.countByCourseTypeAndStatusAndIsDeletedFalse(
                BusinessCourse.COURSE_TYPE_NORMAL, BusinessCourse.STATUS_PUBLISHED);
        Long collections = courseRepository.countByCourseTypeAndStatusAndIsDeletedFalse(
                BusinessCourse.COURSE_TYPE_COLLECTION, BusinessCourse.STATUS_PUBLISHED);

        // 统计各难度等级课程数
        CoursePageVO.CourseStatisticsVO.DifficultyStatistics difficultyStats = 
                CoursePageVO.CourseStatisticsVO.DifficultyStatistics.builder()
                        .l1Count(courseRepository.countByDifficultyLevel(BusinessCourse.DIFFICULTY_L1))
                        .l2Count(courseRepository.countByDifficultyLevel(BusinessCourse.DIFFICULTY_L2))
                        .l3Count(courseRepository.countByDifficultyLevel(BusinessCourse.DIFFICULTY_L3))
                        .l4Count(courseRepository.countByDifficultyLevel(BusinessCourse.DIFFICULTY_L4))
                        .l5Count(courseRepository.countByDifficultyLevel(BusinessCourse.DIFFICULTY_L5))
                        .build();

        return CoursePageVO.CourseStatisticsVO.builder()
                .totalCourses(totalCourses)
                .normalCourses(normalCourses)
                .collections(collections)
                .difficultyStats(difficultyStats)
                .build();
    }

    @Override
    @Transactional
    public void incrementViewCount(Long courseId, String userId) {
        log.info("增加课程观看次数，courseId：{}，userId：{}", courseId, userId);

        Optional<BusinessCourse> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent() && !courseOpt.get().getIsDeleted()) {
            BusinessCourse course = courseOpt.get();
            course.setViewCount(course.getViewCount() + 1);
            courseRepository.save(course);
        }
    }

    @Override
    public CoursePageVO getRecommendedCourses(String userId, Integer page, Integer size) {
        log.info("获取推荐课程，userId：{}，page：{}，size：{}", userId, page, size);

        // TODO: 实现基于用户学习目标和历史的推荐算法
        // 目前返回热门课程作为推荐
        return getPopularCourses(page, size, userId);
    }

    /**
     * 构建分页参数
     */
    private Pageable buildPageable(CourseQueryDTO queryDTO) {
        Sort sort;
        switch (queryDTO.getSortBy()) {
            case "view_count_desc":
                sort = Sort.by(Sort.Direction.DESC, "viewCount", "publishTime");
                break;
            case "created_time_desc":
                sort = Sort.by(Sort.Direction.DESC, "createdTime");
                break;
            case "sort_order_asc":
                sort = Sort.by(Sort.Direction.ASC, "sortOrder", "createdTime");
                break;
            case "publish_time_desc":
            default:
                sort = Sort.by(Sort.Direction.DESC, "publishTime");
                break;
        }

        return PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
    }

    /**
     * 构建课程分页VO（从Page对象）
     */
    private CoursePageVO buildCoursePageVO(Page<BusinessCourse> coursePage, CourseQueryDTO queryDTO) {
        List<CourseVO> courseVOs = coursePage.getContent().stream()
                .map(course -> convertToVO(course, null))
                .collect(Collectors.toList());

        return CoursePageVO.builder()
                .courses(courseVOs)
                .currentPage(coursePage.getNumber())
                .pageSize(coursePage.getSize())
                .totalPages(coursePage.getTotalPages())
                .totalElements(coursePage.getTotalElements())
                .hasNext(coursePage.hasNext())
                .hasPrevious(coursePage.hasPrevious())
                .isFirst(coursePage.isFirst())
                .isLast(coursePage.isLast())
                .statistics(queryDTO != null && queryDTO.getIncludeSubCourseCount() ? getCourseStatistics() : null)
                .build();
    }

    /**
     * 构建课程分页VO（从List对象）
     */
    private CoursePageVO buildCoursePageVO(List<BusinessCourse> courses, CourseQueryDTO queryDTO) {
        List<CourseVO> courseVOs = courses.stream()
                .map(course -> convertToVO(course, null))
                .collect(Collectors.toList());

        return CoursePageVO.builder()
                .courses(courseVOs)
                .currentPage(0)
                .pageSize(courses.size())
                .totalPages(1)
                .totalElements((long) courses.size())
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
    }

    /**
     * 转换实体为VO
     */
    private CourseVO convertToVO(BusinessCourse course, String userId) {
        CourseVO.CourseVOBuilder builder = CourseVO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .author(course.getAuthor())
                .courseType(course.getCourseType())
                .contentType(course.getContentType())
                .difficultyLevel(course.getDifficultyLevel())
                .difficultyLevels(course.getDifficultyLevels())
                .parentCourseId(course.getParentCourseId())
                .sortOrder(course.getSortOrder())
                .status(course.getStatus())
                .knowledgeNodeCode(course.getKnowledgeNodeCode())
                .knowledgeNodePath(course.getKnowledgeNodePath())
                .knowledgeNodeNamePath(course.getKnowledgeNodeNamePath())
                .tags(course.getTags())
                .durationMinutes(course.getDurationMinutes())
                .viewCount(course.getViewCount())
                .likeCount(course.getLikeCount())
                .collectCount(course.getCollectCount())
                .contentUrl(course.getContentUrl())
                .coverImageUrl(course.getCoverImageUrl())
                .thumbnailUrl(course.getThumbnailUrl())
                .publishTime(course.getPublishTime())
                .createdTime(course.getCreatedTime());

        // 解析难度等级列表
        if (StringUtils.hasText(course.getDifficultyLevels())) {
            List<String> difficultyLevelList = Arrays.asList(course.getDifficultyLevels().split(","));
            builder.difficultyLevelList(difficultyLevelList);
        }

        // 解析标签列表
        if (StringUtils.hasText(course.getTags())) {
            List<String> tagList = Arrays.asList(course.getTags().split(","));
            builder.tagList(tagList);
        }

        // 格式化时长
        if (course.getDurationMinutes() != null && course.getDurationMinutes() > 0) {
            builder.formattedDuration(formatDuration(course.getDurationMinutes()));
        }

        // 获取父课程标题
        if (course.getParentCourseId() != null) {
            Optional<BusinessCourse> parentCourse = courseRepository.findById(course.getParentCourseId());
            if (parentCourse.isPresent() && !parentCourse.get().getIsDeleted()) {
                builder.parentCourseTitle(parentCourse.get().getTitle());
            }
        }

        // TODO: 根据userId获取用户相关状态（收藏、点赞、学习进度等）
        if (StringUtils.hasText(userId)) {
            builder.isCollected(false)
                    .isLiked(false)
                    .progressPercent(0);
        }

        return builder.build();
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "0分钟";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours > 0) {
            if (remainingMinutes > 0) {
                return hours + "小时" + remainingMinutes + "分钟";
            } else {
                return hours + "小时";
            }
        } else {
            return remainingMinutes + "分钟";
        }
    }
}

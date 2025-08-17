package com.lore.master.service.business.impl;

import com.lore.master.common.exception.BusinessException;
import com.lore.master.common.result.ResultCode;
import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.dto.business.CourseRequest;
import com.lore.master.data.entity.business.BusinessCourse;
import com.lore.master.data.repository.business.BusinessCourseRepository;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.service.business.BusinessCourseService;
import com.lore.master.service.business.MarkdownProcessingService;
import com.lore.master.service.business.impl.MarkdownProcessingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    private final MarkdownProcessingService markdownProcessingService;

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
                .contentMarkdown(course.getContentMarkdown())
                .contentHtml(course.getContentHtml())
                .contentUpdatedTime(course.getContentUpdatedTime())
                .contentFileIds(course.getContentFileIds())
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

    @Override
    @Transactional("businessTransactionManager")
    public CourseVO createCourse(CourseRequest request) {
        log.info("创建课程，请求参数：{}", request);

        // 参数校验
        validateCourseRequest(request, false);

        // 检查课程编码是否已存在
        if (courseRepository.existsByCourseCodeAndIsDeletedFalse(request.getCourseCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程编码已存在：" + request.getCourseCode());
        }

        // 创建课程实体
        BusinessCourse course = new BusinessCourse();
        copyRequestToEntity(request, course);
        course.setCreatedBy("admin"); // TODO: 从当前登录用户获取
        course.setUpdatedBy("admin");

        // 保存课程
        BusinessCourse savedCourse = courseRepository.save(course);

        log.info("创建课程成功，课程ID：{}，课程编码：{}", savedCourse.getId(), savedCourse.getCourseCode());
        return convertToVO(savedCourse, null);
    }

    @Override
    @Transactional("businessTransactionManager")
    public CourseVO updateCourse(CourseRequest request) {
        log.info("更新课程，请求参数：{}", request);

        // 参数校验
        validateCourseRequest(request, true);

        // 查找现有课程
        Optional<BusinessCourse> optionalCourse = courseRepository.findByIdAndIsDeletedFalse(request.getId());
        if (!optionalCourse.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程不存在，ID：" + request.getId());
        }

        BusinessCourse existingCourse = optionalCourse.get();

        // 检查课程编码是否被其他课程使用
        if (!existingCourse.getCourseCode().equals(request.getCourseCode())) {
            if (courseRepository.existsByCourseCodeAndIsDeletedFalse(request.getCourseCode())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "课程编码已存在：" + request.getCourseCode());
            }
        }

        // 更新课程信息
        copyRequestToEntity(request, existingCourse);
        existingCourse.setUpdatedBy("admin"); // TODO: 从当前登录用户获取

        // 保存更新
        BusinessCourse updatedCourse = courseRepository.save(existingCourse);

        log.info("更新课程成功，课程ID：{}，课程编码：{}", updatedCourse.getId(), updatedCourse.getCourseCode());
        return convertToVO(updatedCourse, null);
    }

    @Override
    @Transactional("businessTransactionManager")
    public Boolean deleteCourse(Long courseId) {
        log.info("删除课程，课程ID：{}", courseId);

        if (courseId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程ID不能为空");
        }

        // 查找课程
        Optional<BusinessCourse> optionalCourse = courseRepository.findByIdAndIsDeletedFalse(courseId);
        if (!optionalCourse.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程不存在，ID：" + courseId);
        }

        BusinessCourse course = optionalCourse.get();

        // 检查是否有子课程
        List<BusinessCourse> subCourses = courseRepository.findByParentCourseIdAndIsDeletedFalse(courseId);
        if (!subCourses.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "存在子课程，无法删除");
        }

        // 软删除
        course.setIsDeleted(true);
        course.setUpdatedBy("admin"); // TODO: 从当前登录用户获取
        courseRepository.save(course);

        log.info("删除课程成功，课程ID：{}", courseId);
        return true;
    }

    /**
     * 校验课程请求参数
     */
    private void validateCourseRequest(CourseRequest request, boolean isUpdate) {
        if (isUpdate && request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "更新课程时ID不能为空");
        }

        if (!StringUtils.hasText(request.getCourseCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程编码不能为空");
        }

        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程标题不能为空");
        }

        if (!StringUtils.hasText(request.getAuthor())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程作者不能为空");
        }

        if (!StringUtils.hasText(request.getCourseType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程类型不能为空");
        }

        if (!StringUtils.hasText(request.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "课程状态不能为空");
        }

        // 普通课程必须有难度等级和内容类型
        if ("NORMAL".equals(request.getCourseType())) {
            if (!StringUtils.hasText(request.getDifficultyLevel())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "普通课程必须指定难度等级");
            }
            if (!StringUtils.hasText(request.getContentType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "普通课程必须指定内容类型");
            }
        }
    }

    /**
     * 将请求数据复制到实体
     */
    private void copyRequestToEntity(CourseRequest request, BusinessCourse entity) {
        entity.setCourseCode(request.getCourseCode());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setAuthor(request.getAuthor());
        entity.setCourseType(request.getCourseType());
        entity.setContentType(request.getContentType());
        entity.setDifficultyLevel(request.getDifficultyLevel());
        entity.setDifficultyLevels(request.getDifficultyLevels());
        entity.setParentCourseId(request.getParentCourseId());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setStatus(request.getStatus());
        entity.setKnowledgeNodeCode(request.getKnowledgeNodeCode());
        entity.setKnowledgeNodePath(request.getKnowledgeNodePath());
        entity.setKnowledgeNodeNamePath(request.getKnowledgeNodeNamePath());
        entity.setTags(request.getTags());
        entity.setDurationMinutes(request.getDurationMinutes());
        entity.setContentUrl(request.getContentUrl());
        entity.setCoverImageUrl(request.getCoverImageUrl());
        entity.setThumbnailUrl(request.getThumbnailUrl());

        // 处理Markdown内容
        if (StringUtils.hasText(request.getContentMarkdown())) {
            entity.setContentMarkdown(request.getContentMarkdown());

            // 将Markdown转换为HTML
            String htmlContent = markdownProcessingService.convertMarkdownToHtml(request.getContentMarkdown());
            entity.setContentHtml(htmlContent);

            // 提取文件引用
            List<String> fileIds = markdownProcessingService.extractFileReferences(request.getContentMarkdown());
            if (!fileIds.isEmpty()) {
                entity.setContentFileIds(((MarkdownProcessingServiceImpl) markdownProcessingService).fileIdsToJson(fileIds));
            }

            entity.setContentUpdatedTime(LocalDateTime.now());
        } else {
            entity.setContentMarkdown(null);
            entity.setContentHtml(null);
            entity.setContentFileIds(null);
            entity.setContentUpdatedTime(null);
        }
    }

    @Override
    @Transactional("businessTransactionManager")
    public void incrementLikeCount(Long courseId, String userId) {
        log.info("增加课程点赞数，courseId：{}，userId：{}", courseId, userId);

        try {
            Optional<BusinessCourse> optionalCourse = courseRepository.findByIdAndIsDeletedFalse(courseId);
            if (optionalCourse.isPresent()) {
                BusinessCourse course = optionalCourse.get();
                course.setLikeCount(course.getLikeCount() + 1);
                courseRepository.save(course);
                log.info("课程点赞数增加成功，courseId：{}，新点赞数：{}", courseId, course.getLikeCount());
            } else {
                log.warn("课程不存在，无法增加点赞数，courseId：{}", courseId);
            }
        } catch (Exception e) {
            log.error("增加课程点赞数失败，courseId：{}，userId：{}", courseId, userId, e);
            throw e;
        }
    }

    @Override
    @Transactional("businessTransactionManager")
    public void incrementCollectCount(Long courseId, String userId) {
        log.info("增加课程收藏数，courseId：{}，userId：{}", courseId, userId);

        try {
            Optional<BusinessCourse> optionalCourse = courseRepository.findByIdAndIsDeletedFalse(courseId);
            if (optionalCourse.isPresent()) {
                BusinessCourse course = optionalCourse.get();
                course.setCollectCount(course.getCollectCount() + 1);
                courseRepository.save(course);
                log.info("课程收藏数增加成功，courseId：{}，新收藏数：{}", courseId, course.getCollectCount());
            } else {
                log.warn("课程不存在，无法增加收藏数，courseId：{}", courseId);
            }
        } catch (Exception e) {
            log.error("增加课程收藏数失败，courseId：{}，userId：{}", courseId, userId, e);
            throw e;
        }
    }
}

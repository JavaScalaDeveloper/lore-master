package com.lore.master.service.business.impl;

import com.lore.master.common.exception.BusinessException;
import com.lore.master.common.result.ResultCode;
import com.lore.master.data.dto.business.CourseQueryDTO;
import com.lore.master.data.dto.business.CourseRequest;
import com.lore.master.data.entity.business.BusinessCourse;
import com.lore.master.data.entity.consumer.ConsumerUserCourseLearningRecord;
import com.lore.master.data.repository.business.BusinessCourseRepository;
import com.lore.master.data.repository.consumer.ConsumerUserCourseLearningRecordRepository;
import com.lore.master.data.vo.business.CourseVO;
import com.lore.master.data.vo.business.CoursePageVO;
import com.lore.master.data.vo.business.CourseListVO;
import com.lore.master.data.vo.business.CourseListPageVO;
import com.lore.master.data.vo.business.RecentLearningCourseVO;
import com.lore.master.service.business.BusinessCourseService;
import com.lore.master.service.business.MarkdownProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final ConsumerUserCourseLearningRecordRepository learningRecordRepository;

    @Override
    public CourseListPageVO getCourses(CourseQueryDTO queryDTO) {
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
    public CourseListPageVO searchCourses(String keyword, Integer page, Integer size, String userId) {
        log.info("搜索课程，keyword：{}，page：{}，size：{}，userId：{}", keyword, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository.searchCourses(keyword, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CourseListPageVO getPopularCourses(Integer page, Integer size, String userId) {
        log.info("获取热门课程，page：{}，size：{}，userId：{}", page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository
                .findByStatusAndIsDeletedFalseOrderByViewCountDescPublishTimeDesc(
                        BusinessCourse.STATUS_PUBLISHED, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CourseListPageVO getLatestCourses(Integer page, Integer size, String userId) {
        log.info("获取最新课程，page：{}，size：{}，userId：{}", page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository
                .findByStatusAndIsDeletedFalseOrderByPublishTimeDesc(
                        BusinessCourse.STATUS_PUBLISHED, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CourseListPageVO getCoursesByKnowledgePath(String knowledgeNodePath, Integer page, Integer size, String userId) {
        log.info("根据知识点路径获取课程，knowledgeNodePath：{}，page：{}，size：{}，userId：{}", 
                knowledgeNodePath, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        String pathPattern = "%" + knowledgeNodePath + "%";
        Page<BusinessCourse> coursePage = courseRepository.findByKnowledgeNodePath(pathPattern, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CourseListPageVO getCoursesByDifficulty(String difficultyLevel, Integer page, Integer size, String userId) {
        log.info("根据难度等级获取课程，difficultyLevel：{}，page：{}，size：{}，userId：{}", 
                difficultyLevel, page, size, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessCourse> coursePage = courseRepository.findByDifficultyLevel(difficultyLevel, pageable);

        return buildCoursePageVO(coursePage, null);
    }

    @Override
    public CourseListPageVO getCoursesByAuthor(String author, Integer page, Integer size, String userId) {
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
    public CourseListPageVO getRecommendedCourses(String userId, Integer page, Integer size) {
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
     * 构建课程分页VO（从Page对象）- 轻量级版本
     */
    private CourseListPageVO buildCoursePageVO(Page<BusinessCourse> coursePage, CourseQueryDTO queryDTO) {
        List<CourseListVO> courseVOs = coursePage.getContent().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return CourseListPageVO.builder()
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
     * 构建课程分页VO（从List对象）- 轻量级版本
     */
    private CourseListPageVO buildCoursePageVO(List<BusinessCourse> courses, CourseQueryDTO queryDTO) {
        List<CourseListVO> courseVOs = courses.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return CourseListPageVO.builder()
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
     * 转换实体为轻量级列表VO（不包含大字段）
     */
    private CourseListVO convertToListVO(BusinessCourse course) {
        CourseListVO.CourseListVOBuilder builder = CourseListVO.builder()
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
                .contentUpdatedTime(course.getContentUpdatedTime())
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

        return builder.build();
    }

    /**
     * 转换实体为完整VO（包含所有字段）
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

        // 如果是合集类型，处理子课程关联
        if ("COLLECTION".equals(request.getCourseType()) && request.getSubCourseIds() != null && !request.getSubCourseIds().isEmpty()) {
            updateSubCourseRelations(savedCourse.getId(), request.getSubCourseIds());
        }

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

        // 如果是合集类型，处理子课程关联
        if ("COLLECTION".equals(request.getCourseType())) {
            if (request.getSubCourseIds() != null) {
                updateSubCourseRelations(request.getId(), request.getSubCourseIds());
            }
        } else {
            // 如果从合集类型改为普通课程，清除原有的子课程关联
            if ("COLLECTION".equals(existingCourse.getCourseType())) {
                updateSubCourseRelations(request.getId(), new ArrayList<>());
            }
        }

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

    /**
     * 更新子课程关联关系
     * @param collectionId 合集ID
     * @param subCourseIds 子课程ID列表
     */
    private void updateSubCourseRelations(Long collectionId, List<Long> subCourseIds) {
        log.info("更新子课程关联关系，合集ID：{}，子课程IDs：{}", collectionId, subCourseIds);

        // 1. 获取当前合集的所有子课程
        List<BusinessCourse> currentSubCourses = courseRepository.findByParentCourseIdAndIsDeletedFalse(collectionId);
        Set<Long> currentSubCourseIds = currentSubCourses.stream()
                .map(BusinessCourse::getId)
                .collect(Collectors.toSet());

        // 2. 新的子课程ID集合
        Set<Long> newSubCourseIds = subCourseIds != null ? new HashSet<>(subCourseIds) : new HashSet<>();

        // 3. 找出需要删除的关联（在旧列表中但不在新列表中）
        Set<Long> toRemove = new HashSet<>(currentSubCourseIds);
        toRemove.removeAll(newSubCourseIds);

        // 4. 找出需要添加的关联（在新列表中但不在旧列表中）
        Set<Long> toAdd = new HashSet<>(newSubCourseIds);
        toAdd.removeAll(currentSubCourseIds);

        // 5. 删除旧的关联（将parent_course_id设为null）
        if (!toRemove.isEmpty()) {
            List<BusinessCourse> coursesToUpdate = courseRepository.findAllById(toRemove);
            for (BusinessCourse course : coursesToUpdate) {
                if (!course.getIsDeleted()) {
                    course.setParentCourseId(null);
                    course.setUpdatedBy("admin");
                }
            }
            courseRepository.saveAll(coursesToUpdate);
            log.info("已移除子课程关联：{}", toRemove);
        }

        // 6. 添加新的关联（设置parent_course_id）
        if (!toAdd.isEmpty()) {
            List<BusinessCourse> coursesToUpdate = courseRepository.findAllById(toAdd);
            for (BusinessCourse course : coursesToUpdate) {
                if (!course.getIsDeleted()) {
                    // 检查课程是否已经属于其他合集
                    if (course.getParentCourseId() != null && !course.getParentCourseId().equals(collectionId)) {
                        log.warn("课程 ID:{} 已属于其他合集 ID:{}，跳过添加", course.getId(), course.getParentCourseId());
                        continue;
                    }
                    // 检查课程类型是否为普通课程
                    if ("COLLECTION".equals(course.getCourseType())) {
                        log.warn("课程 ID:{} 是合集类型，不能作为子课程，跳过添加", course.getId());
                        continue;
                    }
                    course.setParentCourseId(collectionId);
                    course.setUpdatedBy("admin");
                }
            }
            courseRepository.saveAll(coursesToUpdate);
            log.info("已添加子课程关联：{}", toAdd);
        }

        log.info("子课程关联更新完成");
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

    @Override
    @Transactional
    public void saveLearningRecord(String userId, String courseCode) {
        if (userId == null || courseCode == null) {
            log.warn("保存学习记录失败：用户ID或课程编码为空");
            return;
        }

        log.info("保存用户学习记录，userId：{}，courseCode：{}", userId, courseCode);

        try {
            LocalDate today = LocalDate.now();

            // 查找是否已有学习记录（根据用户ID和课程编码）
            Optional<ConsumerUserCourseLearningRecord> existingRecord = learningRecordRepository
                    .findByUserIdAndCourseCode(userId, courseCode);

            ConsumerUserCourseLearningRecord record;
            if (existingRecord.isPresent()) {
                // 更新现有记录
                record = existingRecord.get();
                record.setLearningDuration(record.getLearningDuration() + 60); // 增加1分钟学习时长
                record.setLearningDate(today); // 更新学习日期为今天
                log.info("更新现有学习记录，recordId：{}", record.getId());
            } else {
                // 创建新记录
                record = new ConsumerUserCourseLearningRecord();
                record.setUserId(userId);
                record.setCourseCode(courseCode);
                record.setLearningDuration(60); // 初始1分钟学习时长
                record.setProgressPercent(0);
                record.setIsCompleted(0);
                record.setLearningDate(today);
                log.info("创建新学习记录");
            }

            // 保存记录
            learningRecordRepository.save(record);
            log.info("用户学习记录保存成功，userId：{}，courseCode：{}", userId, courseCode);

        } catch (Exception e) {
            log.error("保存用户学习记录失败，userId：{}，courseCode：{}", userId, courseCode, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public List<RecentLearningCourseVO> getRecentLearningCourses(String userId, int limit) {
        if (userId == null) {
            log.warn("获取用户最近学习课程失败：用户ID为空");
            return List.of();
        }

        log.info("获取用户最近学习课程，userId：{}，limit：{}", userId, limit);

        try {
            // 获取用户最近的学习记录
            List<ConsumerUserCourseLearningRecord> learningRecords = learningRecordRepository
                    .findTop10ByUserIdOrderByCreatedTimeDesc(userId);

            if (learningRecords.isEmpty()) {
                log.info("用户暂无学习记录，userId：{}", userId);
                return List.of();
            }

            // 限制返回数量
            List<ConsumerUserCourseLearningRecord> limitedRecords = learningRecords.stream()
                    .limit(limit)
                    .toList();

            // 根据课程编码获取课程信息并组装返回数据
            List<RecentLearningCourseVO> result = new ArrayList<>();
            for (ConsumerUserCourseLearningRecord record : limitedRecords) {
                try {
                    // 根据课程编码查找课程
                    Optional<BusinessCourse> courseOptional = courseRepository
                            .findByCourseCodeAndIsDeletedFalse(record.getCourseCode());

                    if (courseOptional.isPresent()) {
                        BusinessCourse course = courseOptional.get();

                        RecentLearningCourseVO courseVO = RecentLearningCourseVO.builder()
                                .id(course.getId())
                                .courseCode(course.getCourseCode())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .author(course.getAuthor())
                                .courseType(course.getCourseType())
                                .coverImageUrl(course.getCoverImageUrl())
                                .difficultyLevel(course.getDifficultyLevel())
                                .estimatedMinutes(course.getDurationMinutes())
                                .viewCount(course.getViewCount())
                                .likeCount(course.getLikeCount())
                                .collectCount(course.getCollectCount())
                                .status(course.getStatus())
                                .learningDuration(record.getLearningDuration())
                                .progressPercent(record.getProgressPercent())
                                .isCompleted(record.getIsCompleted())
                                .lastLearningDate(record.getLearningDate())
                                .learningRecordCreatedTime(record.getCreatedTime())
                                .learningRecordUpdatedTime(record.getUpdatedTime())
                                .build();

                        result.add(courseVO);
                    } else {
                        log.warn("课程不存在，courseCode：{}", record.getCourseCode());
                    }
                } catch (Exception e) {
                    log.error("处理学习记录失败，courseCode：{}", record.getCourseCode(), e);
                }
            }

            log.info("获取用户最近学习课程成功，userId：{}，返回数量：{}", userId, result.size());
            return result;

        } catch (Exception e) {
            log.error("获取用户最近学习课程失败，userId：{}", userId, e);
            return List.of();
        }
    }
}

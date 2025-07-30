package com.lore.master.service.admin;

import com.lore.master.data.dto.admin.LearningSkillCatalogDTO;
import com.lore.master.data.dto.admin.LearningSkillCatalogQueryDTO;
import com.lore.master.data.vo.admin.LearningSkillCatalogVO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 学习技能目录服务接口
 */
public interface LearningSkillCatalogService {

    /**
     * 创建技能目录
     */
    LearningSkillCatalogVO createSkillCatalog(LearningSkillCatalogDTO dto);

    /**
     * 更新技能目录
     */
    LearningSkillCatalogVO updateSkillCatalog(Long id, LearningSkillCatalogDTO dto);

    /**
     * 删除技能目录
     */
    void deleteSkillCatalog(Long id);

    /**
     * 批量删除技能目录
     */
    void batchDeleteSkillCatalog(List<Long> ids);

    /**
     * 根据ID查询技能目录
     */
    LearningSkillCatalogVO getSkillCatalogById(Long id);

    /**
     * 根据技能编码查询技能目录
     */
    LearningSkillCatalogVO getSkillCatalogByCode(String skillCode);

    /**
     * 分页查询技能目录
     */
    Page<LearningSkillCatalogVO> querySkillCatalogPage(LearningSkillCatalogQueryDTO queryDTO);

    /**
     * 查询技能目录列表
     */
    List<LearningSkillCatalogVO> querySkillCatalogList(LearningSkillCatalogQueryDTO queryDTO);

    /**
     * 查询技能目录树形结构
     */
    List<LearningSkillCatalogVO> querySkillCatalogTree(LearningSkillCatalogQueryDTO queryDTO);

    /**
     * 查询所有一级分类
     */
    List<LearningSkillCatalogVO> queryFirstLevelCategories();

    /**
     * 查询指定父级下的子分类
     */
    List<LearningSkillCatalogVO> queryChildrenByParentCode(String parentCode);

    /**
     * 查询指定层级的所有技能
     */
    List<LearningSkillCatalogVO> querySkillsByLevel(Integer level);

    /**
     * 查询指定路径前缀下的所有技能
     */
    List<LearningSkillCatalogVO> querySkillsByPathPrefix(String pathPrefix);

    /**
     * 启用/禁用技能目录
     */
    void toggleSkillCatalogStatus(Long id, Boolean isActive);

    /**
     * 批量启用/禁用技能目录
     */
    void batchToggleSkillCatalogStatus(List<Long> ids, Boolean isActive);

    /**
     * 调整技能目录排序
     */
    void adjustSortOrder(Long id, Integer newSortOrder);

    /**
     * 移动技能目录到新的父级
     */
    void moveSkillCatalog(Long id, String newParentCode);

    /**
     * 复制技能目录
     */
    LearningSkillCatalogVO copySkillCatalog(Long id, String newSkillCode, String newSkillName);

    /**
     * 检查技能编码是否存在
     */
    boolean existsBySkillCode(String skillCode);

    /**
     * 检查技能路径是否存在
     */
    boolean existsBySkillPath(String skillPath);

    /**
     * 验证技能目录数据
     */
    void validateSkillCatalog(LearningSkillCatalogDTO dto);

    /**
     * 生成技能路径
     */
    String generateSkillPath(String parentCode, String skillCode);

    /**
     * 获取下一个排序序号
     */
    Integer getNextSortOrder(String parentCode, Integer level);

    /**
     * 导入技能目录数据
     */
    void importSkillCatalogData(List<LearningSkillCatalogDTO> dataList);

    /**
     * 导出技能目录数据
     */
    List<LearningSkillCatalogVO> exportSkillCatalogData(LearningSkillCatalogQueryDTO queryDTO);

    /**
     * 获取技能目录统计信息
     */
    Object getSkillCatalogStatistics();
}

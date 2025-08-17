package com.lore.master.service.admin.impl;

import com.lore.master.common.exception.BusinessException;
import com.lore.master.common.result.ResultCode;
import com.lore.master.data.dto.admin.AdminKnowledgeMapQueryRequest;
import com.lore.master.data.dto.admin.AdminKnowledgeMapRequest;
import com.lore.master.data.entity.admin.AdminKnowledgeMap;
import com.lore.master.data.repository.admin.AdminKnowledgeMapRepository;
import com.lore.master.data.vo.admin.AdminKnowledgeMapResponse;
import com.lore.master.data.vo.admin.AdminKnowledgeMapTreeResponse;
import com.lore.master.service.admin.AdminKnowledgeMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端知识图谱服务实现类
 */
@Slf4j
@Service
public class AdminKnowledgeMapServiceImpl implements AdminKnowledgeMapService {

    @Autowired
    private AdminKnowledgeMapRepository adminKnowledgeMapRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 实体转换为响应DTO
     */
    private AdminKnowledgeMapResponse convertToResponse(AdminKnowledgeMap entity) {
        AdminKnowledgeMapResponse response = new AdminKnowledgeMapResponse();
        BeanUtils.copyProperties(entity, response);

        // 将nodeName的值赋给nodeNameStr字段
        response.setNodeNameStr(entity.getNodeName());

        if (entity.getCreatedTime() != null) {
            response.setCreatedTime(entity.getCreatedTime().format(FORMATTER));
        }
        if (entity.getUpdatedTime() != null) {
            response.setUpdatedTime(entity.getUpdatedTime().format(FORMATTER));
        }

        return response;
    }

    /**
     * 实体列表转换为响应DTO列表
     */
    private List<AdminKnowledgeMapResponse> convertToResponseList(List<AdminKnowledgeMap> entities) {
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("adminTransactionManager")
    public Long addNode(AdminKnowledgeMapRequest request) {
        log.info("添加知识图谱节点，请求参数：{}", request);

        // 参数校验
        validateAddNodeRequest(request);

        // 如果前端传递了nodeNameStr，将其赋值给nodeName
        if (StringUtils.hasText(request.getNodeNameStr())) {
            request.setNodeName(request.getNodeNameStr());
        }

        // 检查节点编码是否已存在
        if (adminKnowledgeMapRepository.existsByNodeCode(request.getNodeCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码已存在：" + request.getNodeCode());
        }

        // 构建节点路径
        String nodePath = buildNodePath(request.getParentCode(), request.getNodeCode());

        // 创建实体对象
        AdminKnowledgeMap entity = new AdminKnowledgeMap();
        BeanUtils.copyProperties(request, entity);
        entity.setNodePath(nodePath);
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE");
        entity.setCreatedBy("system");

        // 如果没有设置排序序号，自动设置
        if (entity.getSortOrder() == null) {
            Integer maxSort = StringUtils.hasText(request.getParentCode())
                ? adminKnowledgeMapRepository.findMaxSortOrderByParentCode(request.getParentCode())
                : adminKnowledgeMapRepository.findMaxSortOrderForRootNodes();
            entity.setSortOrder((maxSort != null ? maxSort : 0) + 1);
        }

        // 保存实体
        AdminKnowledgeMap savedEntity = adminKnowledgeMapRepository.save(entity);

        log.info("添加知识图谱节点成功，节点ID：{}", savedEntity.getId());
        return savedEntity.getId();
    }

    @Override
    @Transactional("adminTransactionManager")
    public Boolean deleteNode(String nodeCode) {
        log.info("删除知识图谱节点，节点编码：{}", nodeCode);

        if (!StringUtils.hasText(nodeCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码不能为空");
        }

        // 检查是否存在子节点
        long childCount = adminKnowledgeMapRepository.countByParentCodeAndStatus(nodeCode, "ACTIVE");
        if (childCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "存在子节点，无法删除");
        }

        // 软删除节点
        int rows = adminKnowledgeMapRepository.softDeleteByNodeCode(nodeCode, "system");

        log.info("删除知识图谱节点成功，节点编码：{}，影响行数：{}", nodeCode, rows);
        return rows > 0;
    }

    @Override
    @Transactional("adminTransactionManager")
    public Boolean updateNode(AdminKnowledgeMapRequest request) {
        log.info("更新知识图谱节点，请求参数：{}", request);

        if (!StringUtils.hasText(request.getNodeCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码不能为空");
        }

        // 如果前端传递了nodeNameStr，将其赋值给nodeName
        if (StringUtils.hasText(request.getNodeNameStr())) {
            request.setNodeName(request.getNodeNameStr());
        }

        // 查找现有节点
        Optional<AdminKnowledgeMap> optionalEntity = adminKnowledgeMapRepository.findByNodeCodeAndStatus(request.getNodeCode(), "ACTIVE");
        if (!optionalEntity.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点不存在：" + request.getNodeCode());
        }

        AdminKnowledgeMap entity = optionalEntity.get();

        // 更新字段
        if (StringUtils.hasText(request.getNodeName())) {
            entity.setNodeName(request.getNodeName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            entity.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getDifficultyLevel())) {
            entity.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getEstimatedHours() != null) {
            entity.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            entity.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getSkillCatalogCode())) {
            entity.setSkillCatalogCode(request.getSkillCatalogCode());
        }
        if (StringUtils.hasText(request.getLevelType())) {
            entity.setLevelType(request.getLevelType());
        }

        entity.setUpdatedBy("system");

        // 保存更新
        adminKnowledgeMapRepository.save(entity);

        log.info("更新知识图谱节点成功，节点编码：{}", request.getNodeCode());
        return true;
    }

    @Override
    public AdminKnowledgeMapResponse getNodeDetail(String nodeCode) {
        log.info("查询知识图谱节点详情，节点编码：{}", nodeCode);

        if (!StringUtils.hasText(nodeCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码不能为空");
        }

        Optional<AdminKnowledgeMap> optionalEntity = adminKnowledgeMapRepository.findByNodeCodeAndStatus(nodeCode, "ACTIVE");
        if (!optionalEntity.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点不存在：" + nodeCode);
        }

        AdminKnowledgeMapResponse response = convertToResponse(optionalEntity.get());
        log.info("查询知识图谱节点详情成功，节点编码：{}", nodeCode);
        return response;
    }

    @Override
    public List<AdminKnowledgeMapResponse> getNodeList(AdminKnowledgeMapQueryRequest request) {
        log.info("分页查询知识图谱节点列表，请求参数：{}", request);

        // 构建分页对象
        Pageable pageable = PageRequest.of(
            request.getPageNum() - 1,
            request.getPageSize() != null ? request.getPageSize() : 20
        );

        // 调用Repository的分页查询方法
        Page<AdminKnowledgeMap> page = adminKnowledgeMapRepository.findByConditions(
            request.getRootCode(),
            request.getNodeType(),
            request.getLevelDepth(),
            request.getLevelType(),
            request.getDifficultyLevel(),
            StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE",
            request.getKeyword(),
            pageable
        );

        List<AdminKnowledgeMapResponse> results = convertToResponseList(page.getContent());

        log.info("分页查询知识图谱节点列表成功，返回{}条记录", results.size());
        return results;
    }

    @Override
    public List<AdminKnowledgeMapResponse> getChildNodes(String parentCode) {
        log.info("查询子节点列表，父节点编码：{}", parentCode);

        if (!StringUtils.hasText(parentCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "父节点编码不能为空");
        }

        List<AdminKnowledgeMap> entities = adminKnowledgeMapRepository.findByParentCodeAndStatusOrderBySortOrder(parentCode, "ACTIVE");
        List<AdminKnowledgeMapResponse> results = convertToResponseList(entities);

        log.info("查询子节点列表成功，父节点编码：{}，返回{}条记录", parentCode, results.size());
        return results;
    }

    // 私有方法：参数校验
    private void validateAddNodeRequest(AdminKnowledgeMapRequest request) {
        if (!StringUtils.hasText(request.getNodeCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码不能为空");
        }
        // 节点名称校验：优先检查nodeNameStr，如果没有则检查nodeName
        if (!StringUtils.hasText(request.getNodeNameStr()) && !StringUtils.hasText(request.getNodeName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点名称不能为空");
        }
        if (!StringUtils.hasText(request.getNodeType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点类型不能为空");
        }
        if (!StringUtils.hasText(request.getRootCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "根节点编码不能为空");
        }
        if (request.getLevelDepth() == null || request.getLevelDepth() < 1 || request.getLevelDepth() > 10) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "层级深度必须在1-10之间");
        }
    }



    @Override
    public AdminKnowledgeMapTreeResponse getSkillTree(String rootCode) {
        log.info("查询技能树结构，根节点编码：{}", rootCode);

        if (!StringUtils.hasText(rootCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "根节点编码不能为空");
        }

        // 查询所有节点
        List<AdminKnowledgeMap> allEntities = adminKnowledgeMapRepository.findByRootCodeAndStatusOrderByLevelDepthAscSortOrderAsc(rootCode, "ACTIVE");

        if (allEntities.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "技能树不存在：" + rootCode);
        }

        List<AdminKnowledgeMapResponse> allNodes = convertToResponseList(allEntities);

        // 构建树形结构
        AdminKnowledgeMapTreeResponse treeResponse = new AdminKnowledgeMapTreeResponse();

        // 找到根节点
        AdminKnowledgeMapResponse rootNode = allNodes.stream()
            .filter(node -> "ROOT".equals(node.getNodeType()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ResultCode.PARAM_ERROR, "根节点不存在"));

        treeResponse.setRootCode(rootNode.getNodeCode());
        treeResponse.setRootName(rootNode.getNodeName());

        // 构建子节点树
        List<AdminKnowledgeMapTreeResponse.TreeNode> children = buildTreeNodes(allNodes, rootCode);
        treeResponse.setChildren(children);

        log.info("查询技能树结构成功，根节点编码：{}，总节点数：{}", rootCode, allNodes.size());
        return treeResponse;
    }

    @Override
    public List<AdminKnowledgeMapResponse> getNodesByLevel(String rootCode, Integer levelDepth) {
        log.info("查询指定层级节点列表，根节点编码：{}，层级深度：{}", rootCode, levelDepth);

        if (!StringUtils.hasText(rootCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "根节点编码不能为空");
        }
        if (levelDepth == null || levelDepth < 1 || levelDepth > 10) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "层级深度必须在1-10之间");
        }

        List<AdminKnowledgeMap> entities = adminKnowledgeMapRepository.findByRootCodeAndLevelDepthAndStatusOrderBySortOrder(rootCode, levelDepth, "ACTIVE");
        List<AdminKnowledgeMapResponse> results = convertToResponseList(entities);

        log.info("查询指定层级节点列表成功，返回{}条记录", results.size());
        return results;
    }

    @Override
    public List<AdminKnowledgeMapResponse> getRootNodes() {
        log.info("查询所有根节点列表");

        List<AdminKnowledgeMap> entities = adminKnowledgeMapRepository.findByNodeTypeAndStatusOrderBySortOrder("ROOT", "ACTIVE");
        List<AdminKnowledgeMapResponse> results = convertToResponseList(entities);

        log.info("查询所有根节点列表成功，返回{}条记录", results.size());
        return results;
    }

    @Override
    @Transactional("adminTransactionManager")
    public Boolean updateNodeSort(List<Map<String, Object>> sortList) {
        log.info("批量更新节点排序，更新{}个节点", sortList.size());

        if (sortList == null || sortList.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "排序列表不能为空");
        }

        int totalUpdated = 0;
        for (Map<String, Object> item : sortList) {
            String nodeCode = (String) item.get("nodeCode");
            Integer sortOrder = (Integer) item.get("sortOrder");

            if (!StringUtils.hasText(nodeCode) || sortOrder == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码和排序序号不能为空");
            }

            int updated = adminKnowledgeMapRepository.updateSortOrder(nodeCode, sortOrder, "system");
            totalUpdated += updated;
        }

        log.info("批量更新节点排序成功，影响行数：{}", totalUpdated);
        return true;
    }

    @Override
    @Transactional("adminTransactionManager")
    public Boolean moveNode(String nodeCode, String newParentCode) {
        log.info("移动节点，节点编码：{}，新父节点编码：{}", nodeCode, newParentCode);

        if (!StringUtils.hasText(nodeCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点编码不能为空");
        }

        // 验证节点是否存在
        Optional<AdminKnowledgeMap> optionalEntity = adminKnowledgeMapRepository.findByNodeCodeAndStatus(nodeCode, "ACTIVE");
        if (!optionalEntity.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "节点不存在：" + nodeCode);
        }

        AdminKnowledgeMap entity = optionalEntity.get();

        // 构建新的节点路径
        String newNodePath = buildNodePath(newParentCode, nodeCode);

        // 计算新的层级深度
        Integer newLevelDepth = StringUtils.hasText(newParentCode) ?
            newNodePath.split("/").length : 1;

        // 更新节点信息
        entity.setParentCode(newParentCode);
        entity.setNodePath(newNodePath);
        entity.setLevelDepth(newLevelDepth);
        entity.setUpdatedBy("system");

        adminKnowledgeMapRepository.save(entity);

        // 递归更新所有子节点的路径和层级
        updateChildrenPaths(nodeCode, newNodePath, newLevelDepth);

        log.info("移动节点成功，节点编码：{}", nodeCode);
        return true;
    }

    // 私有方法：构建树形节点
    private List<AdminKnowledgeMapTreeResponse.TreeNode> buildTreeNodes(
            List<AdminKnowledgeMapResponse> allNodes, String parentCode) {

        return allNodes.stream()
            .filter(node -> Objects.equals(node.getParentCode(), parentCode))
            .sorted(Comparator.comparing(AdminKnowledgeMapResponse::getSortOrder))
            .map(node -> {
                AdminKnowledgeMapTreeResponse.TreeNode treeNode = new AdminKnowledgeMapTreeResponse.TreeNode();
                treeNode.setNodeCode(node.getNodeCode());
                treeNode.setNodeName(node.getNodeName());
                treeNode.setNodeNameStr(node.getNodeNameStr()); // 设置nodeNameStr字段
                treeNode.setNodeType(node.getNodeType());
                treeNode.setLevelDepth(node.getLevelDepth());
                treeNode.setLevelType(node.getLevelType());
                treeNode.setSortOrder(node.getSortOrder());
                treeNode.setDifficultyLevel(node.getDifficultyLevel());
                treeNode.setEstimatedHours(node.getEstimatedHours());
                treeNode.setDescription(node.getDescription());

                // 递归构建子节点
                List<AdminKnowledgeMapTreeResponse.TreeNode> children = buildTreeNodes(allNodes, node.getNodeCode());
                treeNode.setChildren(children);

                return treeNode;
            })
            .collect(Collectors.toList());
    }

    // 私有方法：递归更新子节点路径
    private void updateChildrenPaths(String parentCode, String parentPath, Integer parentLevel) {
        // 查询所有子节点
        List<AdminKnowledgeMap> childEntities = adminKnowledgeMapRepository.findByParentCodeAndStatusOrderBySortOrder(parentCode, "ACTIVE");

        for (AdminKnowledgeMap childEntity : childEntities) {
            String newChildPath = parentPath + "/" + childEntity.getNodeCode();
            Integer newChildLevel = parentLevel + 1;

            // 更新子节点路径和层级
            adminKnowledgeMapRepository.updateNodePathAndLevel(childEntity.getNodeCode(), newChildPath, newChildLevel, "system");

            // 递归更新子节点的子节点
            updateChildrenPaths(childEntity.getNodeCode(), newChildPath, newChildLevel);
        }
    }

    // 私有方法：构建节点路径
    private String buildNodePath(String parentCode, String nodeCode) {
        if (!StringUtils.hasText(parentCode)) {
            // 根节点
            return nodeCode;
        }

        // 查询父节点路径
        Optional<AdminKnowledgeMap> parentEntity = adminKnowledgeMapRepository.findByNodeCodeAndStatus(parentCode, "ACTIVE");
        if (!parentEntity.isPresent()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "父节点不存在：" + parentCode);
        }

        String parentPath = parentEntity.get().getNodePath();
        return parentPath + "/" + nodeCode;
    }

}

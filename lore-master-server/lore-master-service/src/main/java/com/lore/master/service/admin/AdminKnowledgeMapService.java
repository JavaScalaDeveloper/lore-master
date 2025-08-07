package com.lore.master.service.admin;

import com.lore.master.data.dto.admin.AdminKnowledgeMapQueryRequest;
import com.lore.master.data.dto.admin.AdminKnowledgeMapRequest;
import com.lore.master.data.vo.admin.AdminKnowledgeMapResponse;
import com.lore.master.data.vo.admin.AdminKnowledgeMapTreeResponse;

import java.util.List;
import java.util.Map;

/**
 * 管理端知识图谱服务接口
 * 提供技能路线知识图谱的业务逻辑处理
 */
public interface AdminKnowledgeMapService {

    /**
     * 添加知识图谱节点
     * @param request 节点信息
     * @return 节点ID
     */
    Long addNode(AdminKnowledgeMapRequest request);

    /**
     * 删除知识图谱节点
     * @param nodeCode 节点编码
     * @return 删除结果
     */
    Boolean deleteNode(String nodeCode);

    /**
     * 更新知识图谱节点
     * @param request 节点信息
     * @return 更新结果
     */
    Boolean updateNode(AdminKnowledgeMapRequest request);

    /**
     * 查询单个节点详情
     * @param nodeCode 节点编码
     * @return 节点详情
     */
    AdminKnowledgeMapResponse getNodeDetail(String nodeCode);

    /**
     * 分页查询节点列表
     * @param request 查询条件
     * @return 节点列表
     */
    List<AdminKnowledgeMapResponse> getNodeList(AdminKnowledgeMapQueryRequest request);

    /**
     * 查询指定节点的子节点列表
     * @param parentCode 父节点编码
     * @return 子节点列表
     */
    List<AdminKnowledgeMapResponse> getChildNodes(String parentCode);

    /**
     * 查询完整的技能树结构
     * @param rootCode 根节点编码
     * @return 技能树结构
     */
    AdminKnowledgeMapTreeResponse getSkillTree(String rootCode);

    /**
     * 查询指定层级的节点列表
     * @param rootCode 根节点编码
     * @param levelDepth 层级深度
     * @return 节点列表
     */
    List<AdminKnowledgeMapResponse> getNodesByLevel(String rootCode, Integer levelDepth);

    /**
     * 查询所有根节点列表
     * @return 根节点列表
     */
    List<AdminKnowledgeMapResponse> getRootNodes();

    /**
     * 批量更新节点排序
     * @param sortList 排序列表，包含nodeCode和sortOrder
     * @return 更新结果
     */
    Boolean updateNodeSort(List<Map<String, Object>> sortList);

    /**
     * 移动节点到新的父节点下
     * @param nodeCode 节点编码
     * @param newParentCode 新父节点编码
     * @return 移动结果
     */
    Boolean moveNode(String nodeCode, String newParentCode);

}

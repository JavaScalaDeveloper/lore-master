package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.admin.AdminKnowledgeMapQueryRequest;
import com.lore.master.data.dto.admin.AdminKnowledgeMapRequest;
import com.lore.master.data.vo.admin.AdminKnowledgeMapResponse;
import com.lore.master.data.vo.admin.AdminKnowledgeMapTreeResponse;
import com.lore.master.service.admin.AdminKnowledgeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端知识图谱控制器
 * 提供技能路线知识图谱的增删改查和树形结构查询功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/knowledge-map")
@RequiredArgsConstructor
public class AdminKnowledgeMapController {

    private final AdminKnowledgeMapService adminKnowledgeMapService;

    /**
     * 添加知识图谱节点
     */
    @PostMapping("/addNode")
    public Result<Long> addNode(@RequestBody AdminKnowledgeMapRequest request) {
        try {
            log.info("添加知识图谱节点，请求参数：{}", request);
            
            Long nodeId = adminKnowledgeMapService.addNode(request);
            
            log.info("添加知识图谱节点成功，节点ID：{}", nodeId);
            return Result.success(nodeId);
        } catch (Exception e) {
            log.error("添加知识图谱节点失败", e);
            return Result.error("添加节点失败：" + e.getMessage());
        }
    }

    /**
     * 删除知识图谱节点
     */
    @PostMapping("/deleteNode")
    public Result<Boolean> deleteNode(@RequestParam String nodeCode) {
        try {
            log.info("删除知识图谱节点，节点编码：{}", nodeCode);
            
            Boolean result = adminKnowledgeMapService.deleteNode(nodeCode);
            
            log.info("删除知识图谱节点成功，节点编码：{}", nodeCode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除知识图谱节点失败，节点编码：{}", nodeCode, e);
            return Result.error("删除节点失败：" + e.getMessage());
        }
    }

    /**
     * 更新知识图谱节点
     */
    @PostMapping("/updateNode")
    public Result<Boolean> updateNode(@RequestBody AdminKnowledgeMapRequest request) {
        try {
            log.info("更新知识图谱节点，请求参数：{}", request);
            
            Boolean result = adminKnowledgeMapService.updateNode(request);
            
            log.info("更新知识图谱节点成功，节点编码：{}", request.getNodeCode());
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新知识图谱节点失败", e);
            return Result.error("更新节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询单个知识图谱节点详情
     */
    @PostMapping("/getNodeDetail")
    public Result<AdminKnowledgeMapResponse> getNodeDetail(@RequestParam String nodeCode) {
        try {
            log.info("查询知识图谱节点详情，节点编码：{}", nodeCode);
            
            AdminKnowledgeMapResponse response = adminKnowledgeMapService.getNodeDetail(nodeCode);
            
            log.info("查询知识图谱节点详情成功，节点编码：{}", nodeCode);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询知识图谱节点详情失败，节点编码：{}", nodeCode, e);
            return Result.error("查询节点详情失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询知识图谱节点列表
     */
    @PostMapping("/getNodeList")
    public Result<List<AdminKnowledgeMapResponse>> getNodeList(@RequestBody AdminKnowledgeMapQueryRequest request) {
        try {
            log.info("分页查询知识图谱节点列表，请求参数：{}", request);
            
            List<AdminKnowledgeMapResponse> list = adminKnowledgeMapService.getNodeList(request);

            log.info("分页查询知识图谱节点列表成功，返回{}条记录", list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("分页查询知识图谱节点列表失败", e);
            return Result.error("查询节点列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定节点的子节点列表
     */
    @PostMapping("/getChildNodes")
    public Result<List<AdminKnowledgeMapResponse>> getChildNodes(@RequestParam String parentCode) {
        try {
            log.info("查询子节点列表，父节点编码：{}", parentCode);

            List<AdminKnowledgeMapResponse> list = adminKnowledgeMapService.getChildNodes(parentCode);

            log.info("查询子节点列表成功，父节点编码：{}，返回{}条记录", parentCode, list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询子节点列表失败，父节点编码：{}", parentCode, e);
            return Result.error("查询子节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询完整的技能树结构（思维导图）
     */
    @PostMapping("/getSkillTree")
    public Result<AdminKnowledgeMapTreeResponse> getSkillTree(@RequestParam String rootCode) {
        try {
            log.info("查询技能树结构，根节点编码：{}", rootCode);

            AdminKnowledgeMapTreeResponse tree = adminKnowledgeMapService.getSkillTree(rootCode);

            log.info("查询技能树结构成功，根节点编码：{}", rootCode);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("查询技能树结构失败，根节点编码：{}", rootCode, e);
            return Result.error("查询技能树失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定层级的节点列表
     */
    @PostMapping("/getNodesByLevel")
    public Result<List<AdminKnowledgeMapResponse>> getNodesByLevel(
            @RequestParam String rootCode,
            @RequestParam Integer levelDepth) {
        try {
            log.info("查询指定层级节点列表，根节点编码：{}，层级深度：{}", rootCode, levelDepth);

            List<AdminKnowledgeMapResponse> list = adminKnowledgeMapService.getNodesByLevel(rootCode, levelDepth);

            log.info("查询指定层级节点列表成功，返回{}条记录", list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询指定层级节点列表失败，根节点编码：{}，层级深度：{}", rootCode, levelDepth, e);
            return Result.error("查询指定层级节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有根节点列表
     */
    @PostMapping("/getRootNodes")
    public Result<List<AdminKnowledgeMapResponse>> getRootNodes() {
        try {
            log.info("查询所有根节点列表");

            List<AdminKnowledgeMapResponse> list = adminKnowledgeMapService.getRootNodes();

            log.info("查询所有根节点列表成功，返回{}条记录", list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询所有根节点列表失败", e);
            return Result.error("查询根节点列表失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新节点排序
     */
    @PostMapping("/updateNodeSort")
    public Result<Boolean> updateNodeSort(@RequestBody List<Map<String, Object>> sortList) {
        try {
            log.info("批量更新节点排序，更新{}个节点", sortList.size());

            Boolean result = adminKnowledgeMapService.updateNodeSort(sortList);

            log.info("批量更新节点排序成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量更新节点排序失败", e);
            return Result.error("更新节点排序失败：" + e.getMessage());
        }
    }

    /**
     * 移动节点到新的父节点下
     */
    @PostMapping("/moveNode")
    public Result<Boolean> moveNode(
            @RequestParam String nodeCode,
            @RequestParam String newParentCode) {
        try {
            log.info("移动节点，节点编码：{}，新父节点编码：{}", nodeCode, newParentCode);

            Boolean result = adminKnowledgeMapService.moveNode(nodeCode, newParentCode);

            log.info("移动节点成功，节点编码：{}", nodeCode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("移动节点失败，节点编码：{}", nodeCode, e);
            return Result.error("移动节点失败：" + e.getMessage());
        }
    }

}

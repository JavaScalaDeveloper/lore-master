package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.vo.admin.AdminKnowledgeMapResponse;
import com.lore.master.data.vo.admin.AdminKnowledgeMapTreeResponse;
import com.lore.master.service.admin.AdminKnowledgeMapService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消费者端知识图谱控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/consumer/knowledge-map")
@RequiredArgsConstructor
@Validated
public class KnowledgeMapController {

    private final AdminKnowledgeMapService adminKnowledgeMapService;

    /**
     * 查询完整的技能树结构（思维导图）
     */
    @PostMapping("/getSkillTree")
    public Result<AdminKnowledgeMapTreeResponse> getSkillTree(@RequestParam @NotBlank String rootCode) {
        try {
            log.info("消费者端查询技能树结构，根节点编码：{}", rootCode);

            AdminKnowledgeMapTreeResponse tree = adminKnowledgeMapService.getSkillTree(rootCode);

            log.info("消费者端查询技能树结构成功，根节点编码：{}", rootCode);
            return Result.success("查询成功", tree);
        } catch (Exception e) {
            log.error("消费者端查询技能树结构失败，根节点编码：{}", rootCode, e);
            return Result.error("查询技能树失败：" + e.getMessage());
        }
    }

    /**
     * 查询根节点列表
     */
    @GetMapping("/getRootNodes")
    public Result<List<AdminKnowledgeMapResponse>> getRootNodes() {
        try {
            log.info("消费者端查询根节点列表");

            List<AdminKnowledgeMapResponse> rootNodes = adminKnowledgeMapService.getRootNodes();

            log.info("消费者端查询根节点列表成功，返回{}条记录", rootNodes.size());
            return Result.success("查询成功", rootNodes);
        } catch (Exception e) {
            log.error("消费者端查询根节点列表失败", e);
            return Result.error("查询根节点失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定节点的详细信息
     */
    @GetMapping("/getNodeDetail")
    public Result<AdminKnowledgeMapResponse> getNodeDetail(@RequestParam @NotBlank String nodeCode) {
        try {
            log.info("消费者端查询节点详情，节点编码：{}", nodeCode);

            AdminKnowledgeMapResponse nodeDetail = adminKnowledgeMapService.getNodeDetail(nodeCode);

            log.info("消费者端查询节点详情成功，节点编码：{}", nodeCode);
            return Result.success("查询成功", nodeDetail);
        } catch (Exception e) {
            log.error("消费者端查询节点详情失败，节点编码：{}", nodeCode, e);
            return Result.error("查询节点详情失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定节点的子节点列表
     */
    @GetMapping("/getChildNodes")
    public Result<List<AdminKnowledgeMapResponse>> getChildNodes(@RequestParam @NotBlank String parentCode) {
        try {
            log.info("消费者端查询子节点列表，父节点编码：{}", parentCode);

            List<AdminKnowledgeMapResponse> childNodes = adminKnowledgeMapService.getChildNodes(parentCode);

            log.info("消费者端查询子节点列表成功，父节点编码：{}，返回{}条记录", parentCode, childNodes.size());
            return Result.success("查询成功", childNodes);
        } catch (Exception e) {
            log.error("消费者端查询子节点列表失败，父节点编码：{}", parentCode, e);
            return Result.error("查询子节点失败：" + e.getMessage());
        }
    }
}

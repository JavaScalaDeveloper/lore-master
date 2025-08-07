package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.AdminKnowledgeMap;
import com.lore.master.data.repository.admin.AdminKnowledgeMapRepository;
import com.lore.master.data.dto.admin.AdminKnowledgeMapRequest;
import com.lore.master.data.vo.admin.AdminKnowledgeMapResponse;
import com.lore.master.data.vo.admin.AdminKnowledgeMapTreeResponse;
import com.lore.master.service.admin.impl.AdminKnowledgeMapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 知识图谱服务测试类
 */
@ExtendWith(MockitoExtension.class)
class AdminKnowledgeMapServiceTest {

    @Mock
    private AdminKnowledgeMapRepository adminKnowledgeMapRepository;

    @InjectMocks
    private AdminKnowledgeMapServiceImpl adminKnowledgeMapService;

    private AdminKnowledgeMap rootEntity;
    private AdminKnowledgeMap levelEntity;
    private AdminKnowledgeMap leafEntity;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        rootEntity = new AdminKnowledgeMap();
        rootEntity.setId(1L);
        rootEntity.setNodeCode("java_expert");
        rootEntity.setNodeName("Java专家");
        rootEntity.setNodeType("ROOT");
        rootEntity.setRootCode("java_expert");
        rootEntity.setNodePath("java_expert");
        rootEntity.setLevelDepth(1);
        rootEntity.setSortOrder(1);
        rootEntity.setDifficultyLevel("EXPERT");
        rootEntity.setEstimatedHours(2000);
        rootEntity.setStatus("ACTIVE");
        rootEntity.setCreatedBy("system");
        rootEntity.setCreatedTime(LocalDateTime.now());

        levelEntity = new AdminKnowledgeMap();
        levelEntity.setId(2L);
        levelEntity.setNodeCode("java_l1");
        levelEntity.setNodeName("L1初级");
        levelEntity.setNodeType("LEVEL");
        levelEntity.setParentCode("java_expert");
        levelEntity.setRootCode("java_expert");
        levelEntity.setNodePath("java_expert/java_l1");
        levelEntity.setLevelDepth(2);
        levelEntity.setLevelType("L1");
        levelEntity.setSortOrder(1);
        levelEntity.setDifficultyLevel("BEGINNER");
        levelEntity.setEstimatedHours(200);
        levelEntity.setStatus("ACTIVE");
        levelEntity.setCreatedBy("system");
        levelEntity.setCreatedTime(LocalDateTime.now());

        leafEntity = new AdminKnowledgeMap();
        leafEntity.setId(3L);
        leafEntity.setNodeCode("java_l1_basic");
        leafEntity.setNodeName("Java基础语法");
        leafEntity.setNodeType("LEAF");
        leafEntity.setParentCode("java_l1");
        leafEntity.setRootCode("java_expert");
        leafEntity.setNodePath("java_expert/java_l1/java_l1_basic");
        leafEntity.setLevelDepth(3);
        leafEntity.setSortOrder(1);
        leafEntity.setDifficultyLevel("BEGINNER");
        leafEntity.setEstimatedHours(80);
        leafEntity.setStatus("ACTIVE");
        leafEntity.setCreatedBy("system");
        leafEntity.setCreatedTime(LocalDateTime.now());
    }

    @Test
    void testAddNode() {
        // 准备测试数据
        AdminKnowledgeMapRequest request = new AdminKnowledgeMapRequest();
        request.setNodeCode("test_node");
        request.setNodeName("测试节点");
        request.setNodeType("LEAF");
        request.setRootCode("java_expert");
        request.setLevelDepth(2);
        request.setDifficultyLevel("BEGINNER");
        request.setEstimatedHours(10);

        AdminKnowledgeMap savedEntity = new AdminKnowledgeMap();
        savedEntity.setId(100L);
        savedEntity.setNodeCode(request.getNodeCode());

        // Mock方法调用
        when(adminKnowledgeMapRepository.existsByNodeCode(anyString())).thenReturn(false);
        when(adminKnowledgeMapRepository.findMaxSortOrderForRootNodes()).thenReturn(0);
        when(adminKnowledgeMapRepository.save(any(AdminKnowledgeMap.class))).thenReturn(savedEntity);

        // 执行测试
        Long result = adminKnowledgeMapService.addNode(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result);
        verify(adminKnowledgeMapRepository).save(any(AdminKnowledgeMap.class));
    }

    @Test
    void testGetNodeDetail() {
        // Mock方法调用
        when(adminKnowledgeMapRepository.findByNodeCodeAndStatus("java_expert", "ACTIVE"))
                .thenReturn(Optional.of(rootEntity));

        // 执行测试
        AdminKnowledgeMapResponse result = adminKnowledgeMapService.getNodeDetail("java_expert");

        // 验证结果
        assertNotNull(result);
        assertEquals("java_expert", result.getNodeCode());
        assertEquals("Java专家", result.getNodeName());
        assertEquals("ROOT", result.getNodeType());
    }

    @Test
    void testGetChildNodes() {
        // Mock方法调用
        when(adminKnowledgeMapRepository.findByParentCodeAndStatusOrderBySortOrder("java_expert", "ACTIVE"))
                .thenReturn(Arrays.asList(levelEntity));

        // 执行测试
        List<AdminKnowledgeMapResponse> result = adminKnowledgeMapService.getChildNodes("java_expert");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("java_l1", result.get(0).getNodeCode());
        assertEquals("L1初级", result.get(0).getNodeName());
    }

    @Test
    void testGetSkillTree() {
        // Mock方法调用
        when(adminKnowledgeMapRepository.findByRootCodeAndStatusOrderByLevelDepthAscSortOrderAsc("java_expert", "ACTIVE"))
                .thenReturn(Arrays.asList(rootEntity, levelEntity, leafEntity));

        // 执行测试
        AdminKnowledgeMapTreeResponse result = adminKnowledgeMapService.getSkillTree("java_expert");

        // 验证结果
        assertNotNull(result);
        assertEquals("java_expert", result.getRootCode());
        assertEquals("Java专家", result.getRootName());
        assertNotNull(result.getChildren());
    }

    @Test
    void testDeleteNode() {
        // Mock方法调用
        when(adminKnowledgeMapRepository.countByParentCodeAndStatus("java_expert", "ACTIVE"))
                .thenReturn(0L);
        when(adminKnowledgeMapRepository.softDeleteByNodeCode("java_expert", "system"))
                .thenReturn(1);

        // 执行测试
        Boolean result = adminKnowledgeMapService.deleteNode("java_expert");

        // 验证结果
        assertTrue(result);
        verify(adminKnowledgeMapRepository).softDeleteByNodeCode("java_expert", "system");
    }

    @Test
    void testGetRootNodes() {
        // Mock方法调用
        when(adminKnowledgeMapRepository.findByNodeTypeAndStatusOrderBySortOrder("ROOT", "ACTIVE"))
                .thenReturn(Arrays.asList(rootEntity));

        // 执行测试
        List<AdminKnowledgeMapResponse> result = adminKnowledgeMapService.getRootNodes();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("java_expert", result.get(0).getNodeCode());
        assertEquals("ROOT", result.get(0).getNodeType());
    }
}

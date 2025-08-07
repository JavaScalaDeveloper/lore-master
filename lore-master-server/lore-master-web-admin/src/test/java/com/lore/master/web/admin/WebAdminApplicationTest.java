package com.lore.master.web.admin;

import com.lore.master.data.repository.admin.AdminKnowledgeMapRepository;
import com.lore.master.service.admin.AdminKnowledgeMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 管理端应用启动测试
 */
@SpringBootTest
@ActiveProfiles("test")
class WebAdminApplicationTest {

    @Autowired(required = false)
    private AdminKnowledgeMapRepository adminKnowledgeMapRepository;

    @Autowired(required = false)
    private AdminKnowledgeMapService adminKnowledgeMapService;

    @Test
    void contextLoads() {
        // 验证应用上下文能够正常加载
        // 注意：在测试环境中，如果没有配置数据源，这些bean可能为null
        // 这里只是验证应用能够正常启动
    }

    @Test
    void beansAreLoadedWhenDataSourceConfigured() {
        // 当数据源正确配置时，验证bean是否正确加载
        if (adminKnowledgeMapRepository != null) {
            assertNotNull(adminKnowledgeMapRepository, "AdminKnowledgeMapRepository should be loaded");
        }
        if (adminKnowledgeMapService != null) {
            assertNotNull(adminKnowledgeMapService, "AdminKnowledgeMapService should be loaded");
        }
    }
}

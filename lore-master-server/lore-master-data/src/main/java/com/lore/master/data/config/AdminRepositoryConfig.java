package com.lore.master.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 管理端数据源Repository配置
 */
@Configuration
@ComponentScan(basePackages = "com.lore.master.data.repository.admin")
@EnableJpaRepositories(
        basePackages = "com.lore.master.data.repository.admin",
        entityManagerFactoryRef = "adminEntityManagerFactory",
        transactionManagerRef = "adminTransactionManager"
)
@EntityScan(basePackages = "com.lore.master.data.entity.admin")
public class AdminRepositoryConfig {
}

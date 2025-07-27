package com.lore.master.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 存储数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.lore.master.data.repository.storage",
        entityManagerFactoryRef = "storageEntityManagerFactory",
        transactionManagerRef = "storageTransactionManager"
)
public class StorageRepositoryConfig {
}

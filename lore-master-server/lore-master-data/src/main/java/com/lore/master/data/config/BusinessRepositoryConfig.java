package com.lore.master.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * B端数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.business",
    entityManagerFactoryRef = "businessEntityManagerFactory",
    transactionManagerRef = "businessTransactionManager"
)
public class BusinessRepositoryConfig {
}

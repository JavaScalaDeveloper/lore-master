package com.lore.master.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 主数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.lore.master.data.repository.consumer",
        entityManagerFactoryRef = "consumerEntityManagerFactory",
        transactionManagerRef = "consumerTransactionManager"
)
public class ConsumerRepositoryConfig {
}

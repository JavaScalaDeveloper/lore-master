package com.lore.master.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * B端数据源Repository配置
 */
@Configuration
@Order(1) // 设置更高的优先级
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.business",
    entityManagerFactoryRef = "businessEntityManagerFactory",
    transactionManagerRef = "businessTransactionManager"
)
@EntityScan(basePackages = "com.lore.master.data.entity.business")
public class BusinessRepositoryConfig {
}

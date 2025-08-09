package com.lore.master.data.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;

/**
 * 多数据源配置
 * 通用数据源配置，可被所有Web模块使用
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * 主数据源（C端用户数据库 - lore_consumer）
     */
    @Primary
    @Bean(name = "consumerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.consumer")
    public DataSource consumerDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "adminDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * B端数据源（B端业务数据库 - lore_business）
     */
    @Bean(name = "businessDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.business")
    public DataSource businessDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 存储数据源（文件存储数据库 - lore_middleware）
     */
    @Bean(name = "storageDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.storage")
    public DataSource storageDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 主数据源的EntityManagerFactory
     */
    @Primary
    @Bean(name = "consumerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean consumerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("consumerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.consumer", "com.lore.master.data.entity.business")
                .persistenceUnit("consumer")
                .build();
    }

    /**
     * 管理端数据源的EntityManagerFactory
     */
    @Bean(name = "adminEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean adminEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("adminDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.admin")
                .persistenceUnit("admin")
                .build();
    }

    /**
     * B端数据源的EntityManagerFactory
     */
    @Bean(name = "businessEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean businessEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("businessDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.business")
                .persistenceUnit("business")
                .build();
    }

    /**
     * 存储数据源的EntityManagerFactory
     */
    @Bean(name = "storageEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean storageEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("storageDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.storage")
                .persistenceUnit("storage")
                .build();
    }

    /**
     * 主数据源的事务管理器
     */
    @Primary
    @Bean(name = "consumerTransactionManager")
    public PlatformTransactionManager consumerTransactionManager(
            @Qualifier("consumerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * 主数据源的EntityManager
     */
    @Bean(name = "consumerEntityManager")
    public EntityManager consumerEntityManager(@Qualifier("consumerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean(name = "adminEntityManager")
    public EntityManager adminEntityManager(@Qualifier("adminEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * B端数据源的EntityManager
     */
    @Bean(name = "businessEntityManager")
    public EntityManager businessEntityManager(@Qualifier("businessEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * 存储数据源的EntityManager
     */
    @Bean(name = "storageEntityManager")
    public EntityManager storageEntityManager(@Qualifier("storageEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * 管理端数据源的事务管理器
     */
    @Bean(name = "adminTransactionManager")
    public PlatformTransactionManager adminTransactionManager(
            @Qualifier("adminEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * B端数据源的事务管理器
     */
    @Bean(name = "businessTransactionManager")
    public PlatformTransactionManager businessTransactionManager(
            @Qualifier("businessEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * 存储数据源的事务管理器
     */
    @Bean(name = "storageTransactionManager")
    public PlatformTransactionManager storageTransactionManager(
            @Qualifier("storageEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
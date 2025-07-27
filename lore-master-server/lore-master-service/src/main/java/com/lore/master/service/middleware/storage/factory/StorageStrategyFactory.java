package com.lore.master.service.middleware.storage.factory;

import com.lore.master.service.middleware.storage.strategy.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 存储策略工厂
 * 根据配置选择合适的存储策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageStrategyFactory {

    private final List<StorageStrategy> storageStrategies;

    @Value("${file-storage.strategy:mysql}")
    private String configuredStrategy;

    /**
     * 获取当前配置的存储策略
     * 
     * @return 存储策略实例
     */
    public StorageStrategy getCurrentStrategy() {
        return getStrategy(configuredStrategy);
    }

    /**
     * 根据类型获取存储策略
     * 
     * @param strategyType 策略类型
     * @return 存储策略实例
     */
    public StorageStrategy getStrategy(String strategyType) {
        Map<String, StorageStrategy> strategyMap = storageStrategies.stream()
                .collect(Collectors.toMap(
                        StorageStrategy::getStorageType,
                        Function.identity()
                ));

        StorageStrategy strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            log.warn("未找到存储策略: {}, 使用默认MySQL策略", strategyType);
            strategy = strategyMap.get("mysql");
            if (strategy == null) {
                throw new IllegalStateException("没有可用的存储策略");
            }
        }

        log.debug("使用存储策略: {}", strategy.getStorageType());
        return strategy;
    }

    /**
     * 获取所有可用的存储策略类型
     * 
     * @return 策略类型列表
     */
    public List<String> getAvailableStrategyTypes() {
        return storageStrategies.stream()
                .map(StorageStrategy::getStorageType)
                .collect(Collectors.toList());
    }

    /**
     * 验证当前策略配置
     * 
     * @return 配置是否有效
     */
    public boolean validateCurrentStrategy() {
        try {
            StorageStrategy currentStrategy = getCurrentStrategy();
            boolean isValid = currentStrategy.validateConfiguration();
            
            if (isValid) {
                log.info("存储策略配置验证成功: {}", currentStrategy.getStorageType());
            } else {
                log.error("存储策略配置验证失败: {}", currentStrategy.getStorageType());
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("存储策略配置验证异常", e);
            return false;
        }
    }

    /**
     * 获取当前策略的统计信息
     * 
     * @return 存储统计信息
     */
    public StorageStrategy.StorageStatistics getCurrentStrategyStatistics() {
        try {
            StorageStrategy currentStrategy = getCurrentStrategy();
            return currentStrategy.getStorageStatistics();
        } catch (Exception e) {
            log.error("获取存储策略统计信息失败", e);
            StorageStrategy.StorageStatistics statistics = new StorageStrategy.StorageStatistics();
            statistics.setTotalFiles(0);
            statistics.setTotalSize(0);
            statistics.setAvailableSpace(0);
            statistics.setUsagePercentage(0.0);
            return statistics;
        }
    }
}

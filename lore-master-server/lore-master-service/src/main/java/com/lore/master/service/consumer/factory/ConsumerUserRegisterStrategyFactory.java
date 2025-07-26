package com.lore.master.service.consumer.factory;

import com.lore.master.service.consumer.strategy.ConsumerUserRegisterStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C端用户注册策略工厂
 * 使用工厂模式管理所有注册策略
 */
@Slf4j
@Component
public class ConsumerUserRegisterStrategyFactory {

    private final Map<String, ConsumerUserRegisterStrategy> strategies;

    /**
     * 构造函数，自动注入所有ConsumerUserRegisterStrategy实现
     */
    @Autowired
    public ConsumerUserRegisterStrategyFactory(List<ConsumerUserRegisterStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        ConsumerUserRegisterStrategy::getSupportedType,
                        Function.identity()
                ));
        
        log.info("注册策略工厂初始化完成，支持的注册类型: {}", strategies.keySet());
    }

    /**
     * 根据注册类型获取对应的策略
     *
     * @param registerType 注册类型
     * @return 注册策略
     * @throws IllegalArgumentException 不支持的注册类型时抛出
     */
    public ConsumerUserRegisterStrategy getStrategy(String registerType) {
        ConsumerUserRegisterStrategy strategy = strategies.get(registerType);

        if (strategy == null) {
            throw new IllegalArgumentException("不支持的注册类型: " + registerType);
        }

        return strategy;
    }

    /**
     * 检查是否支持指定的注册类型
     * 
     * @param registerType 注册类型
     * @return true-支持，false-不支持
     */
    public boolean isSupported(String registerType) {
        return strategies.containsKey(registerType);
    }

    /**
     * 获取所有支持的注册类型
     * 
     * @return 支持的注册类型集合
     */
    public java.util.Set<String> getSupportedTypes() {
        return strategies.keySet();
    }

    /**
     * 获取所有策略实例
     *
     * @return 策略实例集合
     */
    public java.util.Collection<ConsumerUserRegisterStrategy> getAllStrategies() {
        return strategies.values();
    }
}

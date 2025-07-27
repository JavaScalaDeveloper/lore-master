package com.lore.master.service.consumer.factory;

import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C端用户登录策略工厂
 */
@Component
@RequiredArgsConstructor
public class ConsumerUserLoginStrategyFactory {

    private final List<ConsumerUserLoginStrategy> strategies;

    /**
     * 获取登录策略
     * 
     * @param loginType 登录类型
     * @return 登录策略
     */
    public ConsumerUserLoginStrategy getStrategy(String loginType) {
        Map<String, ConsumerUserLoginStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ConsumerUserLoginStrategy::getType,
                        Function.identity()
                ));

        ConsumerUserLoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的登录类型: " + loginType);
        }

        return strategy;
    }

    /**
     * 获取所有支持的登录类型
     * 
     * @return 登录类型列表
     */
    public List<String> getSupportedLoginTypes() {
        return strategies.stream()
                .map(ConsumerUserLoginStrategy::getType)
                .collect(Collectors.toList());
    }
}

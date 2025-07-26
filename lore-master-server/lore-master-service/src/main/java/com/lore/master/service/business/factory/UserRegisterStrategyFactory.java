package com.lore.master.service.business.factory;

import com.lore.master.service.business.strategy.UserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户注册策略工厂（工厂模式）
 */
@Component
@RequiredArgsConstructor
public class UserRegisterStrategyFactory {
    
    private final List<UserRegisterStrategy> strategies;
    private Map<String, UserRegisterStrategy> strategyMap;
    
    /**
     * 初始化策略映射
     */
    private void initStrategyMap() {
        if (strategyMap == null) {
            strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                    UserRegisterStrategy::getSupportedType,
                    Function.identity()
                ));
        }
    }
    
    /**
     * 根据注册类型获取对应的策略
     */
    public UserRegisterStrategy getStrategy(String registerType) {
        initStrategyMap();
        
        UserRegisterStrategy strategy = strategyMap.get(registerType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的注册类型: " + registerType);
        }
        
        return strategy;
    }
    
    /**
     * 获取所有支持的注册类型
     */
    public List<String> getSupportedTypes() {
        initStrategyMap();
        return List.copyOf(strategyMap.keySet());
    }
}

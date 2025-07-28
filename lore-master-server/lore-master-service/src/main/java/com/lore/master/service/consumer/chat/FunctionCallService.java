package com.lore.master.service.consumer.chat;

import java.util.Map;

/**
 * Function Call服务接口
 */
public interface FunctionCallService {

    /**
     * 执行函数调用
     * @param functionName 函数名称
     * @param parameters 参数
     * @return 执行结果
     */
    Object executeFunction(String functionName, Map<String, Object> parameters);

    /**
     * 获取可用的函数列表
     * @return 函数定义列表
     */
    Map<String, FunctionDefinition> getAvailableFunctions();

    /**
     * 函数定义类
     */
    class FunctionDefinition {
        private String name;
        private String description;
        private Map<String, Object> parameters;

        public FunctionDefinition(String name, String description, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }
}

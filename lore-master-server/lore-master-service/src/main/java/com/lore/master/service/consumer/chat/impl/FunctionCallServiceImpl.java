package com.lore.master.service.consumer.chat.impl;

import com.lore.master.service.consumer.chat.FunctionCallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Function Call服务实现类
 */
@Slf4j
@Service
public class FunctionCallServiceImpl implements FunctionCallService {

    private final Map<String, FunctionDefinition> functions;

    public FunctionCallServiceImpl() {
        this.functions = initializeFunctions();
    }

    @Override
    public Object executeFunction(String functionName, Map<String, Object> parameters) {
        log.info("执行函数调用: functionName={}, parameters={}", functionName, parameters);

        switch (functionName) {
            case "get_current_time":
                return getCurrentTime();
            case "get_weather":
                return getWeather((String) parameters.get("city"));
            case "calculate":
                return calculate((String) parameters.get("expression"));
            case "search_knowledge":
                return searchKnowledge((String) parameters.get("query"));
            case "get_user_info":
                return getUserInfo((String) parameters.get("userId"));
            case "send_notification":
                return sendNotification((String) parameters.get("message"), (String) parameters.get("userId"));
            default:
                return "未知的函数: " + functionName;
        }
    }

    @Override
    public Map<String, FunctionDefinition> getAvailableFunctions() {
        return new HashMap<>(functions);
    }

    private Map<String, FunctionDefinition> initializeFunctions() {
        Map<String, FunctionDefinition> funcs = new HashMap<>();

        // 获取当前时间
        Map<String, Object> timeParams = new HashMap<>();
        timeParams.put("type", "object");
        timeParams.put("properties", new HashMap<>());
        funcs.put("get_current_time", new FunctionDefinition(
                "get_current_time",
                "获取当前时间",
                timeParams
        ));

        // 获取天气信息
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("type", "object");
        Map<String, Object> weatherProps = new HashMap<>();
        Map<String, Object> cityProp = new HashMap<>();
        cityProp.put("type", "string");
        cityProp.put("description", "城市名称");
        weatherProps.put("city", cityProp);
        weatherParams.put("properties", weatherProps);
        weatherParams.put("required", new String[]{"city"});
        funcs.put("get_weather", new FunctionDefinition(
                "get_weather",
                "获取指定城市的天气信息",
                weatherParams
        ));

        // 计算器
        Map<String, Object> calcParams = new HashMap<>();
        calcParams.put("type", "object");
        Map<String, Object> calcProps = new HashMap<>();
        Map<String, Object> exprProp = new HashMap<>();
        exprProp.put("type", "string");
        exprProp.put("description", "数学表达式");
        calcProps.put("expression", exprProp);
        calcParams.put("properties", calcProps);
        calcParams.put("required", new String[]{"expression"});
        funcs.put("calculate", new FunctionDefinition(
                "calculate",
                "执行数学计算",
                calcParams
        ));

        // 知识搜索
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("type", "object");
        Map<String, Object> searchProps = new HashMap<>();
        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "搜索查询");
        searchProps.put("query", queryProp);
        searchParams.put("properties", searchProps);
        searchParams.put("required", new String[]{"query"});
        funcs.put("search_knowledge", new FunctionDefinition(
                "search_knowledge",
                "搜索知识库",
                searchParams
        ));

        // 获取用户信息
        Map<String, Object> userParams = new HashMap<>();
        userParams.put("type", "object");
        Map<String, Object> userProps = new HashMap<>();
        Map<String, Object> userIdProp = new HashMap<>();
        userIdProp.put("type", "string");
        userIdProp.put("description", "用户ID");
        userProps.put("userId", userIdProp);
        userParams.put("properties", userProps);
        userParams.put("required", new String[]{"userId"});
        funcs.put("get_user_info", new FunctionDefinition(
                "get_user_info",
                "获取用户信息",
                userParams
        ));

        // 发送通知
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("type", "object");
        Map<String, Object> notifyProps = new HashMap<>();
        Map<String, Object> msgProp = new HashMap<>();
        msgProp.put("type", "string");
        msgProp.put("description", "通知消息");
        Map<String, Object> userIdProp2 = new HashMap<>();
        userIdProp2.put("type", "string");
        userIdProp2.put("description", "用户ID");
        notifyProps.put("message", msgProp);
        notifyProps.put("userId", userIdProp2);
        notifyParams.put("properties", notifyProps);
        notifyParams.put("required", new String[]{"message", "userId"});
        funcs.put("send_notification", new FunctionDefinition(
                "send_notification",
                "发送通知给用户",
                notifyParams
        ));

        return funcs;
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private Map<String, Object> getWeather(String city) {
        // 模拟天气数据
        Random random = new Random();
        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", 15 + random.nextInt(20));
        weather.put("humidity", 40 + random.nextInt(40));
        weather.put("condition", random.nextBoolean() ? "晴天" : "多云");
        weather.put("windSpeed", 5 + random.nextInt(15));
        return weather;
    }

    private Map<String, Object> calculate(String expression) {
        try {
            // 简单的计算器实现（仅支持基本运算）
            double result = evaluateExpression(expression);
            Map<String, Object> calcResult = new HashMap<>();
            calcResult.put("expression", expression);
            calcResult.put("result", result);
            calcResult.put("success", true);
            return calcResult;
        } catch (Exception e) {
            Map<String, Object> calcResult = new HashMap<>();
            calcResult.put("expression", expression);
            calcResult.put("error", "计算错误: " + e.getMessage());
            calcResult.put("success", false);
            return calcResult;
        }
    }

    private double evaluateExpression(String expression) {
        // 简单的表达式计算（实际项目中应使用专业的表达式解析库）
        expression = expression.replaceAll("\\s+", "");
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        } else if (expression.contains("/")) {
            String[] parts = expression.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        } else {
            return Double.parseDouble(expression);
        }
    }

    private Map<String, Object> searchKnowledge(String query) {
        // 模拟知识搜索
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("results", "关于「" + query + "」的搜索结果：这是一个模拟的知识库搜索结果。");
        result.put("count", 1);
        return result;
    }

    private Map<String, Object> getUserInfo(String userId) {
        // 模拟用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("nickname", "用户" + userId);
        userInfo.put("level", "初级");
        userInfo.put("points", 1000);
        userInfo.put("joinDate", "2024-01-01");
        return userInfo;
    }

    private Map<String, Object> sendNotification(String message, String userId) {
        // 模拟发送通知
        log.info("发送通知给用户 {}: {}", userId, message);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "通知已发送");
        result.put("userId", userId);
        result.put("content", message);
        result.put("timestamp", getCurrentTime());
        return result;
    }
}

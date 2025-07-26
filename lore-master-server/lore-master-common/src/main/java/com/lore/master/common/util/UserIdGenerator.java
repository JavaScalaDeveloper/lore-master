package com.lore.master.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 用户ID生成器
 * 生成格式：YYYYXXXXXXXX 的唯一用户ID，防止遍历爬取
 *
 * 格式说明：
 * - YYYY: 年份前缀
 * - XXXXXXXX: 8位随机字母数字组合
 */
@Slf4j
@Component
public class UserIdGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SEQUENCE_NAME = "user_id_seq";
    private static final String[] CHARS = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F", "G", "H", "J", "K",
        "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z"  // 排除了容易混淆的字符：I, O
    };

    /**
     * 生成用户ID - 方式1：基于序列表（推荐）
     * 格式：YYYYXXXXXXXX
     */
    public String generateUserIdFromSequence() {
        try {
            // 获取并更新序列值
            String sql = "UPDATE user_id_sequence SET current_value = current_value + increment_step WHERE sequence_name = ?";
            jdbcTemplate.update(sql, SEQUENCE_NAME);

            // 获取当前序列值
            String querySql = "SELECT current_value FROM user_id_sequence WHERE sequence_name = ?";
            Long sequenceValue = jdbcTemplate.queryForObject(querySql, Long.class, SEQUENCE_NAME);

            if (sequenceValue == null) {
                throw new RuntimeException("无法获取用户ID序列值");
            }

            // 生成格式：YYYYXXXXXXXX
            String userId = generateFormattedUserId(sequenceValue, "SEQ");

            log.debug("基于序列生成用户ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("从序列生成用户ID失败", e);
            // 降级到时间戳方式
            return generateUserIdFromTimestamp();
        }
    }

    /**
     * 生成用户ID - 方式2：基于时间戳（备用方案）
     * 格式：YYYYXXXXXXXX
     */
    public String generateUserIdFromTimestamp() {
        try {
            // 使用当前时间戳作为种子
            long timestamp = System.currentTimeMillis();

            // 生成格式：YYYYXXXXXXXX
            String userId = generateFormattedUserId(timestamp, "TIME");

            log.debug("基于时间戳生成用户ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("基于时间戳生成用户ID失败", e);
            // 最后的降级方案
            return generateUserIdRandom();
        }
    }

    /**
     * 生成用户ID - 方式3：纯随机（最后降级方案）
     * 格式：YYYYXXXXXXXX
     */
    public String generateUserIdRandom() {
        // 使用随机数作为种子
        long randomSeed = SECURE_RANDOM.nextLong();

        // 生成格式：YYYYXXXXXXXX
        String userId = generateFormattedUserId(Math.abs(randomSeed), "RAND");

        log.debug("随机生成用户ID: {}", userId);
        return userId;
    }

    /**
     * 生成格式化的用户ID
     * 格式：YYYYXXXXXXXX
     */
    private String generateFormattedUserId(long seed, String type) {
        // 获取当前年份
        String year = String.valueOf(LocalDateTime.now().getYear());

        // 生成8位随机码
        String randomPart = generateRandomCode(8, seed + type.hashCode());

        return year + randomPart;
    }

    /**
     * 生成随机码
     */
    private String generateRandomCode(int length, long seed) {
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        random.setSeed(seed + System.nanoTime());

        for (int i = 0; i < length; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成用户ID - 主入口方法
     * 优先使用序列方式，失败时自动降级
     */
    public String generateUserId() {
        return generateUserIdFromSequence();
    }

    /**
     * 验证用户ID格式是否正确
     * 格式：YYYYXXXXXXXX
     */
    public boolean isValidUserId(String userId) {
        if (userId == null || userId.length() != 12) {
            return false;
        }

        // 检查格式：YYYYXXXXXXXX
        if (!userId.matches("^\\d{4}[0-9A-Z]{8}$")) {
            return false;
        }

        // 检查年份是否合理（2020-2099）
        String yearStr = userId.substring(0, 4);
        try {
            int year = Integer.parseInt(yearStr);
            return year >= 2020 && year <= 2099;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取用户ID的生成方式（通过最后一位校验码推断）
     */
    public String getUserIdGenerationType(String userId) {
        if (!isValidUserId(userId)) {
            return "invalid";
        }

        // 通过最后一位字符推断生成方式（这只是示例，实际可以更复杂）
        char lastChar = userId.charAt(userId.length() - 1);
        if (lastChar >= '0' && lastChar <= '9') {
            return "sequence";
        } else if (lastChar >= 'A' && lastChar <= 'M') {
            return "timestamp";
        } else {
            return "random";
        }
    }

    /**
     * 批量生成用户ID（用于测试或批量导入）
     */
    public String[] generateUserIds(int count) {
        String[] userIds = new String[count];
        for (int i = 0; i < count; i++) {
            userIds[i] = generateUserId();
            
            // 避免在同一毫秒内生成重复ID
            if (i % 100 == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return userIds;
    }

    /**
     * 检查用户ID是否已存在
     */
    public boolean isUserIdExists(String userId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查用户ID是否存在失败: {}", userId, e);
            return false;
        }
    }

    /**
     * 生成唯一的用户ID（确保不重复）
     */
    public String generateUniqueUserId() {
        String userId;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            userId = generateUserId();
            attempts++;
            
            if (attempts > maxAttempts) {
                log.error("生成唯一用户ID失败，尝试次数超过限制: {}", maxAttempts);
                throw new RuntimeException("无法生成唯一用户ID");
            }
            
        } while (isUserIdExists(userId));
        
        log.info("生成唯一用户ID成功: {} (尝试次数: {})", userId, attempts);
        return userId;
    }
}

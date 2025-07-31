package com.lore.master.data.entity.consumer;

import lombok.Getter;

/**
 * 用户学习目标状态枚举
 */
@Getter
public enum UserLearningGoalStatusEnum {

    ACTIVE("active", "进行中"),
    COMPLETED("completed", "已完成"),
    PAUSED("paused", "暂停"),
    ABANDONED("abandoned", "放弃");

    private final String code;
    private final String desc;

    UserLearningGoalStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static UserLearningGoalStatusEnum getByCode(String code) {
        for (UserLearningGoalStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
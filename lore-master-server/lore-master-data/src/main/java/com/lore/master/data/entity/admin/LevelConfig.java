package com.lore.master.data.entity.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 等级配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "level_configs")
public class LevelConfig {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 等级
     */
    @Column(name = "level", nullable = false, unique = true)
    private Integer level;
    
    /**
     * 等级名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 等级描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 等级图标
     */
    @Column(name = "icon", length = 200)
    private String icon;
    
    /**
     * 等级颜色
     */
    @Column(name = "color", length = 20)
    private String color;
    
    /**
     * 所需最小积分
     */
    @Column(name = "min_score", nullable = false)
    private Integer minScore;
    
    /**
     * 所需最大积分（-1表示无上限）
     */
    @Column(name = "max_score")
    private Integer maxScore;
    
    /**
     * 升级奖励积分
     */
    @Column(name = "reward_score")
    private Integer rewardScore;
    
    /**
     * 等级特权（JSON格式）
     */
    @Column(name = "privileges", columnDefinition = "TEXT")
    private String privileges;
    
    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 修改时间
     */
    @UpdateTimestamp
    @Column(name = "modify_time", nullable = false)
    private LocalDateTime modifyTime;
}

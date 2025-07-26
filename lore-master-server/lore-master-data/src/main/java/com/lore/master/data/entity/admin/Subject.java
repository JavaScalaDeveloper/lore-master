package com.lore.master.data.entity.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 学科实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "subjects")
public class Subject {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 学科名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 学科编码
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * 学科描述
     */
    @Column(name = "description", length = 1000)
    private String description;
    
    /**
     * 学科图标
     */
    @Column(name = "icon", length = 200)
    private String icon;
    
    /**
     * 学科颜色
     */
    @Column(name = "color", length = 20)
    private String color;
    
    /**
     * 父级学科ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 学科层级
     */
    @Column(name = "level")
    private Integer level;
    
    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
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

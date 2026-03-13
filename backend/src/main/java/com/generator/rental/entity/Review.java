package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reviews")
public class Review extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long tenantId;
    private Long generatorId;

    @TableField(exist = false)
    private User tenant;

    @TableField(exist = false)
    private Generator generator;

    private Integer rating;
    private String comment;
}

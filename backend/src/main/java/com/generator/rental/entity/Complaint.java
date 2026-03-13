package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("complaints")
public class Complaint extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long complainantId;

    private Long respondentId;

    @TableField(exist = false)
    private User complainant;

    @TableField(exist = false)
    private User respondent;

    private String content;
    private Status status = Status.PENDING;
    private String resolution;

    public enum Status {
        PENDING, PROCESSING, RESOLVED, REJECTED
    }
}

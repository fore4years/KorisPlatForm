package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_requests")
public class RepairRequest extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    @TableField(exist = false)
    private Order order;

    private String description;
    private String imageUrl;
    private Status status = Status.PENDING;
    private String merchantResponse;

    public enum Status {
        PENDING, PROCESSING, COMPLETED
    }
}

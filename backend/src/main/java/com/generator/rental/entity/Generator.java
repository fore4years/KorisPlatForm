package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("generators")
public class Generator extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    @TableField(exist = false)
    private User merchant;

    private String name;
    private String power;
    private String fuelConsumption;
    private String weight;
    private String size;
    private BigDecimal dailyRent;
    private BigDecimal weeklyRent;
    private BigDecimal monthlyRent;
    private BigDecimal deposit;
    private String deliveryMethod;
    private Integer deliveryRange; // in km
    private BigDecimal deliveryFee;
    private String imageUrl;
    private String proofOfOwnershipUrl; // 权属证明
    private StockStatus stockStatus = StockStatus.AVAILABLE;
    private AuditStatus auditStatus = AuditStatus.PENDING;
    private String rejectionReason;
    private String description;
    
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public enum StockStatus {
        AVAILABLE, RENTED, MAINTENANCE
    }

    public enum AuditStatus {
        PENDING, APPROVED, REJECTED
    }
}

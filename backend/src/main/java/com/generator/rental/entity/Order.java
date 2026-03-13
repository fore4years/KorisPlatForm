package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long merchantId;
    private Long generatorId;

    @TableField(exist = false)
    private User tenant;

    @TableField(exist = false)
    private User merchant;

    @TableField(exist = false)
    private Generator generator;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal serviceFee;

    private Status status = Status.WAIT_CONFIRM;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
    private DeliveryType deliveryType;
    private String deliveryEvidenceUrl;

    private BigDecimal deductionAmount;
    private BigDecimal refundAmount;
    private String returnEvidenceUrl;
    private String transactionId;

    public enum Status {
        WAIT_PAY, WAIT_CONFIRM, CONFIRMED, DELIVERED, RENTING, WAIT_RETURN, COMPLETED, CANCELLED
    }

    public enum PaymentMethod {
        ONLINE, OFFLINE
    }

    public enum PaymentStatus {
        PENDING, PAID
    }

    public enum DeliveryType {
        DELIVERY, PICKUP
    }
}

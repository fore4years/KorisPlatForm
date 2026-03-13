package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_applications")
public class MerchantApplication extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private String merchantName;
    private String contactPhone;
    private String address;
    private String serviceType;
    private MerchantType merchantType;

    // Personal Merchant Fields
    private String idCardFront;
    private String idCardBack;

    // Enterprise Merchant Fields
    private String businessLicense;
    private String legalPersonIdFront;
    private String legalPersonIdBack;

    // Common Proofs
    private String generatorOwnerCert;
    private String generatorPhoto;

    // Status
    private Status status = Status.PENDING;
    private String rejectionReason;
    private String auditBy;
    private LocalDateTime auditTime;

    public enum MerchantType {
        PERSONAL, ENTERPRISE
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}

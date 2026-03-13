package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String username;
    private String password;
    private String phone;
    private Role role;
    private String name;
    private String identityCard;
    private String businessLicense;
    private String rejectionReason;

    // New Fields for Profile
    private String avatar;
    private String address;
    private String companyName;
    private String contactPerson;
    private String businessScope;
    private String deliveryRange;
    private String pendingInfo; // JSON string for pending audit info

    private Status status = Status.ACTIVE;

    public enum Role {
        TENANT, MERCHANT, ADMIN
    }

    public enum Status {
        PENDING, ACTIVE, DISABLED
    }

    // PrePersist is JPA, manual handling in Service or DB default needed for userId if not set
    // However, for consistency, we'll handle userId generation in Service or here if possible.
    // MP doesn't support @PrePersist. We'll handle it in Service.
}

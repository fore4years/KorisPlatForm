package com.generator.rental.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String avatar;
    private String address;
    private String companyName;
    private String contactPerson;
    private String businessScope;
    private String deliveryRange;
    // Sensitive fields that might need audit
    private String identityCard;
    private String businessLicense;
}

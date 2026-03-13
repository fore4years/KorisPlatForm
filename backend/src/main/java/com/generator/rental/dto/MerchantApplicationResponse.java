package com.generator.rental.dto;

import com.generator.rental.entity.MerchantApplication;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantApplicationResponse {
    private Long id;
    private String userId;
    private String username;
    private String merchantName;
    private String contactPhone;
    private String address;
    private String serviceType;
    private MerchantApplication.MerchantType merchantType;
    
    private String idCardFront;
    private String idCardBack;
    private String businessLicense;
    private String legalPersonIdFront;
    private String legalPersonIdBack;
    
    private String generatorOwnerCert;
    private String generatorPhoto;
    
    private MerchantApplication.Status status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime auditTime;
}

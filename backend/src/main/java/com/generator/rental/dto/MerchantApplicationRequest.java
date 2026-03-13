package com.generator.rental.dto;

import com.generator.rental.entity.MerchantApplication;
import lombok.Data;

@Data
public class MerchantApplicationRequest {
    private String merchantName;
    private String contactPhone;
    private String address;
    private String serviceType;
    private MerchantApplication.MerchantType merchantType;
    
    // Personal
    private String idCardFront;
    private String idCardBack;
    
    // Enterprise
    private String businessLicense;
    private String legalPersonIdFront;
    private String legalPersonIdBack;
    
    // Common
    private String generatorOwnerCert;
    private String generatorPhoto;
}

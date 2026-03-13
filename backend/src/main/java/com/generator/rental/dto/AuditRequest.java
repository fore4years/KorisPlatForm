package com.generator.rental.dto;

import com.generator.rental.entity.MerchantApplication;
import lombok.Data;

@Data
public class AuditRequest {
    private MerchantApplication.Status status; // APPROVED or REJECTED
    private String rejectionReason;
}

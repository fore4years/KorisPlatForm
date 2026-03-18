package com.generator.rental.service;

import com.generator.rental.dto.AuditRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.entity.User;
import com.generator.rental.entity.PlatformConfig;

import java.util.List;

public interface AdminService {
    // Deprecated or Modified
    List<User> getPendingMerchants();
    void approveMerchant(String userId);
    void rejectMerchant(String userId, String reason);
    
    // New methods
    List<MerchantApplicationResponse> getMerchantApplications(String status);
    MerchantApplicationResponse auditApplication(Long applicationId, String adminId, AuditRequest request);
    
    List<User> getAllUsers();
    void updateUserStatus(String userId, User.Status status);
    
    List<PlatformConfig> getAllConfigs();
    void updateConfig(String key, String value);
    void initConfigs();

    // Equipment Audit
    List<com.generator.rental.entity.Generator> getPendingGenerators();
    void auditGenerator(Long generatorId, com.generator.rental.entity.Generator.AuditStatus status, String reason);
}

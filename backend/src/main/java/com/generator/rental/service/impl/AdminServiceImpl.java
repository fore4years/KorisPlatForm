package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generator.rental.dto.AuditRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.entity.MerchantApplication;
import com.generator.rental.entity.PlatformConfig;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.MerchantApplicationMapper;
import com.generator.rental.mapper.PlatformConfigMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.AdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantApplicationMapper merchantApplicationMapper;

    @Autowired
    private PlatformConfigMapper configMapper;

    @Override
    public List<User> getPendingMerchants() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, User.Role.MERCHANT)
                .eq(User::getStatus, User.Status.PENDING));
    }

    @Override
    public void approveMerchant(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) throw new RuntimeException("User not found");
        user.setStatus(User.Status.ACTIVE);
        user.setRejectionReason(null);
        userMapper.updateById(user);
    }

    @Override
    public void rejectMerchant(String userId, String reason) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) throw new RuntimeException("User not found");
        user.setStatus(User.Status.DISABLED);
        user.setRejectionReason(reason);
        userMapper.updateById(user);
    }

    @Override
    public List<MerchantApplicationResponse> getMerchantApplications(String status) {
        LambdaQueryWrapper<MerchantApplication> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq(MerchantApplication::getStatus, MerchantApplication.Status.valueOf(status));
        }
        
        List<MerchantApplication> apps = merchantApplicationMapper.selectList(query);
        return apps.stream().map(this::convertToAppResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MerchantApplicationResponse auditApplication(Long applicationId, String adminId, AuditRequest request) {
        MerchantApplication app = merchantApplicationMapper.selectById(applicationId);
        if (app == null) throw new RuntimeException("Application not found");
        
        if (app.getStatus() != MerchantApplication.Status.PENDING) {
            throw new RuntimeException("Application is not pending");
        }

        app.setStatus(request.getStatus());
        app.setAuditBy(adminId);
        app.setAuditTime(LocalDateTime.now());
        app.setRejectionReason(request.getRejectionReason());

        if (request.getStatus() == MerchantApplication.Status.APPROVED) {
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                user.setRole(User.Role.MERCHANT);
                user.setCompanyName(app.getMerchantName());
                user.setContactPerson(app.getMerchantName());
                user.setAddress(app.getAddress());
                user.setBusinessLicense(app.getBusinessLicense());
                user.setIdentityCard(app.getIdCardFront());
                userMapper.updateById(user);
                
                // Set back for response
                app.setUser(user);
            }
        } else {
             User user = userMapper.selectById(app.getUserId());
             app.setUser(user);
        }

        merchantApplicationMapper.updateById(app);
        return convertToAppResponse(app);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public void updateUserStatus(String userId, User.Status status) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) throw new RuntimeException("User not found");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public List<PlatformConfig> getAllConfigs() {
        initConfigs();
        return configMapper.selectList(null);
    }

    @Override
    public void updateConfig(String key, String value) {
        PlatformConfig config = configMapper.selectOne(new LambdaQueryWrapper<PlatformConfig>().eq(PlatformConfig::getConfigKey, key));
        if (config == null) throw new RuntimeException("Config not found");
        config.setValue(value);
        configMapper.updateById(config);
    }

    @Override
    public void initConfigs() {
        if (configMapper.selectCount(null) == 0) {
            createConfig("service_fee_rate", "0.05", "Service Fee Rate (e.g. 0.05 for 5%)");
            createConfig("deposit_refund_days", "3", "Days to refund deposit after return");
            createConfig("dispute_process", "Standard Mediation", "Dispute resolution process description");
        }
    }

    private void createConfig(String key, String value, String description) {
        PlatformConfig config = new PlatformConfig();
        config.setKey(key);
        config.setValue(value);
        config.setDescription(description);
        configMapper.insert(config);
    }
    
    private MerchantApplicationResponse convertToAppResponse(MerchantApplication app) {
        MerchantApplicationResponse response = new MerchantApplicationResponse();
        BeanUtils.copyProperties(app, response);
        
        if (app.getUser() == null && app.getUserId() != null) {
            User user = userMapper.selectById(app.getUserId());
            app.setUser(user);
        }
        
        if (app.getUser() != null) {
            response.setUserId(app.getUser().getUserId());
            response.setUsername(app.getUser().getUsername());
        }
        response.setCreatedAt(app.getCreateTime());
        return response;
    }
}

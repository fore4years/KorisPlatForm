package com.generator.rental.service;

import com.generator.rental.dto.LoginRequest;
import com.generator.rental.dto.MerchantApplicationRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.dto.RegisterRequest;
import com.generator.rental.dto.UserResponse;
import com.generator.rental.dto.UserUpdateRequest;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
    UserResponse getProfile(String userId);
    UserResponse updateProfile(String userId, UserUpdateRequest request);
    
    // New methods
    MerchantApplicationResponse submitMerchantApplication(String userId, MerchantApplicationRequest request);
    MerchantApplicationResponse getMyMerchantApplication(String userId);

    // Password Reset
    void sendForgotPasswordCode(String phone);
    void resetPassword(String phone, String code, String newPassword);
}

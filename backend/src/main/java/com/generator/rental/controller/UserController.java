package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.MerchantApplicationRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.dto.UserResponse;
import com.generator.rental.dto.UserUpdateRequest;
import com.generator.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取个人资料
     *
     * @param userId 用户ID
     * @return 用户资料响应 DTO
     */
    @GetMapping("/profile")
    public Result<UserResponse> getProfile(@RequestHeader("X-User-ID") String userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 更新个人资料
     *
     * @param userId 用户ID
     * @param request 用户更新请求 DTO
     * @return 更新后的用户资料响应
     */
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateProfile(userId, request));
    }

    /**
     * 提交商户申请
     *
     * @param userId 用户ID
     * @param request 商户申请请求 DTO
     * @return 商户申请响应
     */
    @PostMapping("/merchant-application")
    public Result<MerchantApplicationResponse> submitMerchantApplication(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody MerchantApplicationRequest request) {
        return Result.success(userService.submitMerchantApplication(userId, request));
    }

    /**
     * 获取我的商户申请状态
     *
     * @param userId 用户ID
     * @return 商户申请响应
     */
    @GetMapping("/merchant-application")
    public Result<MerchantApplicationResponse> getMyMerchantApplication(
            @RequestHeader("X-User-ID") String userId) {
        return Result.success(userService.getMyMerchantApplication(userId));
    }
}

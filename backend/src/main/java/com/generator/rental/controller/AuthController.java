package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.LoginRequest;
import com.generator.rental.dto.RegisterRequest;
import com.generator.rental.dto.UserResponse;
import com.generator.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.generator.rental.dto.ForgotPasswordRequest;
import com.generator.rental.dto.ResetPasswordRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @param request 注册请求 DTO
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求 DTO
     * @return 登录成功的用户信息
     */
    @PostMapping("/login")
    public Result<UserResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 发送忘记密码验证码
     *
     * @param request 忘记密码请求 DTO (包含手机号)
     * @return 成功提示信息
     */
    @PostMapping("/forgot-password/send-code")
    public Result<String> sendForgotPasswordCode(@RequestBody ForgotPasswordRequest request) {
        userService.sendForgotPasswordCode(request.getPhone());
        return Result.success("Verification code sent");
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求 DTO
     * @return 成功提示信息
     */
    @PostMapping("/forgot-password/reset")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
        return Result.success("Password reset successfully");
    }
}

package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.AuditRequest;
import com.generator.rental.dto.MerchantApplicationResponse;
import com.generator.rental.dto.StatisticsDTO;
import com.generator.rental.entity.PlatformConfig;
import com.generator.rental.entity.User;
import com.generator.rental.service.AdminService;
import com.generator.rental.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private StatisticsService statisticsService;

    // --- Statistics ---

    /**
     * 获取管理员统计数据
     *
     * @param dimension 统计维度 (day, week, month)
     * @return 统计数据 DTO
     */
    @GetMapping("/stats")
    public Result<StatisticsDTO> getAdminStats(@RequestParam(defaultValue = "day") String dimension) {
        return Result.success(statisticsService.getStatistics(dimension));
    }

    // --- Merchant Audit (Old - Deprecated) ---

    /**
     * 获取待审核商户列表（旧接口）
     *
     * @return 待审核商户列表
     */
    @GetMapping("/merchants/pending")
    public Result<List<User>> getPendingMerchants() {
        return Result.success(adminService.getPendingMerchants());
    }

    /**
     * 批准商户申请（旧接口）
     *
     * @param userId 商户ID
     * @return 空响应
     */
    @PostMapping("/merchants/{userId}/approve")
    public Result<Void> approveMerchant(@PathVariable String userId) {
        adminService.approveMerchant(userId);
        return Result.success();
    }

    /**
     * 拒绝商户申请（旧接口）
     *
     * @param userId 商户ID
     * @param payload 包含拒绝理由的 Map
     * @return 空响应
     */
    @PostMapping("/merchants/{userId}/reject")
    public Result<Void> rejectMerchant(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        adminService.rejectMerchant(userId, payload.get("reason"));
        return Result.success();
    }

    // --- Merchant Audit (New) ---

    /**
     * 获取商户申请列表
     *
     * @param status 申请状态 (可选)
     * @return 商户申请响应列表
     */
    @GetMapping("/merchant-applications")
    public Result<List<MerchantApplicationResponse>> getMerchantApplications(
            @RequestParam(required = false) String status) {
        return Result.success(adminService.getMerchantApplications(status));
    }

    /**
     * 审核商户申请
     *
     * @param id 申请ID
     * @param adminId 管理员ID
     * @param request 审核请求 DTO
     * @return 商户申请响应
     */
    @PostMapping("/merchant-applications/{id}/audit")
    public Result<MerchantApplicationResponse> auditApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String adminId,
            @RequestBody AuditRequest request) {
        return Result.success(adminService.auditApplication(id, adminId, request));
    }

    // --- User Management ---

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        return Result.success(adminService.getAllUsers());
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param payload 包含新状态的 Map
     * @return 空响应
     */
    @PostMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        adminService.updateUserStatus(userId, User.Status.valueOf(payload.get("status")));
        return Result.success();
    }

    // --- Platform Config ---

    /**
     * 获取所有平台配置
     *
     * @return 平台配置列表
     */
    @GetMapping("/config")
    public Result<List<PlatformConfig>> getAllConfigs() {
        return Result.success(adminService.getAllConfigs());
    }

    /**
     * 更新平台配置
     *
     * @param payload 包含配置键值的 Map
     * @return 空响应
     */
    @PostMapping("/config/update")
    public Result<Void> updateConfig(@RequestBody Map<String, String> payload) {
        adminService.updateConfig(payload.get("key"), payload.get("value"));
        return Result.success();
    }

    // --- Generator Audit ---

    /**
     * 获取待审核发电机列表
     *
     * @return 待审核发电机列表
     */
    @GetMapping("/generators/pending")
    public Result<List<Generator>> getPendingGenerators() {
        return Result.success(adminService.getPendingGenerators());
    }

    /**
     * 审核发电机
     *
     * @param id 发电机ID
     * @param payload 包含 status 和 reason 的 Map
     * @return 空响应
     */
    @PostMapping("/generators/{id}/audit")
    public Result<Void> auditGenerator(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Generator.AuditStatus status = Generator.AuditStatus.valueOf(payload.get("status"));
        String reason = payload.get("reason");
        adminService.auditGenerator(id, status, reason);
        return Result.success();
    }
}

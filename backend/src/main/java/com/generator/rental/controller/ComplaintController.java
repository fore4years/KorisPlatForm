package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.entity.Complaint;
import com.generator.rental.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * 创建投诉
     *
     * @param payload 投诉信息 Map (orderId, complainantId, content)
     * @return 创建的投诉实体
     */
    @PostMapping("/create")
    public Result<Complaint> createComplaint(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String complainantUserId = (String) payload.get("complainantId");
        String content = (String) payload.get("content");
        return Result.success(complaintService.createComplaint(orderId, complainantUserId, content));
    }

    // Admin endpoint to list all

    /**
     * 获取所有投诉列表（管理员）
     *
     * @return 投诉列表
     */
    @GetMapping("/admin/all")
    public Result<List<Complaint>> getAllComplaints() {
        return Result.success(complaintService.getAllComplaints());
    }

    /**
     * 处理/解决投诉
     *
     * @param id 投诉ID
     * @param payload 包含处理结果的 Map (resolution)
     * @return 空响应
     */
    @PostMapping("/{id}/resolve")
    public Result<Void> resolveComplaint(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        complaintService.resolveComplaint(id, payload.get("resolution"));
        return Result.success();
    }
}

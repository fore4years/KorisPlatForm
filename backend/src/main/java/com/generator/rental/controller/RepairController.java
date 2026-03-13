package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.entity.RepairRequest;
import com.generator.rental.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    @Autowired
    private RepairService repairService;

    /**
     * 创建维修请求
     *
     * @param payload 包含orderId, description, imageUrl的Map
     * @return 创建的维修请求实体
     */
    @PostMapping("/create")
    public Result<RepairRequest> create(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String description = (String) payload.get("description");
        String imageUrl = (String) payload.get("imageUrl");
        return Result.success(repairService.createRequest(orderId, description, imageUrl));
    }

    /**
     * 更新维修请求状态
     *
     * @param id 维修请求ID
     * @param payload 包含status, response的Map
     * @return 更新后的维修请求实体
     */
    @PostMapping("/{id}/update")
    public Result<RepairRequest> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return Result.success(repairService.updateStatus(id, payload.get("status"), payload.get("response")));
    }

    /**
     * 根据订单ID获取维修请求列表
     *
     * @param orderId 订单ID
     * @return 维修请求列表
     */
    @GetMapping("/order/{orderId}")
    public Result<List<RepairRequest>> getByOrder(@PathVariable Long orderId) {
        return Result.success(repairService.getByOrder(orderId));
    }
}

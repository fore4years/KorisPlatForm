package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.OrderRequest;
import com.generator.rental.dto.OrderResponse;
import com.generator.rental.dto.PriceCalculationResponse;
import com.generator.rental.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 计算订单价格
     *
     * @param payload 包含generatorId, startTime, endTime的Map
     * @return 价格计算结果
     */
    @PostMapping("/calculate-price")
    @PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
    public Result<PriceCalculationResponse> calculatePrice(@RequestBody Map<String, Object> payload) {
        Long generatorId = Long.valueOf(payload.get("generatorId").toString());
        String startTime = (String) payload.get("startTime");
        String endTime = (String) payload.get("endTime");
        return Result.success(orderService.calculatePrice(generatorId, startTime, endTime));
    }

    /**
     * 创建订单
     *
     * @param request 订单请求 DTO
     * @param tenantId 租户ID
     * @return 订单响应
     */
    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public Result<OrderResponse> createOrder(@RequestBody OrderRequest request, @RequestHeader("X-User-ID") String tenantId) {
        return Result.success(orderService.createOrder(request, tenantId));
    }

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("authenticated()")
    public Result<OrderResponse> getDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 商家确认订单
     *
     * @param id 订单ID
     * @return 订单响应
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<OrderResponse> confirmOrder(@PathVariable Long id) {
        return Result.success(orderService.confirmOrder(id));
    }

    /**
     * 更新物流方式
     *
     * @param id 订单ID
     * @param payload 包含deliveryType的Map
     * @return 订单响应
     */
    @PostMapping("/{id}/delivery")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<OrderResponse> updateDelivery(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return Result.success(orderService.updateDelivery(id, payload.get("deliveryType")));
    }

    /**
     * 上传发货/收货凭证
     *
     * @param id 订单ID
     * @param payload 包含evidenceUrl的Map
     * @return 订单响应
     */
    @PostMapping("/{id}/evidence")
    @PreAuthorize("hasAnyRole('MERCHANT', 'TENANT')")
    public Result<OrderResponse> uploadEvidence(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return Result.success(orderService.uploadDeliveryEvidence(id, payload.get("evidenceUrl")));
    }

    /**
     * 确认收货
     *
     * @param id 订单ID
     * @return 订单响应
     */
    @PostMapping("/{id}/receipt")
    @PreAuthorize("hasRole('TENANT')")
    public Result<OrderResponse> confirmReceipt(@PathVariable Long id) {
        return Result.success(orderService.confirmReceipt(id));
    }

    /**
     * 获取商户订单列表
     *
     * @param merchantUserId 商户用户ID
     * @return 订单列表
     */
    @GetMapping("/merchant/{merchantUserId}")
    public Result<List<OrderResponse>> getMerchantOrders(@PathVariable String merchantUserId) {
        return Result.success(orderService.getMerchantOrders(merchantUserId));
    }

    /**
     * 获取商家的客户列表
     */
    @GetMapping("/merchant/{merchantUserId}/customers")
    public Result<List<com.generator.rental.dto.MerchantCustomerDTO>> getMerchantCustomers(@PathVariable String merchantUserId) {
        return Result.success(orderService.getMerchantCustomers(merchantUserId));
    }

    /**
     * 获取商家特定客户的订单历史
     */
    @GetMapping("/merchant/{merchantUserId}/customers/{tenantId}/history")
    public Result<List<OrderResponse>> getCustomerOrderHistory(
            @PathVariable String merchantUserId,
            @PathVariable Long tenantId) {
        return Result.success(orderService.getCustomerOrderHistory(merchantUserId, tenantId));
    }

    /**
     * 获取租户订单列表
     *
     * @param tenantUserId 租户用户ID
     * @return 订单列表
     */
    @GetMapping("/tenant/{tenantUserId}")
    @PreAuthorize("authenticated()")
    public Result<List<OrderResponse>> getTenantOrders(@PathVariable String tenantUserId) {
        return Result.success(orderService.getTenantOrders(tenantUserId));
    }

    /**
     * 完成订单（正常结束）
     *
     * @param id 订单ID
     * @return 订单响应
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('TENANT')")
    public Result<OrderResponse> completeOrder(@PathVariable Long id) {
        return Result.success(orderService.completeOrder(id));
    }

    /**
     * 确认归还（结算）
     *
     * @param id 订单ID
     * @param payload 包含deduction, comment的Map
     * @return 订单响应
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<OrderResponse> confirmReturn(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        java.math.BigDecimal deduction = payload.get("deduction") != null ? new java.math.BigDecimal(payload.get("deduction").toString()) : java.math.BigDecimal.ZERO;
        String comment = (String) payload.get("comment");
        return Result.success(orderService.confirmReturn(id, deduction, comment));
    }
}

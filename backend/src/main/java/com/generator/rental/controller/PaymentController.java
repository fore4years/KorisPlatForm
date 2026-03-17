package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private AlipayService alipayService;

    /**
     * 发起支付
     *
     * @param orderId 订单ID
     * @return 包含支付表单HTML的 Map
     */
    @PostMapping("/pay/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> pay(@PathVariable Long orderId) {
        // Alipay returns an HTML form, we send it back to frontend
        String formHtml = alipayService.createPayOrder(orderId);
        
        Map<String, String> response = new HashMap<>();
        response.put("form", formHtml);
        return Result.success(response);
    }
    
    /**
     * 支付成功回调（前端通知）
     *
     * @param orderId 订单ID
     * @param payload 包含交易号的 Map (tradeNo)
     * @return 成功提示
     */
    @PostMapping("/success/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<String> paymentSuccess(@PathVariable Long orderId, @RequestBody Map<String, String> payload) {
        String tradeNo = payload.get("tradeNo");
        alipayService.handlePaymentSuccess(orderId, tradeNo);
        return Result.success("SUCCESS");
    }

    // Notify endpoint would be here

    /**
     * 支付宝异步通知（后端回调）
     *
     * @param request HTTP请求
     * @return 响应字符串 (success/fail)
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        // Implement callback handling
        // Verify signature, update order status
        return "success";
    }
}

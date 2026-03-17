package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.entity.Contract;
import com.generator.rental.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@PreAuthorize("isAuthenticated()")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /**
     * 根据订单ID获取合同
     *
     * @param orderId 订单ID
     * @return 合同实体
     */
    @GetMapping("/order/{orderId}")
    public Result<Contract> getContract(@PathVariable Long orderId) {
        return Result.success(contractService.getContractByOrderId(orderId));
    }

    /**
     * 签署合同
     *
     * @param payload 签署信息 Map (orderId, userId, role)
     * @return 更新后的合同实体
     */
    @PostMapping("/sign")
    @PreAuthorize("hasAnyRole('TENANT', 'MERCHANT')")
    public Result<Contract> signContract(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String userId = (String) payload.get("userId");
        String role = (String) payload.get("role");
        
        System.out.println("Sign Request: OrderId=" + orderId + ", UserId=" + userId + ", Role=" + role);
        
        return Result.success(contractService.signContract(orderId, userId, role));
    }
}

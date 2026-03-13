package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.entity.Contract;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.ContractMapper;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Contract generateContract(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");

        User tenant = userMapper.selectById(order.getTenantId());
        User merchant = userMapper.selectById(order.getMerchantId());

        String content = String.format(
                "租赁合同\n\n订单编号: %d\n租户: %s (ID: %s)\n商户: %s (ID: %s)\n设备ID: %d\n租期: %s 至 %s\n总金额: %.2f\n押金: %.2f",
                order.getId(),
                tenant.getName(), tenant.getUserId(),
                merchant.getName(), merchant.getUserId(),
                order.getGeneratorId(),
                order.getStartTime(), order.getEndTime(),
                order.getTotalAmount(), order.getDepositAmount()
        );

        Contract contract = new Contract();
        contract.setOrderId(orderId);
        contract.setContent(content);
        
        save(contract);
        return contract;
    }

    @Override
    public Contract getContractByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<Contract>().eq(Contract::getOrderId, orderId));
    }

    @Override
    public Contract signContract(Long orderId, String userId, String role) {
        Contract contract = getOne(new LambdaQueryWrapper<Contract>().eq(Contract::getOrderId, orderId));
        if (contract == null) throw new RuntimeException("Contract not found");

        // Optional: Validate userId matches tenant/merchant of the order
        // Order order = orderMapper.selectById(orderId);
        // ... validation logic ...

        if ("TENANT".equals(role)) {
            contract.setTenantSigned(true);
        } else if ("MERCHANT".equals(role)) {
            contract.setMerchantSigned(true);
        }
        
        if (Boolean.TRUE.equals(contract.getTenantSigned()) && Boolean.TRUE.equals(contract.getMerchantSigned())) {
            contract.setSignDate(LocalDateTime.now());
        }
        
        updateById(contract);
        return contract;
    }
}

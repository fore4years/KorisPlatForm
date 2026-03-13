package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.RepairRequest;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.RepairRequestMapper;
import com.generator.rental.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairServiceImpl extends ServiceImpl<RepairRequestMapper, RepairRequest> implements RepairService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public RepairRequest createRequest(Long orderId, String description, String imageUrl) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        
        RepairRequest request = new RepairRequest();
        request.setOrderId(orderId);
        request.setDescription(description);
        request.setImageUrl(imageUrl);
        request.setStatus(RepairRequest.Status.PENDING);
        
        save(request);
        return request;
    }

    @Override
    public RepairRequest updateStatus(Long id, String status, String response) {
        RepairRequest request = getById(id);
        if (request == null) throw new RuntimeException("Request not found");
        
        if (status != null) {
            request.setStatus(RepairRequest.Status.valueOf(status));
        }
        if (response != null) {
            request.setMerchantResponse(response);
        }
        
        updateById(request);
        return request;
    }

    @Override
    public List<RepairRequest> getByOrder(Long orderId) {
        return list(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getOrderId, orderId));
    }
}

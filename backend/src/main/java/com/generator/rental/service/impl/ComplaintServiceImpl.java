package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.entity.Complaint;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.ComplaintMapper;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Complaint createComplaint(Long orderId, String complainantUserId, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        
        User complainant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, complainantUserId));
        if (complainant == null) throw new RuntimeException("User not found");
        
        Complaint complaint = new Complaint();
        complaint.setOrderId(orderId);
        complaint.setComplainantId(complainant.getId());
        complaint.setContent(content);
        
        // Determine respondent
        // Need to fetch Tenant and Merchant IDs from Order
        // Since we stored tenantId and merchantId in Order, we can compare IDs directly
        
        if (complainant.getId().equals(order.getTenantId())) {
            complaint.setRespondentId(order.getMerchantId());
        } else {
            complaint.setRespondentId(order.getTenantId());
        }
        
        save(complaint);
        return complaint;
    }

    @Override
    public List<Complaint> getAllComplaints() {
        List<Complaint> complaints = list();
        // Populate Complainant and Respondent info for display
        // Batch fetch optimization possible, but loop for now
        for (Complaint c : complaints) {
            c.setComplainant(userMapper.selectById(c.getComplainantId()));
            if (c.getRespondentId() != null) {
                c.setRespondent(userMapper.selectById(c.getRespondentId()));
            }
        }
        return complaints;
    }

    @Override
    public void resolveComplaint(Long id, String resolution) {
        Complaint complaint = getById(id);
        if (complaint == null) throw new RuntimeException("Complaint not found");
        
        complaint.setResolution(resolution);
        complaint.setStatus(Complaint.Status.RESOLVED);
        updateById(complaint);
    }
}

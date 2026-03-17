package com.generator.rental.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantCustomerDTO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private String avatar;
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private LocalDateTime lastOrderTime;
    private String creditStatus; // 根据评价或欠款情况模拟
}

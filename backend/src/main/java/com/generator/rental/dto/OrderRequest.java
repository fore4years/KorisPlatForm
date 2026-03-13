package com.generator.rental.dto;

import com.generator.rental.entity.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRequest {
    private Long generatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
    private Order.PaymentMethod paymentMethod;
}

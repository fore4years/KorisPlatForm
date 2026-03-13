package com.generator.rental.dto;

import com.generator.rental.entity.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String generatorName;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal serviceFee;
    private Order.Status status;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
    
    // Delivery Info
    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
    private Order.DeliveryType deliveryType;
    private String deliveryEvidenceUrl;

    // Return Info
    private BigDecimal deductionAmount;
    private BigDecimal refundAmount;
    private String returnEvidenceUrl;
}

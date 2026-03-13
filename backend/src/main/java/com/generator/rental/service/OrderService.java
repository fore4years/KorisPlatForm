package com.generator.rental.service;

import com.generator.rental.dto.OrderRequest;
import com.generator.rental.dto.OrderResponse;
import com.generator.rental.dto.PriceCalculationResponse;

public interface OrderService {
    PriceCalculationResponse calculatePrice(Long generatorId, String startTime, String endTime);
    OrderResponse createOrder(OrderRequest request, String tenantUserId);
    OrderResponse confirmOrder(Long orderId);
    OrderResponse updateDelivery(Long orderId, String deliveryType);
    OrderResponse uploadDeliveryEvidence(Long orderId, String evidenceUrl);
    OrderResponse getOrderDetail(Long orderId);
    java.util.List<OrderResponse> getMerchantOrders(String merchantUserId);
    java.util.List<OrderResponse> getTenantOrders(String tenantUserId);
    OrderResponse completeOrder(Long orderId);
    OrderResponse confirmReturn(Long orderId, java.math.BigDecimal deductionAmount, String comment);
    OrderResponse confirmReceipt(Long orderId);
}

package com.generator.rental.dto;

import com.generator.rental.entity.Order;
import lombok.Data;

@Data
public class CartAddRequest {
    private Long generatorId;
    private Integer leaseDuration;
    private Order.DeliveryType deliveryType;
}

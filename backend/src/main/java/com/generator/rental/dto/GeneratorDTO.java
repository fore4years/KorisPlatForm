package com.generator.rental.dto;

import com.generator.rental.entity.Generator;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GeneratorDTO {
    private Long id;
    private String name;
    private String power;
    private BigDecimal dailyRent;
    private BigDecimal deposit;
    private String imageUrl;
    private String address;
    private String deliveryMethod;
    private Integer deliveryRange;
    private BigDecimal deliveryFee;
    private Generator.StockStatus stockStatus;
    private Double distance; // Calculated distance
    private Double averageRating;
}

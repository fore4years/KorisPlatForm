package com.generator.rental.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceCalculationResponse {
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private long days;
}

package com.generator.rental.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GeneratorSearchRequest {
    private String power;
    private BigDecimal minRent;
    private BigDecimal maxRent;
    private String location;
    private BigDecimal userLatitude;
    private BigDecimal userLongitude;
    private String sortType; // "price_asc", "price_desc", "distance", "rating"
}

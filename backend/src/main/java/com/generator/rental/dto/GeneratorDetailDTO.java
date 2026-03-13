package com.generator.rental.dto;

import com.generator.rental.entity.Generator;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GeneratorDetailDTO {
    private Long id;
    private String name;
    private String power;
    private String fuelConsumption;
    private String weight;
    private String size;
    private BigDecimal dailyRent;
    private BigDecimal weeklyRent;
    private BigDecimal monthlyRent;
    private BigDecimal deposit;
    private String deliveryMethod;
    private String imageUrl;
    private String description;
    private String address;
    private Generator.StockStatus stockStatus;
    
    private MerchantInfo merchant;
    private List<ReviewDTO> reviews;
    private Double averageRating;

    @Data
    public static class MerchantInfo {
        private Long id;
        private String userid;
        private String name;
        private String phone;
    }

    @Data
    public static class ReviewDTO {
        private Integer rating;
        private String comment;
        private String username;
        private String createdAt;
    }
}

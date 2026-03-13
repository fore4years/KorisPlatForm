package com.generator.rental.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsDTO {
    // Core Metrics
    private long totalUsers;
    private long totalMerchants;
    private long onlineGenerators;
    private long totalOrders;
    private BigDecimal totalTransactionVolume;

    // KPIs
    private double merchantOrderCompletionRate;
    private double tenantPositiveReviewRate;
    private double deviceFailureRate;

    // Trend Data (e.g., last 7 days or specified range)
    private List<String> dates;
    private List<Long> userGrowth;
    private List<Long> orderVolume;
    private List<BigDecimal> revenueGrowth;
}

package com.generator.rental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generator.rental.dto.StatisticsDTO;
import com.generator.rental.entity.*;
import com.generator.rental.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GeneratorMapper generatorMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private RepairRequestMapper repairRequestMapper;

    public StatisticsDTO   getStatistics(String timeDimension) {
        StatisticsDTO dto = new StatisticsDTO();

        // 1. 核心
        dto.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, User.Role.TENANT)));
        dto.setTotalMerchants(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, User.Role.MERCHANT)));
        dto.setOnlineGenerators(generatorMapper.selectCount(new LambdaQueryWrapper<Generator>().eq(Generator::getStockStatus, Generator.StockStatus.AVAILABLE)));
        dto.setTotalOrders(orderMapper.selectCount(null));

        List<Order> allOrders = orderMapper.selectList(null);
        BigDecimal totalVolume = allOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalTransactionVolume(totalVolume);

        // 2. KPIs
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .count();
        double completionRate = allOrders.isEmpty() ? 0 : (double) completedOrders / allOrders.size();
        dto.setMerchantOrderCompletionRate(round(completionRate * 100));

        List<Review> reviews = reviewMapper.selectList(null);
        long positiveReviews = reviews.stream()
                .filter(r -> r.getRating() != null && r.getRating() >= 4)
                .count();
        double reviewRate = reviews.isEmpty() ? 0 : (double) positiveReviews / reviews.size();
        dto.setTenantPositiveReviewRate(round(reviewRate * 100));

        // 设备下线率
        long repairCount = repairRequestMapper.selectCount(null);
        double failureRate = allOrders.isEmpty() ? 0 : (double) repairCount / allOrders.size();
        dto.setDeviceFailureRate(round(failureRate * 100));

        // 3. 趋势
        calculateTrends(dto, timeDimension);

        return dto;
    }

    private void calculateTrends(StatisticsDTO dto, String timeDimension) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        DateTimeFormatter formatter;
        ChronoUnit unit;

        switch (timeDimension.toLowerCase()) {
            case "week":
                startDate = now.minusWeeks(12);
                formatter = DateTimeFormatter.ofPattern("yyyy-w");
                unit = ChronoUnit.WEEKS;
                break;
            case "month":
                startDate = now.minusMonths(12);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                unit = ChronoUnit.MONTHS;
                break;
            case "year":
                startDate = now.minusYears(5);
                formatter = DateTimeFormatter.ofPattern("yyyy");
                unit = ChronoUnit.YEARS;
                break;
            case "day":
            default:
                startDate = now.minusDays(30);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                unit = ChronoUnit.DAYS;
                break;
        }

        Map<String, Long> userGrowthMap = new TreeMap<>();
        Map<String, Long> orderVolumeMap = new TreeMap<>();
        Map<String, BigDecimal> revenueMap = new TreeMap<>();

        LocalDateTime current = startDate;
        while (!current.isAfter(now)) {
            String key = current.format(formatter);
            userGrowthMap.put(key, 0L);
            orderVolumeMap.put(key, 0L);
            revenueMap.put(key, BigDecimal.ZERO);

            if (unit == ChronoUnit.DAYS) current = current.plusDays(1);
            else if (unit == ChronoUnit.WEEKS) current = current.plusWeeks(1);
            else if (unit == ChronoUnit.MONTHS) current = current.plusMonths(1);
            else if (unit == ChronoUnit.YEARS) current = current.plusYears(1);
        }

        // Process Users
        // Optimization: Fetch only users created after startDate
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().ge(User::getCreateTime, startDate));
        for (User u : users) {
            if (u.getCreateTime() != null) {
                String key = u.getCreateTime().format(formatter);
                if (userGrowthMap.containsKey(key)) {
                    userGrowthMap.put(key, userGrowthMap.get(key) + 1);
                }
            }
        }

        // Process Orders
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, startDate));
        for (Order o : orders) {
            if (o.getCreateTime() != null) {
                String key = o.getCreateTime().format(formatter);
                if (orderVolumeMap.containsKey(key)) {
                    orderVolumeMap.put(key, orderVolumeMap.get(key) + 1);
                    if (o.getTotalAmount() != null) {
                        revenueMap.put(key, revenueMap.get(key).add(o.getTotalAmount()));
                    }
                }
            }
        }

        dto.setDates(new ArrayList<>(userGrowthMap.keySet()));
        dto.setUserGrowth(new ArrayList<>(userGrowthMap.values()));
        dto.setOrderVolume(new ArrayList<>(orderVolumeMap.values()));
        dto.setRevenueGrowth(new ArrayList<>(revenueMap.values()));
    }

    private double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

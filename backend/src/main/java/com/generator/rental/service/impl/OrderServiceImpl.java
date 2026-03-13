package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.dto.OrderRequest;
import com.generator.rental.dto.OrderResponse;
import com.generator.rental.dto.PriceCalculationResponse;
import com.generator.rental.entity.Contract;
import com.generator.rental.entity.Generator;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.ContractMapper;
import com.generator.rental.mapper.GeneratorMapper;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.ContractService;
import com.generator.rental.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private GeneratorMapper generatorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractMapper contractMapper;

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    @Override
    public PriceCalculationResponse calculatePrice(Long generatorId, String startTimeStr, String endTimeStr) {
        Generator generator = generatorMapper.selectById(generatorId);
        if (generator == null) throw new RuntimeException("发电机不存在");

        LocalDateTime start = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1;

        BigDecimal rentAmount;
        if (days >= 30) {
            long months = days / 30;
            long remainingDays = days % 30;
            rentAmount = generator.getMonthlyRent().multiply(BigDecimal.valueOf(months))
                    .add(generator.getDailyRent().multiply(BigDecimal.valueOf(remainingDays)));
        } else if (days >= 7) {
            long weeks = days / 7;
            long remainingDays = days % 7;
            rentAmount = generator.getWeeklyRent().multiply(BigDecimal.valueOf(weeks))
                    .add(generator.getDailyRent().multiply(BigDecimal.valueOf(remainingDays)));
        } else {
            rentAmount = generator.getDailyRent().multiply(BigDecimal.valueOf(days));
        }

        BigDecimal deposit = generator.getDeposit();
        BigDecimal serviceFee = rentAmount.multiply(SERVICE_FEE_RATE);
        BigDecimal total = rentAmount.add(deposit).add(serviceFee);

        PriceCalculationResponse response = new PriceCalculationResponse();
        response.setRentAmount(rentAmount);
        response.setDepositAmount(deposit);
        response.setServiceFee(serviceFee);
        response.setTotalAmount(total);
        response.setDays(days);

        return response;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String tenantId) {
        Generator generator = generatorMapper.selectById(request.getGeneratorId());
        if (generator == null) throw new RuntimeException("发电机不存在");
        
        if (generator.getStockStatus() != Generator.StockStatus.AVAILABLE) {
            throw new RuntimeException("发电机不可用");
        }

        User tenant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, tenantId));
        if (tenant == null) throw new RuntimeException("租户不存在");

        PriceCalculationResponse price = calculatePrice(
                request.getGeneratorId(), 
                request.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME), 
                request.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        Order order = new Order();
        order.setTenantId(tenant.getId());
        order.setMerchantId(generator.getMerchantId());
        order.setGeneratorId(generator.getId());
        order.setStartTime(request.getStartTime());
        order.setEndTime(request.getEndTime());
        order.setTotalAmount(price.getTotalAmount());
        order.setDepositAmount(price.getDepositAmount());
        order.setServiceFee(price.getServiceFee());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setPaymentMethod(request.getPaymentMethod());

        if (request.getPaymentMethod() == Order.PaymentMethod.ONLINE) {
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.Status.WAIT_PAY); 
        } else {
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.Status.WAIT_CONFIRM);
        }

        save(order);
        
        // Populate transient fields for response
        order.setGenerator(generator);
        order.setTenant(tenant);
        
        return convertToResponse(order);
    }

    @Override
    public OrderResponse confirmOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        
        if (order.getStatus() != Order.Status.WAIT_CONFIRM) {
            throw new RuntimeException("订单状态不是待确认");
        }

        order.setStatus(Order.Status.CONFIRMED);
        updateById(order);
        
        contractService.generateContract(orderId);

        return convertToResponse(order);
    }

    @Override
    public OrderResponse updateDelivery(Long orderId, String deliveryType) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        
        Contract contract = contractMapper.selectOne(new LambdaQueryWrapper<Contract>().eq(Contract::getOrderId, orderId));
        if (contract == null) throw new RuntimeException("合同不存在");
        
        if (!Boolean.TRUE.equals(contract.getTenantSigned()) || !Boolean.TRUE.equals(contract.getMerchantSigned())) {
            throw new RuntimeException("双方必须签署合同后才能交付");
        }

        order.setDeliveryType(Order.DeliveryType.valueOf(deliveryType));
        order.setStatus(Order.Status.DELIVERED);
        updateById(order);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse uploadDeliveryEvidence(Long orderId, String evidenceUrl) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        
        order.setDeliveryEvidenceUrl(evidenceUrl);
        order.setStatus(Order.Status.RENTING);
        updateById(order);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse getOrderDetail(Long orderId) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        return convertToResponse(order);
    }

    @Override
    public List<OrderResponse> getMerchantOrders(String merchantUserId) {
        User merchant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, merchantUserId));
        if (merchant == null) return List.of();

        List<Order> orders = list(new LambdaQueryWrapper<Order>().eq(Order::getMerchantId, merchant.getId()));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getTenantOrders(String tenantUserId) {
        User tenant = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, tenantUserId));
        if (tenant == null) return List.of();

        List<Order> orders = list(new LambdaQueryWrapper<Order>().eq(Order::getTenantId, tenant.getId()));
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse completeOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if (order.getStatus() != Order.Status.RENTING) {
            throw new RuntimeException("订单不在租赁中");
        }
        
        order.setStatus(Order.Status.COMPLETED);
        
        Generator gen = generatorMapper.selectById(order.getGeneratorId());
        if (gen != null) {
            gen.setStockStatus(Generator.StockStatus.AVAILABLE);
            generatorMapper.updateById(gen);
        }
        
        updateById(order);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse confirmReturn(Long orderId, BigDecimal deductionAmount, String comment) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        
        BigDecimal deposit = order.getDepositAmount();
        BigDecimal refund = deposit.subtract(deductionAmount != null ? deductionAmount : BigDecimal.ZERO);
        
        order.setDeductionAmount(deductionAmount);
        order.setRefundAmount(refund);
        order.setStatus(Order.Status.COMPLETED);
        
        Generator gen = generatorMapper.selectById(order.getGeneratorId());
        if (gen != null) {
            gen.setStockStatus(Generator.StockStatus.AVAILABLE);
            generatorMapper.updateById(gen);
        }
        
        updateById(order);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse confirmReceipt(Long orderId) {
        Order order = getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        
        if (order.getStatus() != Order.Status.DELIVERED) {
            throw new RuntimeException("Order status is not DELIVERED");
        }
        
        order.setStatus(Order.Status.RENTING);
        updateById(order);
        return convertToResponse(order);
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        BeanUtils.copyProperties(order, response);
        
        // Populate Generator Info
        if (order.getGenerator() == null) {
            Generator gen = generatorMapper.selectById(order.getGeneratorId());
            if (gen != null) {
                response.setGeneratorName(gen.getName());
                order.setGenerator(gen);
            }
        } else {
            response.setGeneratorName(order.getGenerator().getName());
        }
        
        // Populate User Info if needed (optional based on DTO)
        
        response.setCreatedAt(order.getCreateTime());
        return response;
    }
}

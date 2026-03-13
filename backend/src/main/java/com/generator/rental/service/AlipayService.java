package com.generator.rental.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.generator.rental.config.AlipayConfigProperties;
import com.generator.rental.entity.Order;
import com.generator.rental.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlipayService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfigProperties alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    public String createPayOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if (order.getStatus() != Order.Status.WAIT_PAY && order.getStatus() != Order.Status.WAIT_CONFIRM) {
             if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
                throw new RuntimeException("订单已支付");
             }
        }
        
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new RuntimeException("订单已支付");
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        
        String bizContent = "{" +
                "\"out_trade_no\":\"" + orderId + "\"," +
                "\"total_amount\":\"" + order.getTotalAmount() + "\"," +
                "\"subject\":\"Generator Rental Order " + orderId + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}";
        
        request.setBizContent(bizContent);

        try {
            System.out.println("Initiating Alipay request for Order: " + orderId);
            System.out.println("Gateway: " + alipayConfig.getGatewayUrl());
            System.out.println("AppID: " + alipayConfig.getAppId());

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                System.out.println("Alipay form generated successfully");
                return response.getBody(); 
            } else {
                String errorMsg = "支付宝调用失败: Code=" + response.getCode() + 
                                  ", Msg=" + response.getMsg() + 
                                  ", SubCode=" + response.getSubCode() + 
                                  ", SubMsg=" + response.getSubMsg();
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝调用失败: " + e.getMessage(), e);
        }
    }

    public void handlePaymentSuccess(Long orderId, String tradeNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            return;
        }
        
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setTransactionId(tradeNo);
        
        if (order.getStatus() == Order.Status.WAIT_PAY) {
            order.setStatus(Order.Status.WAIT_CONFIRM);
        }
        
        orderMapper.updateById(order);
    }
}

package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.entity.Order;
import com.generator.rental.entity.Review;
import com.generator.rental.mapper.OrderMapper;
import com.generator.rental.mapper.ReviewMapper;
import com.generator.rental.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Review createReview(Long orderId, Integer rating, String comment) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("未发现订单");

        if (order.getStatus() != Order.Status.COMPLETED) {
            throw new RuntimeException("订单没有完成");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setTenantId(order.getTenantId());
        review.setGeneratorId(order.getGeneratorId());
        review.setRating(rating);
        review.setComment(comment);

        save(review);
        return review;
    }

    @Override
    public List<Review> getReviewsByGenerator(Long generatorId) {
        return list(new LambdaQueryWrapper<Review>().eq(Review::getGeneratorId, generatorId));
    }
}

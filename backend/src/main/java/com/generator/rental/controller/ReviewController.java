package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.entity.Review;
import com.generator.rental.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建评价
     *
     * @param payload 包含orderId, rating, comment的Map
     * @return 创建的评价实体
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('TENANT')")
    public Result<Review> create(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        Integer rating = Integer.valueOf(payload.get("rating").toString());
        String comment = (String) payload.get("comment");
        return Result.success(reviewService.createReview(orderId, rating, comment));
    }

    /**
     * 获取发电机的评价列表
     *
     * @param generatorId 发电机ID
     * @return 评价列表
     */
    @GetMapping("/generator/{generatorId}")
    public Result<List<Review>> getGeneratorReviews(@PathVariable Long generatorId) {
        return Result.success(reviewService.getReviewsByGenerator(generatorId));
    }
}

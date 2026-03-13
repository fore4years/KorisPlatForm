package com.generator.rental.service;

import com.generator.rental.entity.Review;
import java.util.List;

public interface ReviewService {
    Review createReview(Long orderId, Integer rating, String comment);
    List<Review> getReviewsByGenerator(Long generatorId);
}

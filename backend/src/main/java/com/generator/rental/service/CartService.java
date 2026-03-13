package com.generator.rental.service;

import com.generator.rental.entity.CartItem;
import com.generator.rental.dto.CartAddRequest;
import java.util.List;

public interface CartService {
    List<CartItem> getCart(String userId);
    CartItem addToCart(String userId, CartAddRequest request);
    void deleteItem(String userId, Long cartId);
    void deleteItems(String userId, List<Long> cartIds);
}

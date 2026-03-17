package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.dto.CartAddRequest;
import com.generator.rental.entity.CartItem;
import com.generator.rental.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('TENANT')")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    @GetMapping
    public Result<List<CartItem>> getCart(@RequestHeader("X-User-ID") String userId) {
        return Result.success(cartService.getCart(userId));
    }

    /**
     * 添加商品到购物车
     *
     * @param userId 用户ID
     * @param request 购物车添加请求 DTO
     * @return 添加的购物车商品
     */
    @PostMapping("/add")
    public Result<CartItem> addToCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CartAddRequest request) {
        return Result.success(cartService.addToCart(userId, request));
    }

    /**
     * 删除购物车商品
     *
     * @param userId 用户ID
     * @param id 购物车项ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable Long id) {
        cartService.deleteItem(userId, id);
        return Result.success();
    }

    /**
     * 批量删除购物车商品
     *
     * @param userId 用户ID
     * @param body 包含ID列表的 Map
     * @return 空响应
     */
    @PostMapping("/batch-delete")
    public Result<Void> deleteItems(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody Map<String, List<Long>> body) {
        cartService.deleteItems(userId, body.get("ids"));
        return Result.success();
    }
}

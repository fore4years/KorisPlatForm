package com.generator.rental.controller;

import com.generator.rental.common.Result;
import com.generator.rental.entity.Favorite;
import com.generator.rental.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    @GetMapping
    public Result<List<Favorite>> getFavorites(@RequestHeader("X-User-ID") String userId) {
        return Result.success(favoriteService.getFavorites(userId));
    }

    /**
     * 切换收藏状态（添加/取消）
     *
     * @param userId 用户ID
     * @param body 包含发电机ID的 Map
     * @return 包含最新收藏状态的 Map
     */
    @PostMapping("/toggle")
    public Result<Map<String, Boolean>> toggleFavorite(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody Map<String, Long> body) {
        boolean isFavorited = favoriteService.toggleFavorite(userId, body.get("generatorId"));
        return Result.success(Map.of("favorited", isFavorited));
    }
}

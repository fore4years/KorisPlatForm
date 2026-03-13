package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.entity.Favorite;
import com.generator.rental.entity.Generator;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.FavoriteMapper;
import com.generator.rental.mapper.GeneratorMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GeneratorMapper generatorMapper;

    @Override
    public List<Favorite> getFavorites(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return List.of();

        List<Favorite> favorites = list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .orderByDesc(Favorite::getCreateTime));
        
        // Populate Generator and filter out invalid ones
        favorites.forEach(f -> {
            Generator gen = generatorMapper.selectById(f.getGeneratorId());
            f.setGenerator(gen);
        });
        
        favorites.removeIf(f -> f.getGenerator() == null);
        
        return favorites;
    }

    @Override
    public boolean toggleFavorite(String userId, Long generatorId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) throw new RuntimeException("User not found");

        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .eq(Favorite::getGeneratorId, generatorId);
        
        if (count(query) > 0) {
            remove(query);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(user.getId());
            favorite.setGeneratorId(generatorId);
            save(favorite);
            return true;
        }
    }
}

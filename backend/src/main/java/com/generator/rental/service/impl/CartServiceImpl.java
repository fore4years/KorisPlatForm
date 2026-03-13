package com.generator.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generator.rental.dto.CartAddRequest;
import com.generator.rental.entity.CartItem;
import com.generator.rental.entity.Generator;
import com.generator.rental.entity.User;
import com.generator.rental.mapper.CartItemMapper;
import com.generator.rental.mapper.GeneratorMapper;
import com.generator.rental.mapper.UserMapper;
import com.generator.rental.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    @Autowired
    private GeneratorMapper generatorMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<CartItem> getCart(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return List.of();

        List<CartItem> items = list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, user.getId()));
        
        // Populate Generator and filter out invalid ones
        items.forEach(item -> {
            Generator gen = generatorMapper.selectById(item.getGeneratorId());
            item.setGenerator(gen);
        });
        
        items.removeIf(item -> item.getGenerator() == null);
        
        return items;
    }

    @Override
    public CartItem addToCart(String userId, CartAddRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) throw new RuntimeException("User not found");
        
        Generator generator = generatorMapper.selectById(request.getGeneratorId());
        if (generator == null) throw new RuntimeException("Generator not found");

        if (generator.getStockStatus() != Generator.StockStatus.AVAILABLE) {
            throw new RuntimeException("Generator is not available");
        }

        CartItem item = new CartItem();
        item.setUserId(user.getId());
        item.setGeneratorId(generator.getId());
        item.setLeaseDuration(request.getLeaseDuration());
        item.setDeliveryType(request.getDeliveryType());
        
        save(item);
        
        // Populate for return
        item.setGenerator(generator);
        
        return item;
    }

    @Override
    public void deleteItem(String userId, Long cartId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return;

        remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, user.getId())
                .eq(CartItem::getId, cartId));
    }

    @Override
    public void deleteItems(String userId, List<Long> cartIds) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return;

        remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, user.getId())
                .in(CartItem::getId, cartIds));
    }
}

package com.generator.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generator.rental.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}

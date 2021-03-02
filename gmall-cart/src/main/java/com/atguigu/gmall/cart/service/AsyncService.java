package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author hehao
 * @create 2021-02-24 16:45
 */
@Service
public class AsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(String userId, Long skuId, Cart cart) {
        this.cartMapper.update(cart, new QueryWrapper<Cart>().eq("sku_id", skuId).eq("user_id", userId));
    }

    @Async
    public void saveCart(String userId, Cart cart) {
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCart(String userKey) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userKey));
    }

    @Async
    public void deleteCartBySkuId(String userKey, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userKey).eq("sku_id", skuId));
    }
}

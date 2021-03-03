package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;
import org.springframework.util.concurrent.ListenableFuture;


import java.util.List;
import java.util.concurrent.Future;

/**
 * @author hehao
 * @create 2021-02-23 17:18
 */
public interface CartService {
    void saveCart(Cart cart);

    Cart queryCartBySkuId(Long skuId);

    void executor1();

   void executor2();

    List<Cart> showCart();

    void updateNum(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> checkSelCart(Long userId);
}

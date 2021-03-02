package com.atguigu.gmall.cart.entity.api;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-26 23:24
 */
public interface GmallCartApi {

    /**
     * 获取登录用户选中的购物车
     */
    @ResponseBody
    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> checkSelCart(@PathVariable("userId") Long userId);
}

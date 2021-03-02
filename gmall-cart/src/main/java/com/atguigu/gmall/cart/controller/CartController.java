package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.intercept.LoginInterceptHandler;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author hehao
 * @create 2021-02-23 17:18
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request) {
        System.out.println(LoginInterceptHandler.getUserInfo());
        long now = System.currentTimeMillis();
        cartService.executor1();
        cartService.executor2();
//        stringFuture.addCallback(result ->{
//            System.out.println(result);
//        },ex -> {
//            System.out.println(ex.getMessage());
//        });
//        stringFuture1.addCallback(result ->{
//            System.out.println(result);
//        },ex -> {
//            System.out.println(ex.getMessage());
//        });
        System.out.println("当前controller执行的时间为" + (System.currentTimeMillis() - now));
        return "hello test";
    }

    /**
     * 保存一条购物车记录
     */
    @GetMapping()
    public String saveCart(Cart cart) {
        this.cartService.saveCart(cart);
        //重定向到加入购物车成功页面，进行查询回显
        return "redirect:http://cart.gmall.com/addToCart.html?skuId=" + cart.getSkuId();
    }

    /**
     * 回显购物车数据
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("addToCart.html")
    public String addCart(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }


    /**
     * 获取用户购物车信息
     *
     * @param model
     * @return
     */
    @GetMapping("cart.html")
    public String showCart(Model model) {
        List<Cart> carts = this.cartService.showCart();
        model.addAttribute("carts", carts);
        return "cart";
    }

    /**
     * 更改购物车订单数量
     *
     * @param cart 获取skuId和count
     * @return
     */
    @ResponseBody
    @PostMapping("/updateNum")
    public ResponseVo updateNum(@RequestBody Cart cart) {
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    /**
     * 删除购物车记录
     *
     * @param skuId
     * @return
     */
    @ResponseBody
    @PostMapping("/deleteCart")
    public ResponseVo deleteCart(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    /**
     * 获取登录用户选中的购物车
     */
    @ResponseBody
    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> checkSelCart(@PathVariable("userId") Long userId) {
        List<Cart> cartList = this.cartService.checkSelCart(userId);
        return ResponseVo.ok(cartList);
    }
}

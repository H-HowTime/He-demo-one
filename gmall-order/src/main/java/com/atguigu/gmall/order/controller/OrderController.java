package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;

import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.servie.OrderService;
import com.atguigu.gmall.order.vo.ConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hehao
 * @create 2021-02-26 21:12
 */
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping("confirm")
    public String confirm(Model model) {
        ConfirmVo confirmVo = this.orderService.orderConfirm();
        model.addAttribute("confirmVo", confirmVo);
        return "trade";
    }

    /**
     * 提交订单
     *
     * @return
     */
    @ResponseBody
    @PostMapping("submit")
    public ResponseVo<String> orderSubmit(@RequestBody OrderSubmitVo orderSubmitVo) {
        orderService.orderSubmit(orderSubmitVo);
        return ResponseVo.ok(orderSubmitVo.getOrderToken());
    }
}

package com.atguigu.gmall.order.servie;

import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.ConfirmVo;

/**
 * @author hehao
 * @create 2021-02-26 23:40
 */
public interface OrderService {
    ConfirmVo orderConfirm();

    void orderSubmit(OrderSubmitVo orderSubmitVo);
}

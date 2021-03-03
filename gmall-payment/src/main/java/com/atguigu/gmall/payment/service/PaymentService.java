package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.vo.PayAsyncVo;

/**
 * @author hehao
 * @create 2021-03-01 17:48
 */
public interface PaymentService {
    OrderEntity toPay(String orderToken);

    String alipay(String orderToken);

    String payOk(PayAsyncVo payAsyncVo);

    OrderEntity queryOrderByToken(String orderToken);

}

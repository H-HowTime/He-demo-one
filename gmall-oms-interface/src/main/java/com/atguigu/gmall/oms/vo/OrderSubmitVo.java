package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hehao
 * @create 2021-02-27 20:07
 */
@Data
public class OrderSubmitVo {

    private String orderToken; //: [[${confirmVo.orderToken}]],  // 订单编号 防止重复提交
    private UserAddressEntity address; // {}, // 用户选中的收货地址
    private Integer payType; // 1 支付类型 1-
    private String deliveryCompany; //"顺丰快递",
    private Integer bounds; //[[${confirmVo.bounds}]],   // 积分信息
    private List<OrderItemVo> items; //[[${confirmVo.orderItems}]], // 送货清单
    private BigDecimal totalPrice; // 0 验证价格使用
}

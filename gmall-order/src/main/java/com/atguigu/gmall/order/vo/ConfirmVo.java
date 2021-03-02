package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-26 22:41
 */
@Data
public class ConfirmVo {

    //收件人地址信息列表
    private List<UserAddressEntity> address;

    //商品清单列表
    private List<OrderItemVo> orderItems;

    //积分
    private Integer bounds;

    //订单详情页唯一标识 防止订单反复提交，避免接口幂等性问题
    private String orderToken;
}

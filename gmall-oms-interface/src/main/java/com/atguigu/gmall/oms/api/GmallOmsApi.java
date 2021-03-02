package com.atguigu.gmall.oms.api;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hehao
 * @create 2021-02-22 12:56
 */
public interface GmallOmsApi {

    /**
     * 下单
     */
    @PostMapping("oms/order/save/{userId}")
    @ApiOperation("下单")
    public ResponseVo downOrder(@RequestBody OrderSubmitVo orderSubmitVo, @PathVariable("userId") Long userId);

    /**
     * 根据订单编号查询订单信息
     */
    @GetMapping("oms/order/token/{orderToken}")
    @ApiOperation("根据订单编号查询订单信息")
    public ResponseVo<OrderEntity> queryOrderByOrderToken(@PathVariable("orderToken") String orderToken);
}

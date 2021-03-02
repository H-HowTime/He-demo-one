package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 21:19:35
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    Integer updateStatus(@Param("orderToken") String orderToken, @Param("expectStatus")Integer expectStatus, @Param("targetStatus")Integer targetStatus);
}

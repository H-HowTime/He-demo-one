package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.payment.pojo.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hehao
 * @create 2021-03-01 21:00
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentInfoEntity> {
}

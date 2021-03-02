package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.cart.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author hehao
 * @create 2021-02-23 17:44
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}

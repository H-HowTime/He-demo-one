package com.atguigu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.entity.Cart;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-26 19:15
 */
@Component
public class CartXxlJob {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String EXCEPTION_KEY = "cart:exception:info";
    private static final String KEY_PREFIX = "cart:info:";


    @XxlJob("SyncCartData")
    public ReturnT<String> syncCartData(String param) {
        //查询失败购物车记录userId
        BoundSetOperations<String, String> setOps = this.stringRedisTemplate.boundSetOps(EXCEPTION_KEY);
        if (setOps.size() == 0) {
            //如果失败购物记录为null，则直接返回
            return ReturnT.SUCCESS;
        }
        //如果不为空，获取userId
        String userId;
        while (StringUtils.isNotBlank(userId = setOps.pop())) {
            //如果存在，删除mysql中的该userId对应的购物车记录
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
            //查询redis获取购物车记录
            if (this.stringRedisTemplate.hasKey(KEY_PREFIX + userId)) {
                //获取redis中的数据
                BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
                if (hashOps.size() == 0) {
                    //如果不存在，则直接返回，继续下次循环
                    continue;
                }
                List<Object> carts = hashOps.values();
                carts.forEach(cartJson -> {
                    this.cartMapper.insert(JSON.parseObject(cartJson.toString(), Cart.class));
                });
            }
        }
        return ReturnT.SUCCESS;
    }
}

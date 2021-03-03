package com.atguigu.gmall.wms.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author hehao
 * @create 2021-02-28 17:18
 */
@Component
public class StockListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String LOCK_SUCCESS_PREFIX = "store:lock:success:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "STOCK_MINUS_QUEUE", durable = "true"),
            exchange = @Exchange(name = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void paySuccessMinusStockListener(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //获取锁库存信息缓存
        String lockVosJson = this.stringRedisTemplate.opsForValue().get(LOCK_SUCCESS_PREFIX + orderToken);
        if (StringUtils.isBlank(lockVosJson)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //减少库存、增加销量、同时解锁库存
        List<SkuLockVo> skuLockVos = JSON.parseArray(lockVosJson, SkuLockVo.class);
        skuLockVos.forEach(skuLockVo -> {
            this.wareSkuMapper.minusStock(skuLockVo.getCount(), skuLockVo.getWareId());
        });
        //解锁库存成功，要删除锁库存成功的缓存，以防止重复解锁库存
        this.stringRedisTemplate.delete(LOCK_SUCCESS_PREFIX + orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "STOCK_UNLOCK_QUEUE", durable = "true"),
            exchange = @Exchange(name = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.disable"}
    ))
    public void unLockListener(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //获取锁库存信息缓存
        String lockVosJson = this.stringRedisTemplate.opsForValue().get(LOCK_SUCCESS_PREFIX + orderToken);
        if (StringUtils.isBlank(lockVosJson)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //解锁库存
        List<SkuLockVo> skuLockVos = JSON.parseArray(lockVosJson, SkuLockVo.class);
        skuLockVos.forEach(skuLockVo -> {
            this.wareSkuMapper.unLockStock(skuLockVo.getCount(), skuLockVo.getWareId());
        });
        //解锁库存成功，要删除锁库存成功的缓存，以防止重复解锁库存
        this.stringRedisTemplate.delete(LOCK_SUCCESS_PREFIX + orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

//    @RabbitListener(queues = "DEAD_STOCK_UNLOCK_QUEUE")
//    public void unLockTtl(String orderToken, Message message, Channel channel) throws IOException {
//        if (StringUtils.isBlank(orderToken)) {
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            return;
//        }
//        //获取锁库存信息缓存
//        String lockVosJson = this.stringRedisTemplate.opsForValue().get(LOCK_SUCCESS_PREFIX + orderToken);
//        if (StringUtils.isBlank(lockVosJson)) {
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            return;
//        }
//        //解锁库存
//        List<SkuLockVo> skuLockVos = JSON.parseArray(lockVosJson, SkuLockVo.class);
//        skuLockVos.forEach(skuLockVo -> {
//            this.wareSkuMapper.unLockStock(skuLockVo.getCount(), skuLockVo.getWareId());
//        });
//        //解锁库存成功，要删除锁库存成功的缓存，以防止重复解锁库存
//        this.stringRedisTemplate.delete(LOCK_SUCCESS_PREFIX + orderToken);
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }
}

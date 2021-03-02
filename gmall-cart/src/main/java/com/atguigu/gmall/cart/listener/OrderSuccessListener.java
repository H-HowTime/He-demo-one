package com.atguigu.gmall.cart.listener;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Method;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author hehao
 * @create 2021-02-28 16:33
 */
@Component
public class OrderSuccessListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "cart:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ORDER_SUCCESS_QUEUE", durable = "true"),
            exchange = @Exchange(name = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void orderSuccessListener(Map<String, Object> selectedCartsInfo, Message message, Channel channel) throws IOException {
        if (CollectionUtils.isEmpty(selectedCartsInfo)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //获取userId和skuIds
        Long userId = (Long)selectedCartsInfo.get("userId");
        String skuIdsJson = selectedCartsInfo.get("skuIds").toString();
        List<String> skuIds = JSON.parseArray(skuIdsJson, String.class);
        if (CollectionUtils.isEmpty(skuIds)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //删除购物车记录
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuIds.toArray());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}

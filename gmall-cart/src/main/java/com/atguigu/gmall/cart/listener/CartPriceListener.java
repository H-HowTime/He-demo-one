package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.GmallPmsFeignClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author hehao
 * @create 2021-02-24 19:23
 */
@Component
public class CartPriceListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    private static final String PRICE_PREFIX = "cart:price:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "CART_PRICE_QUEUE", durable = "true"),
            exchange = @Exchange(name = "CART_PRICE_EXCHANGE", ignoreDeclarationExceptions = "true", durable = "true", type = ExchangeTypes.TOPIC),
            key = {"price.update"}
    ))
    public void priceListener(Long spuId, Message message, Channel channel) throws IOException { //1-消息实体 2-消息对象 3-消息管道对象
        if (spuId == null) {
            //手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //监听到消息,获取所有sku信息，更新价格缓存
        ResponseVo<List<SkuEntity>> listResponseVo = gmallPmsFeignClient.querySkuBySpuId(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            //遍历所有sku,同步sku实时价格
            skuEntities.forEach(skuEntity -> {
                if (this.stringRedisTemplate.hasKey(PRICE_PREFIX + skuEntity.getId())) {
                    //价格缓存中存在当前sku，则更新当前sku对应的实时价格
                    this.stringRedisTemplate.opsForValue().set(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
                    System.out.println(skuEntity.getPrice());
                }
            });
            //手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            //手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;

        }
    }
}
